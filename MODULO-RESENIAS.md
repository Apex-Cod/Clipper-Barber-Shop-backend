# Módulo de Reseñas (Reviews)

## Descripción General
Sistema completo de reseñas para barbería que permite a los clientes calificar servicios y empleados después de completar una reserva. Los owners pueden visualizar estadísticas detalladas y gestionar las reseñas.

## Características Principales

### ✅ Para Clientes
- ✨ Crear reseña después de una reserva completada
- 📝 Calificar servicio y empleado (1-5 estrellas cada uno)
- 💬 Agregar comentarios opcionales
- ✏️ Editar sus propias reseñas
- 🗑️ Eliminar sus propias reseñas
- 📋 Listar todas sus reseñas
- 🔍 Consultar reseña de una reserva específica

### ✅ Para Owners
- 📊 Ver todas las reseñas de la empresa
- 📈 Estadísticas completas (distribución 1-5 estrellas)
- 👥 Estadísticas por empleado
- 📄 Listado paginado de reseñas
- 🗑️ Eliminar reseñas inapropiadas

### ✅ Para Público
- 🌐 Ver reseñas de cualquier empresa (sin autenticación)
- 📊 Ver estadísticas públicas de empresas
- 👤 Ver reseñas de empleados específicos

## Arquitectura Hexagonal

```
resenia/
├── adapters/
│   ├── in/
│   │   └── web/
│   │       ├── ReseniaClientController.java    # REST endpoints cliente
│   │       ├── ReseniaOwnerController.java     # REST endpoints owner
│   │       └── ReseniaPublicController.java    # REST endpoints público
│   └── out/
│       ├── ReseniaRepository.java             # JPA Repository
│       └── ReseniaRepositoryAdapter.java      # Adapter del repo
│
├── application/
│   ├── dto/
│   │   ├── CrearReseniaRequest.java           # DTO para crear
│   │   ├── ActualizarReseniaRequest.java      # DTO para actualizar
│   │   ├── ReseniaResponse.java               # DTO de respuesta
│   │   ├── EstadisticasEmpresaResponse.java   # Estadísticas empresa
│   │   └── EstadisticasEmpleadoResponse.java  # Estadísticas empleado
│   └── service/
│       ├── ReseniaClientService.java          # Lógica de negocio cliente
│       ├── ReseniaOwnerService.java           # Lógica de negocio owner
│       └── ReseniaPublicService.java          # Lógica pública
│
└── domain/
    ├── exception/
    │   ├── ReseniaNotFoundException.java
    │   ├── ReseniaAlreadyExistsException.java
    │   ├── ReseniaAccessDeniedException.java
    │   └── ReservaNotCompletedException.java
    └── port/
        └── out/
            └── ReseniaRepositoryPort.java     # Puerto del repositorio
```

## Base de Datos

### Tabla: `resenias`
```sql
CREATE TABLE resenias (
    id BIGSERIAL PRIMARY KEY,
    reserva_id BIGINT NOT NULL UNIQUE REFERENCES reservas(id),
    cliente_id VARCHAR(255) NOT NULL,
    empleado_id VARCHAR(255),
    empresa_id BIGINT NOT NULL REFERENCES empresas(id),
    calificacion_servicio INTEGER NOT NULL CHECK (calificacion_servicio BETWEEN 1 AND 5),
    calificacion_empleado INTEGER CHECK (calificacion_empleado BETWEEN 1 AND 5),
    comentario TEXT,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## API REST

### 🔐 Endpoints Cliente (Requiere `ROLE_CLIENT`)

#### POST /api/client/resenias
Crear una nueva reseña después de completar una reserva.

**Request:**
```json
{
  "reservaId": 123,
  "calificacionServicio": 5,
  "calificacionEmpleado": 4,
  "comentario": "Excelente servicio, muy profesional"
}
```

**Validaciones:**
- `calificacionServicio`: Obligatorio, entre 1-5
- `calificacionEmpleado`: Opcional, entre 1-5
- `comentario`: Opcional, máximo 1000 caracteres
- La reserva debe estar en estado `COMPLETED`
- No puede existir otra reseña para la misma reserva

**Response 201:**
```json
{
  "success": true,
  "message": "Reseña creada exitosamente",
  "data": {
    "id": 456,
    "reservaId": 123,
    "empresaId": 1,
    "empresaNombre": "Clipper BarberShop",
    "clienteId": "client-uuid-123",
    "clienteNombre": null,
    "empleadoId": "employee-uuid-456",
    "empleadoNombre": null,
    "servicioId": 10,
    "servicioNombre": "Corte Clásico",
    "calificacionServicio": 5,
    "calificacionEmpleado": 4,
    "comentario": "Excelente servicio, muy profesional",
    "fecha": "2025-11-03T14:30:00",
    "reservationDate": "2025-11-01T10:00:00"
  }
}
```

#### PUT /api/client/resenias/{id}
Actualizar una reseña propia.

**Request:**
```json
{
  "calificacionServicio": 5,
  "calificacionEmpleado": 5,
  "comentario": "Actualicé mi opinión, todo perfecto"
}
```

#### GET /api/client/resenias
Listar todas las reseñas del cliente autenticado.

#### GET /api/client/resenias/{id}
Obtener una reseña específica propia.

#### GET /api/client/resenias/reserva/{reservaId}
Obtener la reseña de una reserva específica.

#### DELETE /api/client/resenias/{id}
Eliminar una reseña propia (soft delete).

---

### 🔐 Endpoints Owner (Requiere `ROLE_OWNER`)

#### GET /api/owner/resenias
Listar todas las reseñas de la empresa del owner.

#### GET /api/owner/resenias/paginadas?page=0&size=10
Listar reseñas paginadas.

**Response 200:**
```json
{
  "success": true,
  "message": "Reseñas obtenidas",
  "data": {
    "content": [ /* array de reseñas */ ],
    "totalElements": 150,
    "totalPages": 15,
    "number": 0,
    "size": 10
  }
}
```

#### GET /api/owner/resenias/{id}
Ver detalles de una reseña específica.

#### GET /api/owner/resenias/empleado/{empleadoId}
Listar reseñas de un empleado específico.

#### GET /api/owner/resenias/estadisticas/empresa
Obtener estadísticas completas de la empresa.

**Response 200:**
```json
{
  "success": true,
  "message": "Estadísticas obtenidas",
  "data": {
    "empresaId": 1,
    "empresaNombre": "Clipper BarberShop",
    "totalResenias": 150,
    "promedioCalificacion": 4.6,
    "resenias5Estrellas": 90,
    "resenias4Estrellas": 40,
    "resenias3Estrellas": 15,
    "resenias2Estrellas": 3,
    "resenias1Estrella": 2,
    "distribucionCalificaciones": {
      "5": 90,
      "4": 40,
      "3": 15,
      "2": 3,
      "1": 2
    }
  }
}
```

#### GET /api/owner/resenias/estadisticas/empleado/{empleadoId}
Obtener estadísticas de un empleado.

**Response 200:**
```json
{
  "success": true,
  "message": "Estadísticas obtenidas",
  "data": {
    "empleadoId": "employee-uuid-456",
    "empleadoNombre": "Juan Pérez",
    "totalResenias": 45,
    "promedioCalificacion": 4.8
  }
}
```

#### DELETE /api/owner/resenias/{id}
Eliminar una reseña (útil para eliminar reseñas inapropiadas).

---

### 🌐 Endpoints Públicos (Sin autenticación)

#### GET /api/public/empresas/{empresaId}/resenias
Listar todas las reseñas activas de una empresa.

#### GET /api/public/empresas/{empresaId}/resenias/paginadas?page=0&size=10
Listar reseñas paginadas de una empresa.

#### GET /api/public/empresas/{empresaId}/resenias/estadisticas
Obtener estadísticas públicas de una empresa.

#### GET /api/public/empresas/empleados/{empleadoId}/resenias
Listar reseñas de un empleado específico.

## Reglas de Negocio

### 1. Creación de Reseñas
- ✅ Solo se pueden crear reseñas para reservas en estado `COMPLETED`
- ✅ Cada reserva solo puede tener una reseña
- ✅ El cliente debe ser el dueño de la reserva
- ✅ La calificación del servicio es obligatoria (1-5)
- ✅ La calificación del empleado es opcional (1-5)
- ✅ El comentario es opcional (máx 1000 caracteres)

### 2. Actualización de Reseñas
- ✅ Solo el cliente dueño puede actualizar su reseña
- ✅ Se mantienen las mismas validaciones de creación

### 3. Eliminación de Reseñas
- ✅ Clientes pueden eliminar sus propias reseñas
- ✅ Owners pueden eliminar cualquier reseña de su empresa
- ✅ Se usa soft delete (campo `deleted`)

### 4. Visualización
- ✅ Clientes ven solo sus reseñas
- ✅ Owners ven todas las reseñas de su empresa
- ✅ Público puede ver reseñas activas de cualquier empresa

## Manejo de Errores

### Excepciones Personalizadas

#### `ReseniaNotFoundException`
Se lanza cuando no se encuentra una reseña.
```json
{
  "success": false,
  "message": "Reseña con ID 123 no encontrada",
  "data": null
}
```

#### `ReseniaAlreadyExistsException`
Se lanza al intentar crear una segunda reseña para la misma reserva.
```json
{
  "success": false,
  "message": "Ya existe una reseña para esta reserva",
  "data": null
}
```

#### `ReseniaAccessDeniedException`
Se lanza cuando un usuario intenta acceder a una reseña sin permisos.
```json
{
  "success": false,
  "message": "No tienes permiso para acceder a esta reseña",
  "data": null
}
```

#### `ReservaNotCompletedException`
Se lanza al intentar crear una reseña de una reserva no completada.
```json
{
  "success": false,
  "message": "Solo se pueden calificar reservas completadas",
  "data": null
}
```

## Configuración de Seguridad

En `SecurityConfig.java`:

```java
.requestMatchers("/api/public/**").permitAll()  // Endpoints públicos
.requestMatchers("/api/client/**").hasRole("CLIENT")
.requestMatchers("/api/owner/**").hasRole("OWNER")
```

## Validaciones con Bean Validation

### CrearReseniaRequest
```java
@NotNull(message = "El ID de la reserva es obligatorio")
private Long reservaId;

@NotNull(message = "La calificación del servicio es obligatoria")
@Min(value = 1, message = "La calificación debe ser al menos 1")
@Max(value = 5, message = "La calificación debe ser máximo 5")
private Integer calificacionServicio;

@Min(value = 1, message = "La calificación debe ser al menos 1")
@Max(value = 5, message = "La calificación debe ser máximo 5")
private Integer calificacionEmpleado;

@Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
private String comentario;
```

## Ejemplos de Uso

### Flujo Completo

1. **Cliente completa una reserva**
   ```
   PATCH /api/client/reservas/123/completar
   ```

2. **Cliente crea una reseña**
   ```bash
   curl -X POST http://localhost:8080/api/client/resenias \
     -H "Authorization: Bearer <client-token>" \
     -H "Content-Type: application/json" \
     -d '{
       "reservaId": 123,
       "calificacionServicio": 5,
       "calificacionEmpleado": 4,
       "comentario": "Excelente servicio"
     }'
   ```

3. **Owner consulta estadísticas**
   ```bash
   curl -X GET http://localhost:8080/api/owner/resenias/estadisticas/empresa \
     -H "Authorization: Bearer <owner-token>"
   ```

4. **Público consulta reseñas**
   ```bash
   curl -X GET http://localhost:8080/api/public/empresas/1/resenias
   ```

## Mejoras Futuras

- [ ] **WebSocket**: Notificaciones en tiempo real cuando se recibe una nueva reseña
- [ ] **Respuestas del Owner**: Permitir que owners respondan a reseñas
- [ ] **Reportar reseñas**: Sistema para que owners reporten reseñas inapropiadas
- [ ] **Imágenes**: Permitir adjuntar fotos a las reseñas
- [ ] **Filtros avanzados**: Filtrar por calificación, fecha, empleado
- [ ] **Reseñas destacadas**: Marcar reseñas como destacadas
- [ ] **Análisis de sentimiento**: IA para analizar comentarios
- [ ] **Tendencias**: Gráficos de evolución de calificaciones en el tiempo

## Testing

### Casos de Prueba

1. **Creación exitosa de reseña**
   - Reserva completada → Reseña creada ✅

2. **Validación de reserva incompleta**
   - Reserva PENDING → Error 400 ❌

3. **Validación de duplicados**
   - Ya existe reseña → Error 409 ❌

4. **Actualización por otro usuario**
   - Cliente B intenta actualizar reseña de Cliente A → Error 403 ❌

5. **Estadísticas con cero reseñas**
   - Empresa nueva → Estadísticas en cero ✅

6. **Paginación**
   - 150 reseñas → 15 páginas de 10 ✅

## Conclusión

El módulo de reseñas está completamente implementado siguiendo:

- ✅ **Arquitectura hexagonal** (puertos y adaptadores)
- ✅ **Separación de responsabilidades** (3 servicios: Client, Owner, Public)
- ✅ **Validaciones completas** (Bean Validation + lógica de negocio)
- ✅ **Soft delete** (no elimina registros físicamente)
- ✅ **Seguridad por roles** (CLIENT, OWNER, PUBLIC)
- ✅ **DTOs específicos** por operación
- ✅ **Manejo de errores** con excepciones personalizadas
- ✅ **Paginación** para listados grandes
- ✅ **Estadísticas detalladas** para análisis

¡El módulo está listo para usar! 🎉
