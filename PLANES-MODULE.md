# Módulo de Planes de Suscripción

## 📋 Descripción General

El módulo de planes permite gestionar diferentes niveles de suscripción para las empresas (barberías) registradas en el sistema. Cada plan define límites de recursos que una empresa puede utilizar (usuarios, reservas, servicios, promociones).

**Importante**: Los planes están diseñados exclusivamente para:
- **ADMIN**: Gestión completa (CRUD) de planes
- **OWNER**: Consulta de planes disponibles para suscribirse y pagar

Los **CLIENT** (clientes de las barberías) NO tienen acceso a planes, ya que la aplicación está dirigida a dueños de barberías que necesitan pagar para realizar diferentes acciones.

## 🎯 Características Principales

- **3 Tipos de Planes**: GRATUITO, BASICO, PREMIUM
- **Límites Configurables**: Usuarios, reservas mensuales, servicios y promociones
- **Validación Automática**: Servicio que valida límites antes de crear recursos
- **Soft Delete**: Los planes eliminados se marcan como deleted pero no se borran físicamente
- **Control de Acceso**: 
  - **ADMIN**: CRUD completo de planes
  - **OWNER**: Consulta de planes activos para suscripción

## 📊 Tipos de Planes Predefinidos

### Plan GRATUITO
- **Precio**: $0.00/mes
- **Usuarios**: Máximo 1
- **Reservas**: 10 por mes
- **Servicios**: Máximo 3
- **Promociones**: 0

### Plan BÁSICO
- **Precio**: $29.99/mes
- **Usuarios**: Máximo 2
- **Reservas**: 50 por mes
- **Servicios**: Máximo 10
- **Promociones**: Máximo 2

### Plan PREMIUM
- **Precio**: $99.99/mes
- **Usuarios**: Máximo 10
- **Reservas**: ♾️ Ilimitadas
- **Servicios**: ♾️ Ilimitados
- **Promociones**: ♾️ Ilimitadas

## 🏗️ Arquitectura

```
plan/
├── adapters/
│   ├── in/
│   │   └── web/
│   │       ├── PlanOwnerController.java        (ADMIN - CRUD)
│   │       └── PlanOwnerQueryController.java   (OWNER - Consulta)
│   └── out/
│       └── persistence/
│           ├── SpringDataPlanRepository.java
│           └── PlanRepositoryAdapter.java
├── application/
│   ├── dto/
│   │   ├── PlanRequest.java
│   │   ├── ActualizarPlanRequest.java
│   │   └── PlanResponse.java
│   └── service/
│       ├── PlanOwnerService.java
│       ├── PlanClientService.java
│       └── PlanLimitService.java          (Validación de límites)
└── domain/
    ├── exception/
    │   ├── PlanNotFoundException.java
    │   ├── PlanLimitExceededException.java
    │   └── PlanAlreadyExistsException.java
    └── port/
        └── out/
            └── PlanRepositoryPort.java

Entities/
├── Plan.java                               (Entidad JPA)
└── enums/
    └── TipoPlan.java                       (GRATUITO, BASICO, PREMIUM)
```

## 🔌 API Endpoints

### Endpoints para ADMIN

#### 1. Crear Plan
```http
POST /api/admin/planes
Authorization: Bearer {token}
Content-Type: application/json

{
  "tipo": "BASICO",
  "nombre": "Plan Básico",
  "descripcion": "Hasta 2 usuarios y 50 reservas/mes",
  "precio": 29.99,
  "duracionMeses": 1,
  "limiteUsuarios": 2,
  "limiteReservasMes": 50,
  "limiteServicios": 10,
  "limitePromociones": 2,
  "activo": true
}
```

**Respuesta exitosa (201):**
```json
{
  "id": 1,
  "tipo": "BASICO",
  "nombre": "Plan Básico",
  "descripcion": "Hasta 2 usuarios y 50 reservas/mes",
  "precio": 29.99,
  "duracionMeses": 1,
  "limiteUsuarios": 2,
  "limiteReservasMes": 50,
  "limiteServicios": 10,
  "limitePromociones": 2,
  "activo": true
}
```

#### 2. Obtener Plan por ID
```http
GET /api/admin/planes/{id}
Authorization: Bearer {token}
```

#### 3. Listar Todos los Planes
```http
GET /api/admin/planes
Authorization: Bearer {token}
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "tipo": "GRATUITO",
    "nombre": "Plan Gratuito",
    "precio": 0.00,
    ...
  },
  {
    "id": 2,
    "tipo": "BASICO",
    "nombre": "Plan Básico",
    "precio": 29.99,
    ...
  },
  {
    "id": 3,
    "tipo": "PREMIUM",
    "nombre": "Plan Premium",
    "precio": 99.99,
    ...
  }
]
```

#### 4. Listar Planes con Paginación
```http
GET /api/admin/planes/paginados?page=0&size=10&sort=nombre,asc
Authorization: Bearer {token}
```

**Respuesta:**
```json
{
  "content": [...],
  "pageable": {...},
  "totalElements": 3,
  "totalPages": 1,
  "last": true,
  "size": 10,
  "number": 0
}
```

#### 5. Actualizar Plan
```http
PUT /api/admin/planes/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "nombre": "Plan Básico Actualizado",
  "precio": 34.99,
  "limiteUsuarios": 3
}
```

#### 6. Cambiar Estado del Plan
```http
PATCH /api/admin/planes/{id}/estado?activo=false
Authorization: Bearer {token}
```

#### 7. Eliminar Plan (Soft Delete)
```http
DELETE /api/admin/planes/{id}
Authorization: Bearer {token}
```

**Respuesta exitosa (204 No Content)**

---

### Endpoints para OWNER (Consulta de Planes)

#### 1. Listar Planes Activos
```http
GET /api/owner/planes
Authorization: Bearer {token}
```

**Respuesta:**
```json
[
  {
    "id": 1,
    "tipo": "GRATUITO",
    "nombre": "Plan Gratuito",
    "descripcion": "Plan básico gratuito con funcionalidades limitadas",
    "precio": 0.00,
    "duracionMeses": 1,
    "limiteUsuarios": 1,
    "limiteReservasMes": 10,
    "limiteServicios": 3,
    "limitePromociones": 0,
    "activo": true
  },
  ...
]
```

#### 2. Obtener Plan por ID
```http
GET /api/owner/planes/{id}
Authorization: Bearer {token}
```

#### 3. Obtener Plan por Tipo
```http
GET /api/owner/planes/tipo/BASICO
Authorization: Bearer {token}
```

**Respuesta:**
```json
{
  "id": 2,
  "tipo": "BASICO",
  "nombre": "Plan Básico",
  "descripcion": "Hasta 2 usuarios y 50 reservas/mes",
  "precio": 29.99,
  "duracionMeses": 1,
  "limiteUsuarios": 2,
  "limiteReservasMes": 50,
  "limiteServicios": 10,
  "limitePromociones": 2,
  "activo": true
}
```

## 🔒 Validación de Límites

El servicio `PlanLimitService` proporciona métodos para validar si una empresa puede realizar ciertas acciones según su plan:

### Ejemplo de Uso en Servicios

```java
@Service
@RequiredArgsConstructor
public class UsuarioService {
    
    private final PlanLimitService planLimitService;
    private final EmpresaRepository empresaRepository;
    
    public Usuario crearUsuario(Long empresaId, CrearUsuarioRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
            .orElseThrow(() -> new EmpresaNotFoundException("Empresa no encontrada"));
        
        // Contar usuarios actuales
        int cantidadActual = empresa.getUsuarios().size();
        
        // Validar límite del plan
        planLimitService.validarLimiteUsuarios(empresa, cantidadActual);
        
        // Crear usuario si la validación pasa
        Usuario nuevoUsuario = new Usuario();
        // ... resto de la lógica
        
        return usuarioRepository.save(nuevoUsuario);
    }
}
```

### Métodos de Validación Disponibles

1. **`validarLimiteUsuarios(Empresa empresa, int cantidadActual)`**
   - Valida si se puede agregar un nuevo usuario

2. **`validarLimiteReservas(Empresa empresa, int reservasEsteMes)`**
   - Valida si se puede crear una nueva reserva en el mes

3. **`validarLimiteServicios(Empresa empresa, int cantidadActual)`**
   - Valida si se puede crear un nuevo servicio

4. **`validarLimitePromociones(Empresa empresa, int cantidadActual)`**
   - Valida si se puede crear una nueva promoción

5. **`obtenerLimitesInfo(Empresa empresa)`**
   - Obtiene información detallada de los límites del plan de la empresa

### Manejo de Excepciones

Cuando se excede un límite, se lanza `PlanLimitExceededException`:

```java
try {
    planLimitService.validarLimiteUsuarios(empresa, cantidadActual);
} catch (PlanLimitExceededException e) {
    // e.getRecurso() -> "usuarios"
    // e.getLimiteActual() -> 2
    // e.getLimitePermitido() -> 2
    // e.getMessage() -> "Límite del plan excedido para usuarios. Actual: 2, Permitido: 2"
    
    return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
        .body(new ErrorResponse("Límite de plan excedido", e.getMessage()));
}
```

## 💾 Modelo de Datos

### Tabla `planes`

```sql
CREATE TABLE planes (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    precio DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    duracion_meses INTEGER NOT NULL DEFAULT 1,
    limite_usuarios INTEGER,                -- NULL = ilimitado
    limite_reservas_mes INTEGER,            -- NULL = ilimitado
    limite_servicios INTEGER,               -- NULL = ilimitado
    limite_promociones INTEGER,             -- NULL = ilimitado
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Relación con Empresas

```sql
ALTER TABLE empresas ADD COLUMN plan_id BIGINT;
ALTER TABLE empresas ADD CONSTRAINT fk_empresas_plan FOREIGN KEY (plan_id) REFERENCES planes(id);
```

## 🚀 Instalación y Configuración

### 1. Ejecutar Migración SQL

```bash
psql -U tu_usuario -d tu_base_de_datos -f init/migration-planes.sql
```

O desde tu cliente PostgreSQL:

```sql
\i /ruta/a/init/migration-planes.sql
```

### 2. Compilar el Proyecto

```bash
mvn clean compile
```

### 3. Verificar Configuración

Asegúrate de que `application.properties` tenga la configuración correcta de la base de datos:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tu_base_de_datos
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.jpa.hibernate.ddl-auto=update
```

## 🧪 Ejemplos de Prueba

### Crear Plan con curl

```bash
# Login como ADMIN
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "admin123"
  }' | jq -r '.token')

# Crear plan
curl -X POST http://localhost:8080/api/admin/planes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "BASICO",
    "nombre": "Plan Básico",
    "descripcion": "Hasta 2 usuarios y 50 reservas/mes",
    "precio": 29.99,
    "duracionMeses": 1,
    "limiteUsuarios": 2,
    "limiteReservasMes": 50,
    "limiteServicios": 10,
    "limitePromociones": 2,
    "activo": true
  }'
```

### Consultar Planes como Owner

```bash
# Login como OWNER
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "owner@barberia.com",
    "password": "owner123"
  }' | jq -r '.token')

# Listar planes activos
curl -X GET http://localhost:8080/api/owner/planes \
  -H "Authorization: Bearer $TOKEN"

# Obtener plan por tipo
curl -X GET http://localhost:8080/api/owner/planes/tipo/PREMIUM \
  -H "Authorization: Bearer $TOKEN"
```

## 📝 Notas Importantes

1. **Límites Nulos = Ilimitado**: Si un límite es `null`, significa que es ilimitado.
2. **Soft Delete**: Los planes eliminados no se borran físicamente, solo se marcan como `deleted=true`.
3. **Tipo Único**: Cada plan debe tener un tipo único (GRATUITO, BASICO, PREMIUM).
4. **Validación Previa**: Siempre llama a `PlanLimitService` antes de crear recursos limitados.
5. **Plan por Defecto**: Al crear una empresa, asigna automáticamente el plan GRATUITO.
6. **Solo para OWNER**: Los planes son exclusivos para dueños de barberías (OWNER), los clientes (CLIENT) no tienen acceso a esta funcionalidad.

## 🔄 Integración con Otros Módulos

### Módulo de Usuarios
```java
// Antes de crear un usuario
planLimitService.validarLimiteUsuarios(empresa, empresa.getUsuarios().size());
```

### Módulo de Reservas
```java
// Antes de crear una reserva
int reservasEsteMes = reservaRepository.countByEmpresaAndMesActual(empresaId);
planLimitService.validarLimiteReservas(empresa, reservasEsteMes);
```

### Módulo de Servicios
```java
// Antes de crear un servicio
int serviciosActuales = servicioRepository.countByEmpresaAndDeletedFalse(empresaId);
planLimitService.validarLimiteServicios(empresa, serviciosActuales);
```

### Módulo de Promociones
```java
// Antes de crear una promoción
int promocionesActivas = promocionRepository.countByEmpresaAndActivaTrue(empresaId);
planLimitService.validarLimitePromociones(empresa, promocionesActivas);
```

## 🎨 Respuestas de Error

```json
{
  "timestamp": "2025-11-09T10:30:00",
  "status": 402,
  "error": "Payment Required",
  "message": "Límite del plan excedido para usuarios. Actual: 2, Permitido: 2",
  "path": "/api/owner/usuarios"
}
```

## 📚 Referencias

- **Entity**: `apex.code.clipperBarberShop.Entities.Plan`
- **Enum**: `apex.code.clipperBarberShop.Entities.enums.TipoPlan`
- **Services**: `plan.application.service.*`
- **Controllers**: `plan.adapters.in.web.*`
- **Migration**: `init/migration-planes.sql`

---

**Fecha de Creación**: 2025-11-09  
**Versión**: 1.0.0  
**Autor**: Sistema Clipper Barber Shop
