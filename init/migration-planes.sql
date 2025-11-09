-- ============================================================================
-- MIGRACIÓN: Módulo de Planes de Suscripción
-- Descripción: Crea la tabla planes y agrega la relación con empresas
-- Fecha: 2025-11-09
-- ============================================================================

-- ============================================================================
-- 1. CREAR TABLA PLANES
-- ============================================================================

CREATE TABLE IF NOT EXISTS planes (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    precio DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    duracion_meses INTEGER NOT NULL DEFAULT 1,
    limite_usuarios INTEGER,                    -- NULL = ilimitado
    limite_reservas_mes INTEGER,                -- NULL = ilimitado
    limite_servicios INTEGER,                   -- NULL = ilimitado
    limite_promociones INTEGER,                 -- NULL = ilimitado
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mejorar consultas
CREATE INDEX idx_planes_tipo ON planes(tipo);
CREATE INDEX idx_planes_activo ON planes(activo);
CREATE INDEX idx_planes_deleted ON planes(deleted);

-- ============================================================================
-- 2. INSERTAR PLANES INICIALES (GRATUITO, BASICO, PREMIUM)
-- ============================================================================

-- Plan GRATUITO
INSERT INTO planes (
    tipo, 
    nombre, 
    descripcion, 
    precio, 
    duracion_meses, 
    limite_usuarios, 
    limite_reservas_mes, 
    limite_servicios, 
    limite_promociones, 
    activo
) VALUES (
    'GRATUITO',
    'Plan Gratuito',
    'Plan básico gratuito con funcionalidades limitadas',
    0.00,
    1,
    1,      -- Máximo 1 usuario
    10,     -- Máximo 10 reservas por mes
    3,      -- Máximo 3 servicios
    0,      -- Sin promociones
    TRUE
) ON CONFLICT (tipo) DO NOTHING;

-- Plan BASICO
INSERT INTO planes (
    tipo, 
    nombre, 
    descripcion, 
    precio, 
    duracion_meses, 
    limite_usuarios, 
    limite_reservas_mes, 
    limite_servicios, 
    limite_promociones, 
    activo
) VALUES (
    'BASICO',
    'Plan Básico',
    'Hasta 2 usuarios y 50 reservas/mes',
    29.99,
    1,
    2,      -- Máximo 2 usuarios
    50,     -- Máximo 50 reservas por mes
    10,     -- Máximo 10 servicios
    2,      -- Máximo 2 promociones activas
    TRUE
) ON CONFLICT (tipo) DO NOTHING;

-- Plan PREMIUM
INSERT INTO planes (
    tipo, 
    nombre, 
    descripcion, 
    precio, 
    duracion_meses, 
    limite_usuarios, 
    limite_reservas_mes, 
    limite_servicios, 
    limite_promociones, 
    activo
) VALUES (
    'PREMIUM',
    'Plan Premium',
    'Hasta 10 usuarios y reservas ilimitadas',
    99.99,
    1,
    10,     -- Máximo 10 usuarios
    NULL,   -- Reservas ilimitadas
    NULL,   -- Servicios ilimitados
    NULL,   -- Promociones ilimitadas
    TRUE
) ON CONFLICT (tipo) DO NOTHING;

-- ============================================================================
-- 3. AGREGAR COLUMNA plan_id A LA TABLA empresas
-- ============================================================================

-- Verificar si la columna ya existe antes de agregarla
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'empresas' 
        AND column_name = 'plan_id'
    ) THEN
        ALTER TABLE empresas ADD COLUMN plan_id BIGINT;
        
        -- Crear la foreign key
        ALTER TABLE empresas 
        ADD CONSTRAINT fk_empresas_plan 
        FOREIGN KEY (plan_id) 
        REFERENCES planes(id);
        
        -- Crear índice para mejorar consultas
        CREATE INDEX idx_empresas_plan_id ON empresas(plan_id);
        
        RAISE NOTICE 'Columna plan_id agregada exitosamente a la tabla empresas';
    ELSE
        RAISE NOTICE 'La columna plan_id ya existe en la tabla empresas';
    END IF;
END $$;

-- ============================================================================
-- 4. ASIGNAR PLAN GRATUITO A EMPRESAS EXISTENTES (OPCIONAL)
-- ============================================================================

-- Asignar el plan GRATUITO a todas las empresas que no tienen plan asignado
UPDATE empresas 
SET plan_id = (SELECT id FROM planes WHERE tipo = 'GRATUITO' LIMIT 1)
WHERE plan_id IS NULL AND deleted = FALSE;

-- ============================================================================
-- 5. VERIFICACIÓN
-- ============================================================================

-- Verificar que los planes fueron creados correctamente
DO $$
DECLARE
    total_planes INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_planes FROM planes WHERE deleted = FALSE;
    
    IF total_planes >= 3 THEN
        RAISE NOTICE 'Migración exitosa: % planes creados', total_planes;
    ELSE
        RAISE WARNING 'Advertencia: Solo se crearon % planes', total_planes;
    END IF;
END $$;

-- ============================================================================
-- FIN DE LA MIGRACIÓN
-- ============================================================================
