# 📝 Módulo de Reseñas - Quick Start

## 🚀 Inicio Rápido

### 1. Cliente crea una reseña

**Prerrequisitos:**
- Tener una reserva completada
- Estar autenticado como CLIENT

```bash
POST /api/client/resenias
Authorization: Bearer <client-token>
Content-Type: application/json

{
  "reservaId": 123,
  "calificacionServicio": 5,
  "calificacionEmpleado": 4,
  "comentario": "Excelente servicio"
}
```

### 2. Owner consulta estadísticas

```bash
GET /api/owner/resenias/estadisticas/empresa
Authorization: Bearer <owner-token>
```

**Respuesta:**
```json
{
  "totalResenias": 150,
  "promedioCalificacion": 4.6,
  "resenias5Estrellas": 90,
  "resenias4Estrellas": 40,
  "resenias3Estrellas": 15
}
```

### 3. Público consulta reseñas (sin auth)

```bash
GET /api/public/empresas/1/resenias
```

## 📚 Estructura del Módulo

```
resenia/
├── adapters/in/web/          # Controladores REST (Client, Owner, Public)
├── adapters/out/             # Adaptadores JPA
├── application/dto/          # DTOs (Request/Response)
├── application/service/      # Lógica de negocio
└── domain/                   # Excepciones y puertos
```

## 🔑 Roles y Permisos

| Endpoint | Rol | Descripción |
|----------|-----|-------------|
| `POST /api/client/resenias` | CLIENT | Crear reseña |
| `PUT /api/client/resenias/{id}` | CLIENT | Actualizar reseña propia |
| `GET /api/owner/resenias` | OWNER | Ver todas las reseñas |
| `GET /api/owner/resenias/estadisticas` | OWNER | Ver estadísticas |
| `GET /api/public/empresas/{id}/resenias` | PUBLIC | Ver reseñas públicas |

## ✅ Validaciones

### Crear/Actualizar Reseña

- ✅ `calificacionServicio`: **OBLIGATORIO**, 1-5 estrellas
- ✅ `calificacionEmpleado`: OPCIONAL, 1-5 estrellas
- ✅ `comentario`: OPCIONAL, máx 1000 caracteres
- ✅ Reserva debe estar en estado `COMPLETED`
- ✅ No puede existir otra reseña para la misma reserva

## 🛠️ Endpoints Principales

### Cliente (CLIENT)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/client/resenias` | Crear reseña |
| PUT | `/api/client/resenias/{id}` | Actualizar reseña |
| GET | `/api/client/resenias` | Listar mis reseñas |
| GET | `/api/client/resenias/{id}` | Ver una reseña |
| GET | `/api/client/resenias/reserva/{reservaId}` | Ver reseña por reserva |
| DELETE | `/api/client/resenias/{id}` | Eliminar reseña |

### Owner (OWNER)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/owner/resenias` | Listar todas las reseñas |
| GET | `/api/owner/resenias/paginadas` | Listar paginadas |
| GET | `/api/owner/resenias/{id}` | Ver una reseña |
| GET | `/api/owner/resenias/empleado/{empleadoId}` | Reseñas por empleado |
| GET | `/api/owner/resenias/estadisticas/empresa` | Estadísticas empresa |
| GET | `/api/owner/resenias/estadisticas/empleado/{id}` | Estadísticas empleado |
| DELETE | `/api/owner/resenias/{id}` | Eliminar reseña |

### Público (PUBLIC)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/public/empresas/{empresaId}/resenias` | Ver reseñas de empresa |
| GET | `/api/public/empresas/{empresaId}/resenias/paginadas` | Reseñas paginadas |
| GET | `/api/public/empresas/{empresaId}/resenias/estadisticas` | Estadísticas públicas |
| GET | `/api/public/empresas/empleados/{empleadoId}/resenias` | Reseñas por empleado |

## 📊 DTOs

### CrearReseniaRequest
```java
{
  "reservaId": Long,
  "calificacionServicio": Integer (1-5),
  "calificacionEmpleado": Integer (1-5),
  "comentario": String (max 1000)
}
```

### ReseniaResponse
```java
{
  "id": Long,
  "reservaId": Long,
  "empresaId": Long,
  "empresaNombre": String,
  "clienteId": String,
  "empleadoId": String,
  "servicioId": Long,
  "servicioNombre": String,
  "calificacionServicio": Integer,
  "calificacionEmpleado": Integer,
  "comentario": String,
  "fecha": LocalDateTime,
  "reservationDate": LocalDateTime
}
```

### EstadisticasEmpresaResponse
```java
{
  "empresaId": Long,
  "empresaNombre": String,
  "totalResenias": Long,
  "promedioCalificacion": Double,
  "resenias5Estrellas": Long,
  "resenias4Estrellas": Long,
  "resenias3Estrellas": Long,
  "resenias2Estrellas": Long,
  "resenias1Estrella": Long,
  "distribucionCalificaciones": Map<Integer, Long>
}
```

## 🚨 Errores Comunes

### 400 Bad Request - Validación
```json
{
  "success": false,
  "message": "La calificación debe ser al menos 1"
}
```

### 403 Forbidden - Sin permiso
```json
{
  "success": false,
  "message": "No tienes permiso para acceder a esta reseña"
}
```

### 404 Not Found - No encontrada
```json
{
  "success": false,
  "message": "Reseña con ID 123 no encontrada"
}
```

### 409 Conflict - Ya existe
```json
{
  "success": false,
  "message": "Ya existe una reseña para esta reserva"
}
```

## 🧪 Testing con cURL

### Crear reseña (CLIENT)
```bash
curl -X POST http://localhost:8080/api/client/resenias \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{
    "reservaId": 123,
    "calificacionServicio": 5,
    "calificacionEmpleado": 4,
    "comentario": "Excelente servicio"
  }'
```

### Ver estadísticas (OWNER)
```bash
curl -X GET http://localhost:8080/api/owner/resenias/estadisticas/empresa \
  -H "Authorization: Bearer eyJhbGc..."
```

### Ver reseñas públicas (Sin auth)
```bash
curl -X GET http://localhost:8080/api/public/empresas/1/resenias
```

### Ver reseñas paginadas (Sin auth)
```bash
curl -X GET "http://localhost:8080/api/public/empresas/1/resenias/paginadas?page=0&size=10"
```

## 🔄 Flujo Típico

1. **Cliente completa reserva** → Estado: `COMPLETED`
2. **Cliente crea reseña** → POST `/api/client/resenias`
3. **Sistema valida**:
   - ✅ Reserva completada
   - ✅ No existe otra reseña
   - ✅ Cliente es dueño de la reserva
4. **Reseña creada** → Status 201
5. **Owner puede ver** → GET `/api/owner/resenias`
6. **Público puede ver** → GET `/api/public/empresas/1/resenias`

## 💡 Consejos

### Para Developers

1. **Usar paginación**: Para empresas con muchas reseñas
   ```
   GET /api/owner/resenias/paginadas?page=0&size=20
   ```

2. **Soft delete**: Las reseñas eliminadas se marcan como `deleted=true`, no se eliminan físicamente

3. **Estadísticas**: El promedio considera ambas calificaciones (servicio + empleado) / 2

4. **Consultas públicas**: No requieren autenticación, ideales para mostrar en landing page

### Para Frontend

1. **Mostrar estrellas**: Usar `calificacionServicio` y `calificacionEmpleado`
2. **Validar antes de enviar**: Verificar 1-5 estrellas
3. **Deshabilitar botón**: Si ya existe reseña para esa reserva
4. **Mostrar solo si COMPLETED**: Verificar estado de reserva

## 📝 Documentación Completa

Para más detalles, consulta `MODULO-RESENIAS.md`

## 🎯 Próximos Pasos

- [ ] Integrar WebSocket para notificaciones en tiempo real
- [ ] Agregar respuestas del owner a las reseñas
- [ ] Implementar sistema de reportes
- [ ] Agregar filtros avanzados (por fecha, calificación, etc.)
- [ ] Implementar cache para estadísticas

## 📞 Soporte

Para dudas o problemas, revisa:
- `MODULO-RESENIAS.md` - Documentación completa
- `/src/main/java/apex/code/clipperBarberShop/resenia/` - Código fuente
- Excepciones personalizadas en `resenia/domain/exception/`
