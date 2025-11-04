# Módulo de Servicios y Reservas

## 📋 Descripción General

Este módulo implementa la funcionalidad completa de gestión de servicios y reservas para el sistema Clipper Barber Shop, con separación clara de responsabilidades entre roles (OWNER y CLIENT).

## 🏗️ Arquitectura

El módulo sigue una **Arquitectura Hexagonal (Puertos y Adaptadores)** con las siguientes capas:

```
servicio/
├── adapters/
│   ├── in/
│   │   └── web/          # Controladores REST
│   └── out/
│       └── persistence/   # Adaptadores de BD
├── application/
│   ├── dto/              # Data Transfer Objects
│   └── service/          # Lógica de aplicación
└── domain/
    ├── exception/        # Excepciones del dominio
    └── port/
        └── out/          # Interfaces de repositorio

reserva/
├── adapters/
│   ├── in/
│   │   └── web/          # Controladores REST
│   └── out/
│       └── persistence/   # Adaptadores de BD
├── application/
│   ├── dto/              # Data Transfer Objects
│   └── service/          # Lógica de aplicación
└── domain/
    ├── exception/        # Excepciones del dominio
    └── port/
        └── out/          # Interfaces de repositorio
```

---

## 📦 Módulo de Servicios

### Roles y Permisos

#### OWNER
- ✅ Crear servicios
- ✅ Actualizar servicios
- ✅ Obtener detalles de servicios
- ✅ Listar todos los servicios de su empresa
- ✅ Filtrar por categoría
- ✅ Eliminar servicios (soft delete)
- ✅ Restaurar servicios eliminados

#### CLIENT
- ✅ Ver servicios de cualquier empresa
- ✅ Filtrar por empresa y categoría
- ❌ No puede crear, editar o eliminar servicios

### Endpoints - OWNER

**Base URL:** `/api/owner/servicios`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/` | Crear servicio |
| PUT | `/{id}` | Actualizar servicio |
| GET | `/{id}` | Obtener servicio |
| GET | `/` | Listar todos los servicios |
| GET | `/paginados` | Listar servicios paginados |
| GET | `/categoria/{categoria}` | Listar por categoría |
| DELETE | `/{id}` | Eliminar servicio (soft delete) |
| PATCH | `/{id}/restaurar` | Restaurar servicio eliminado |

#### Ejemplo: Crear Servicio
```bash
POST /api/owner/servicios
Authorization: Bearer {token}
Content-Type: application/json

{
  "empresaId": 1,
  "name": "Corte de Cabello",
  "description": "Corte moderno para caballeros",
  "duration": 30,
  "price": 15.00,
  "categoria": "CORTE",
  "publicoObjetivo": "HOMBRES"
}
```

### Endpoints - CLIENT

**Base URL:** `/api/client/servicios`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/{id}` | Obtener servicio |
| GET | `/empresa/{empresaId}` | Listar servicios de una empresa |
| GET | `/empresa/{empresaId}/paginados` | Listar paginados |
| GET | `/empresa/{empresaId}/categoria/{categoria}` | Filtrar por categoría |

#### Ejemplo: Listar Servicios de una Empresa
```bash
GET /api/client/servicios/empresa/1
Authorization: Bearer {token}
```

---

## 📅 Módulo de Reservas

### Roles y Permisos

#### OWNER
- ✅ Crear reservas para cualquier cliente
- ✅ Actualizar cualquier reserva de su empresa
- ✅ Confirmar reservas
- ✅ Completar reservas
- ✅ Cancelar reservas
- ✅ Reprogramar reservas
- ✅ Ver todas las reservas de su empresa
- ✅ Filtrar por estado, empleado, rango de fechas
- ✅ Eliminar reservas (soft delete)

#### CLIENT
- ✅ Crear sus propias reservas
- ✅ Actualizar sus reservas (solo si están PENDING)
- ✅ Cancelar sus reservas
- ✅ Reprogramar sus reservas (solo si están PENDING)
- ✅ Ver sus propias reservas
- ✅ Filtrar por estado
- ✅ Eliminar sus reservas (solo CANCELLED o COMPLETED)

### Estados de Reserva

| Estado | Descripción |
|--------|-------------|
| `PENDING` | Reserva pendiente de confirmación |
| `CONFIRMED` | Reserva confirmada por el owner |
| `COMPLETED` | Servicio completado |
| `CANCELLED` | Reserva cancelada |
| `RESCHEDULED` | Reserva reprogramada |

### Endpoints - OWNER

**Base URL:** `/api/owner/reservas`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/cliente/{clientId}` | Crear reserva para un cliente |
| PUT | `/{id}` | Actualizar reserva |
| PATCH | `/{id}/confirmar` | Confirmar reserva |
| PATCH | `/{id}/completar` | Completar reserva |
| PATCH | `/{id}/cancelar` | Cancelar reserva |
| PATCH | `/{id}/reprogramar` | Reprogramar reserva |
| GET | `/{id}` | Obtener detalles de reserva |
| GET | `/` | Listar todas las reservas |
| GET | `/paginadas` | Listar reservas paginadas |
| GET | `/estado/{status}` | Filtrar por estado |
| GET | `/empleado/{employeeId}` | Filtrar por empleado |
| GET | `/rango?inicio={fecha}&fin={fecha}` | Filtrar por rango de fechas |
| DELETE | `/{id}` | Eliminar reserva |

#### Ejemplo: Crear Reserva
```bash
POST /api/owner/reservas/cliente/uuid-del-cliente
Authorization: Bearer {token}
Content-Type: application/json

{
  "serviceId": 1,
  "reservationDate": "2025-11-01T10:00:00",
  "employeeId": "uuid-del-empleado",
  "promocionId": null,
  "notas": "Cliente prefiere corte bajo"
}
```

#### Ejemplo: Confirmar Reserva
```bash
PATCH /api/owner/reservas/123/confirmar
Authorization: Bearer {token}
```

### Endpoints - CLIENT

**Base URL:** `/api/client/reservas`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/` | Crear reserva |
| PUT | `/{id}` | Actualizar reserva (solo PENDING) |
| PATCH | `/{id}/cancelar` | Cancelar reserva |
| PATCH | `/{id}/reprogramar` | Reprogramar reserva (solo PENDING) |
| GET | `/{id}` | Obtener detalles de reserva |
| GET | `/` | Listar mis reservas |
| GET | `/paginadas` | Listar mis reservas paginadas |
| GET | `/estado/{status}` | Filtrar mis reservas por estado |
| DELETE | `/{id}` | Eliminar reserva (solo CANCELLED o COMPLETED) |

#### Ejemplo: Crear Reserva (Cliente)
```bash
POST /api/client/reservas
Authorization: Bearer {token}
Content-Type: application/json

{
  "serviceId": 1,
  "reservationDate": "2025-11-01T15:30:00",
  "employeeId": "uuid-del-empleado",
  "notas": "Primera vez en este lugar"
}
```

#### Ejemplo: Reprogramar Reserva
```bash
PATCH /api/client/reservas/123/reprogramar
Authorization: Bearer {token}
Content-Type: application/json

{
  "nuevaFecha": "2025-11-02T15:30:00",
  "employeeId": "uuid-del-empleado",
  "motivo": "Surgió un imprevisto"
}
```

---

## 🔒 Validaciones Implementadas

### Validaciones de Reserva

1. **Fecha Futura**: La fecha de la reserva debe ser posterior a la fecha actual
2. **Horario Comercial**: Las reservas deben estar entre 8:00 AM y 8:00 PM
3. **Disponibilidad del Empleado**: No se permiten reservas superpuestas para el mismo empleado
4. **Empleado de la Empresa**: El empleado debe pertenecer a la empresa y estar activo
5. **Servicio Activo**: El servicio no debe estar eliminado
6. **Estados Válidos**: 
   - Clientes solo pueden actualizar/reprogramar reservas PENDING
   - No se pueden cancelar reservas completadas
   - No se pueden actualizar reservas canceladas

### Validaciones de Servicio

1. **Duración**: Entre 5 y 480 minutos (8 horas)
2. **Precio**: Mayor a 0 y menor a 999,999.99
3. **Nombre**: Entre 2 y 100 caracteres
4. **Descripción**: Máximo 500 caracteres
5. **Categoría**: Máximo 50 caracteres
6. **Acceso**: Solo el owner de la empresa puede modificar sus servicios

---

## 🗄️ Borrado Lógico (Soft Delete)

Tanto servicios como reservas implementan borrado lógico:

- Campo `deleted`: Boolean que indica si está eliminado
- Campo `deleted_at`: Timestamp de eliminación
- Campo `deleted_by`: ID del usuario que eliminó

**Características:**
- Los registros nunca se eliminan físicamente de la base de datos
- Las consultas filtran automáticamente registros eliminados (`deleted = false`)
- Los owners pueden restaurar servicios eliminados
- Las reservas eliminadas no aparecen en las consultas normales

---

## 🎯 Casos de Uso Principales

### Flujo de Reserva - Cliente

1. Cliente busca servicios de una empresa
2. Cliente selecciona un servicio
3. Cliente elige fecha, hora y empleado disponible
4. Sistema valida disponibilidad y horarios
5. Se crea la reserva en estado PENDING
6. Owner confirma la reserva (estado CONFIRMED)
7. Servicio se completa (estado COMPLETED)

### Flujo de Gestión - Owner

1. Owner crea servicios para su empresa
2. Owner recibe solicitudes de reserva
3. Owner confirma o rechaza reservas
4. Owner puede reprogramar o cancelar si es necesario
5. Owner marca reservas como completadas
6. Owner puede ver historial y estadísticas

---

## 🔧 Tecnologías Utilizadas

- **Spring Boot 3.x**
- **Spring Security** (autenticación basada en roles)
- **Spring Data JPA** (persistencia)
- **Hibernate** (ORM)
- **Lombok** (reducción de código boilerplate)
- **Jakarta Validation** (validaciones)
- **PostgreSQL** (base de datos)

---

## 📊 Respuestas de API

Todas las respuestas siguen el formato estándar:

### Respuesta Exitosa
```json
{
  "status": "success",
  "mensaje": "Operación exitosa",
  "data": { ... }
}
```

### Respuesta de Error
```json
{
  "status": "error",
  "mensaje": "Descripción del error",
  "data": null
}
```

---

## ⚠️ Manejo de Errores

| Excepción | Código HTTP | Descripción |
|-----------|-------------|-------------|
| `ServicioNotFoundException` | 404 | Servicio no encontrado |
| `ServicioAccessDeniedException` | 403 | Sin permisos para acceder al servicio |
| `ReservaNotFoundException` | 404 | Reserva no encontrada |
| `ReservaAccessDeniedException` | 403 | Sin permisos para acceder a la reserva |
| `ConflictoReservaException` | 409 | Conflicto de horarios |
| `ReservaInvalidStateException` | 400 | Estado de reserva inválido |
| `IllegalArgumentException` | 400 | Argumentos inválidos |
| `MethodArgumentNotValidException` | 400 | Error de validación |

---

## 🚀 Próximas Mejoras

- [ ] Integración con sistema de promociones
- [ ] Notificaciones por email/SMS
- [ ] Sistema de recordatorios
- [ ] Reportes y estadísticas avanzadas
- [ ] Calendario visual de disponibilidad
- [ ] Sistema de calificaciones y reseñas
- [ ] Gestión de listas de espera

---

## 📝 Notas Importantes

1. **Seguridad**: Todos los endpoints están protegidos con JWT y validación de roles
2. **Paginación**: Los endpoints paginados soportan parámetros estándar de Spring Data (page, size, sort)
3. **Formato de Fechas**: Usar ISO 8601 (yyyy-MM-dd'T'HH:mm:ss)
4. **IDs de Usuario**: Los IDs de usuarios (client, employee) son UUIDs en formato String
5. **Borrado Lógico**: Los registros eliminados pueden ser restaurados por administradores

---

## 🧪 Testing

Para probar los endpoints:

1. Registrar una empresa y owner
2. Crear servicios como owner
3. Registrar un cliente
4. Crear reservas como cliente
5. Gestionar reservas como owner

Ver `test-email-verification.sh` para ejemplos de pruebas con curl.

---

## 👥 Autores

Proyecto desarrollado para **Clipper Barber Shop**
