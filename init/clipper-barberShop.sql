CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


-- --------------------------------------------------------
-- Tabla de empresas (tenants)
-- --------------------------------------------------------
CREATE TABLE empresas (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    direccion VARCHAR(255),
    telefono VARCHAR(50),
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'ACTIVA' CHECK (estado IN ('ACTIVA','INACTIVA'))
);

-- --------------------------------------------------------
-- Tabla de planes SaaS
-- --------------------------------------------------------
CREATE TABLE planes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    duracion_meses INT NOT NULL,
    max_usuarios INT,
    max_reservas INT
);

-- --------------------------------------------------------
-- Tabla de suscripciones
-- --------------------------------------------------------
CREATE TABLE suscripciones (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(20) DEFAULT 'ACTIVA' CHECK (estado IN ('ACTIVA','EXPIRADA','CANCELADA')),
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES planes(id),
    UNIQUE(empresa_id) -- Una empresa solo puede tener una suscripción activa a la vez
);

-- --------------------------------------------------------
-- Tabla de pagos
-- --------------------------------------------------------
CREATE TABLE pagos (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    suscripcion_id BIGINT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    metodo VARCHAR(20) CHECK (metodo IN ('CARD','PAYPAL','TRANSFERENCIA')),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE','COMPLETADO','FALLIDO')),
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (suscripcion_id) REFERENCES suscripciones(id)
);    @Column(nullable = false, length = 60)
    private String password; // BCrypt hash always generates 60 characters

-- --------------------------------------------------------
-- Configuración de empresa
-- --------------------------------------------------------
CREATE TABLE configuracion_empresa (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL UNIQUE,
    logo_url VARCHAR(255),
    color_tema VARCHAR(50),
    zona_horaria VARCHAR(50) DEFAULT 'UTC',
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
);

-- --------------------------------------------------------
-- Usuarios
-- --------------------------------------------------------
CREATE TABLE usuarios (
    id VARCHAR(255) PRIMARY KEY DEFAULT uuid_generate_v4()::VARCHAR,
    empresa_id BIGINT,
    email VARCHAR(255) UNIQUE NOT NULL,
    last_name VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER','ADMIN','CLIENT','EMPLOYEE')),
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
);

-- --------------------------------------------------------
-- Servicios
-- --------------------------------------------------------
CREATE TABLE servicios (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    description VARCHAR(255),
    duration INT NOT NULL CHECK (duration > 0),
    name VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL CHECK (price >= 0),
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
);

-- --------------------------------------------------------
-- Promociones
-- --------------------------------------------------------
CREATE TABLE promociones (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    description VARCHAR(255),
    discount DOUBLE PRECISION NOT NULL CHECK (discount >= 0 AND discount <= 100),
    end_date DATE NOT NULL,
    start_date DATE NOT NULL,
    servicio_id BIGINT NOT NULL,
    activa BOOLEAN DEFAULT TRUE,
    CHECK (end_date >= start_date),
    FOREIGN KEY (servicio_id) REFERENCES servicios(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
);

-- --------------------------------------------------------
-- Disponibilidad
-- --------------------------------------------------------
CREATE TABLE disponibilidad (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    employee_id VARCHAR(255) NOT NULL,
    disponible BOOLEAN DEFAULT TRUE,
    CHECK (hora_fin > hora_inicio),
    FOREIGN KEY (employee_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    UNIQUE(employee_id, fecha, hora_inicio) -- Evitar duplicados
);

-- --------------------------------------------------------
-- Reservas
-- --------------------------------------------------------
CREATE TABLE reservas (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    final_price DOUBLE PRECISION CHECK (final_price >= 0),
    reservation_date TIMESTAMP NOT NULL,
    duracion_minutos INT NOT NULL CHECK (duracion_minutos > 0),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING','CONFIRMED','CANCELLED','COMPLETED')),
    client_id VARCHAR(255) NOT NULL,
    employee_id VARCHAR(255) NOT NULL,
    service_id BIGINT NOT NULL,
    promocion_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES servicios(id) ON DELETE CASCADE,
    FOREIGN KEY (promocion_id) REFERENCES promociones(id) ON DELETE SET NULL,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
);

-- --------------------------------------------------------
-- Reseñas
-- --------------------------------------------------------
CREATE TABLE resenias (
    id BIGSERIAL PRIMARY KEY,
    reserva_id BIGINT NOT NULL UNIQUE, -- Una reseña por reserva
    cliente_id VARCHAR(255) NOT NULL,
    empleado_id VARCHAR(255) NOT NULL,
    empresa_id BIGINT NOT NULL,
    calificacion_servicio INT NOT NULL CHECK (calificacion_servicio BETWEEN 1 AND 5),
    calificacion_empleado INT CHECK (calificacion_empleado BETWEEN 1 AND 5),
    comentario TEXT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reserva_id) REFERENCES reservas(id) ON DELETE CASCADE,
    FOREIGN KEY (cliente_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (empleado_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
);

-- --------------------------------------------------------
-- Logs de actividad
-- --------------------------------------------------------
CREATE TABLE logs_actividad (
    id BIGSERIAL PRIMARY KEY,
    usuario_id VARCHAR(255),
    empresa_id BIGINT NOT NULL,
    accion VARCHAR(255) NOT NULL,
    tabla_afectada VARCHAR(100),
    registro_id VARCHAR(100),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_usuarios_empresa_role ON usuarios(empresa_id, role);
CREATE INDEX idx_servicios_empresa_activo ON servicios(empresa_id, activo);
CREATE INDEX idx_reservas_fecha_status ON reservas(reservation_date, status);
CREATE INDEX idx_reservas_cliente_empresa ON reservas(client_id, empresa_id);
CREATE INDEX idx_promociones_fechas ON promociones(start_date, end_date, activa);
CREATE INDEX idx_disponibilidad_empleado_fecha ON disponibilidad(employee_id, fecha);
CREATE INDEX idx_logs_empresa_fecha ON logs_actividad(empresa_id, fecha);