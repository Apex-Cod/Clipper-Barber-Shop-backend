# 🎁 Módulo de Promociones

## 📋 Descripción General

El módulo de promociones permite a los usuarios OWNER gestionar descuentos y ofertas especiales para sus servicios. Las promociones pueden ser de tipo porcentaje o monto fijo, con opciones avanzadas como límites de uso, fechas de vigencia, y montos mínimos.

## 🏗️ Arquitectura

Sigue arquitectura hexagonal (Ports & Adapters):

```
promocion/
├── adapters/
│   ├── in/web/
│   │   └── PromocionOwnerController.java  # Controlador REST
│   └── out/persistence/
│       ├── JpaPromocionRepository.java     # JPA Repository
│       └── PromocionRepositoryAdapter.java # Adaptador
├── application/
│   ├── dto/
│   │   ├── PromocionRequest.java           # DTO para crear
│   │   ├── ActualizarPromocionRequest.java # DTO para actualizar
│   │   └── PromocionResponse.java          # DTO de respuesta
│   └── service/
│       └── PromocionOwnerService.java      # Lógica de negocio
└── domain/
    ├── exception/
    │   ├── PromocionNotFoundException.java
    │   ├── PromocionExpiredException.java
    │   └── PromocionAccessDeniedException.java
    └── port/out/
        └── PromocionRepositoryPort.java    # Puerto de repositorio
```

## 📊 Modelo de Datos

### Entidad Promocion

```java
@Entity
@Table(name = "promociones")
public class Promocion extends SoftDeletableEntity {
    private Long id;
    private Empresa empresa;           // Empresa dueña de la promoción
    private String nombre;             // Nombre de la promoción
    private String descripcion;        // Descripción detallada
    private TipoDescuento tipoDescuento; // PORCENTAJE o MONTO_FIJO
    private Double valorDescuento;     // Valor del descuento
    private LocalDate fechaInicio;     // Fecha de inicio
    private LocalDate fechaFin;        // Fecha de finalización
    private Boolean activa;            // Estado activo/inactivo
    private Integer limiteUsos;        // Límite de usos (null = ilimitado)
    private Integer usosActuales;      // Contador de usos
    private Double montoMinimo;        // Monto mínimo para aplicar
    private Servicio servicio;         // Servicio específico (null = todos)
}
```

### Enum TipoDescuento

```java
public enum TipoDescuento {
    PORCENTAJE,  // Descuento en porcentaje (ej: 20% off)
    MONTO_FIJO   // Descuento en monto fijo (ej: $5 off)
}
```

## 🔒 Seguridad y Permisos

**Solo usuarios con rol OWNER pueden:**
- ✅ Crear promociones
- ✅ Actualizar promociones
- ✅ Activar/desactivar promociones
- ✅ Eliminar promociones (soft delete)
- ✅ Listar promociones de su empresa

**Validaciones de acceso:**
- Las promociones solo pueden ser gestionadas por el OWNER de la empresa
- No se puede acceder a promociones de otras empresas

## 📡 API Endpoints

### Base URL: `/api/owner/promociones`

#### 1. Crear Promoción

```http
POST /api/owner/promociones
Authorization: Bearer {token}
Content-Type: application/json

{
  "nombre": "Descuento de Bienvenida",
  "descripcion": "20% de descuento en tu primer corte",
  "tipoDescuento": "PORCENTAJE",
  "valorDescuento": 20.0,
  "fechaInicio": "2025-11-01",
  "fechaFin": "2025-12-31",
  "limiteUsos": 100,
  "montoMinimo": 10.0,
  "servicioId": null  // null = aplica a todos los servicios
}
```

**Respuesta 201 Created:**
```json
{
  "status": "success",
  "mensaje": "Promoción creada exitosamente",
  "data": {
    "id": 1,
    "empresaId": 1,
    "empresaNombre": "Barber Shop Premium",
    "nombre": "Descuento de Bienvenida",
    "descripcion": "20% de descuento en tu primer corte",
    "tipoDescuento": "PORCENTAJE",
    "valorDescuento": 20.0,
    "fechaInicio": "2025-11-01",
    "fechaFin": "2025-12-31",
    "activa": true,
    "limiteUsos": 100,
    "usosActuales": 0,
    "montoMinimo": 10.0,
    "servicioId": null,
    "servicioNombre": null,
    "vigente": true
  }
}
```

#### 2. Actualizar Promoción

```http
PUT /api/owner/promociones/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "nombre": "Descuento Actualizado",
  "valorDescuento": 25.0,
  "activa": true
}
```

**Respuesta 200 OK:**
```json
{
  "status": "success",
  "mensaje": "Promoción actualizada exitosamente",
  "data": { ... }
}
```

#### 3. Obtener Promoción por ID

```http
GET /api/owner/promociones/{id}
Authorization: Bearer {token}
```

**Respuesta 200 OK:**
```json
{
  "status": "success",
  "mensaje": "Promoción obtenida exitosamente",
  "data": { ... }
}
```

#### 4. Listar Todas las Promociones

```http
GET /api/owner/promociones
Authorization: Bearer {token}
```

**Respuesta 200 OK:**
```json
{
  "status": "success",
  "mensaje": "Promociones obtenidas exitosamente",
  "data": [
    { ... },
    { ... }
  ]
}
```

#### 5. Listar con Paginación

```http
GET /api/owner/promociones/paginadas?page=0&size=10&sort=nombre,asc
Authorization: Bearer {token}
```

**Respuesta 200 OK:**
```json
{
  "status": "success",
  "mensaje": "Promociones paginadas obtenidas exitosamente",
  "data": {
    "content": [ ... ],
    "pageable": { ... },
    "totalPages": 5,
    "totalElements": 50,
    "size": 10,
    "number": 0
  }
}
```

#### 6. Listar Solo Promociones Activas

```http
GET /api/owner/promociones/activas
Authorization: Bearer {token}
```

#### 7. Activar Promoción

```http
PATCH /api/owner/promociones/{id}/activar
Authorization: Bearer {token}
```

**Respuesta 200 OK:**
```json
{
  "status": "success",
  "mensaje": "Promoción activada exitosamente",
  "data": { ... }
}
```

#### 8. Desactivar Promoción

```http
PATCH /api/owner/promociones/{id}/desactivar
Authorization: Bearer {token}
```

**Respuesta 200 OK:**
```json
{
  "status": "success",
  "mensaje": "Promoción desactivada exitosamente",
  "data": { ... }
}
```

#### 9. Eliminar Promoción (Soft Delete)

```http
DELETE /api/owner/promociones/{id}
Authorization: Bearer {token}
```

**Respuesta 200 OK:**
```json
{
  "status": "success",
  "mensaje": "Promoción eliminada exitosamente",
  "data": null
}
```

## 🔧 Validaciones

### Validaciones del Request

**PromocionRequest:**
- ✅ `nombre`: Obligatorio, máximo 100 caracteres
- ✅ `descripcion`: Opcional, máximo 500 caracteres
- ✅ `tipoDescuento`: Obligatorio (PORCENTAJE o MONTO_FIJO)
- ✅ `valorDescuento`: Obligatorio, mayor a 0
  - Si PORCENTAJE: debe estar entre 0 y 100
- ✅ `fechaInicio`: Obligatoria, debe ser hoy o posterior
- ✅ `fechaFin`: Obligatoria, debe ser posterior a hoy
- ✅ `fechaFin` debe ser posterior a `fechaInicio`
- ✅ `limiteUsos`: Opcional, mínimo 1 si se especifica
- ✅ `montoMinimo`: Opcional, mayor a 0 si se especifica
- ✅ `servicioId`: Opcional, debe existir y pertenecer a la empresa

### Validaciones de Negocio

**Al crear/actualizar:**
- ✅ El usuario debe ser OWNER
- ✅ El usuario debe tener una empresa asociada
- ✅ Si se especifica `servicioId`, el servicio debe pertenecer a la empresa
- ✅ Las fechas deben ser coherentes

**Al aplicar en reservas:**
- ✅ La promoción debe estar vigente (activa y dentro de fechas)
- ✅ No debe haber alcanzado el límite de usos
- ✅ El monto de la reserva debe cumplir el monto mínimo
- ✅ La promoción debe aplicar al servicio seleccionado

## 💰 Integración con Reservas

### Cálculo de Precio con Promoción

El módulo de reservas utiliza la promoción automáticamente:

```java
// En ReservaValidationService
Double precioFinal = calcularPrecioFinal(servicio, promocionId);
```

**Lógica de cálculo:**

```java
// Descuento por PORCENTAJE
descuento = precioBase * (valorDescuento / 100)
precioFinal = precioBase - descuento

// Descuento MONTO_FIJO
descuento = min(valorDescuento, precioBase)
precioFinal = precioBase - descuento
```

**Ejemplos:**

1. **Porcentaje:**
   - Precio base: $50.00
   - Descuento: 20%
   - Cálculo: $50.00 - ($50.00 * 0.20) = $40.00

2. **Monto Fijo:**
   - Precio base: $50.00
   - Descuento: $10.00
   - Cálculo: $50.00 - $10.00 = $40.00

3. **Monto Fijo Mayor al Precio:**
   - Precio base: $8.00
   - Descuento: $10.00
   - Cálculo: $8.00 - min($10.00, $8.00) = $0.00

### Registro de Uso

Después de confirmar una reserva, se incrementa el contador:

```java
validationService.registrarUsoPromocion(promocionId);
```

## 🎯 Casos de Uso

### Caso 1: Promoción General

```json
{
  "nombre": "Black Friday",
  "descripcion": "50% en todos los servicios",
  "tipoDescuento": "PORCENTAJE",
  "valorDescuento": 50.0,
  "fechaInicio": "2025-11-25",
  "fechaFin": "2025-11-25",
  "servicioId": null  // Aplica a todos
}
```

### Caso 2: Promoción Específica

```json
{
  "nombre": "Corte Clásico en Oferta",
  "descripcion": "$5 de descuento en corte clásico",
  "tipoDescuento": "MONTO_FIJO",
  "valorDescuento": 5.0,
  "fechaInicio": "2025-11-01",
  "fechaFin": "2025-11-30",
  "servicioId": 3  // Solo para ese servicio
}
```

### Caso 3: Promoción con Límite

```json
{
  "nombre": "Primeros 50 Clientes",
  "descripcion": "30% para los primeros 50",
  "tipoDescuento": "PORCENTAJE",
  "valorDescuento": 30.0,
  "fechaInicio": "2025-11-01",
  "fechaFin": "2025-12-31",
  "limiteUsos": 50  // Solo 50 usos
}
```

### Caso 4: Promoción con Monto Mínimo

```json
{
  "nombre": "Descuento en Compras Mayores",
  "descripcion": "$10 off en servicios de $50+",
  "tipoDescuento": "MONTO_FIJO",
  "valorDescuento": 10.0,
  "fechaInicio": "2025-11-01",
  "fechaFin": "2025-12-31",
  "montoMinimo": 50.0  // Requiere mínimo $50
}
```

## ❌ Manejo de Errores

### 404 Not Found
```json
{
  "status": "error",
  "mensaje": "Promoción no encontrada con ID: 123",
  "data": null
}
```

### 403 Forbidden
```json
{
  "status": "error",
  "mensaje": "No tienes permisos para acceder a esta promoción",
  "data": null
}
```

### 400 Bad Request - Promoción Expirada
```json
{
  "status": "error",
  "mensaje": "La promoción con ID 5 ha expirado o alcanzó su límite de usos",
  "data": null
}
```

### 400 Bad Request - Validación
```json
{
  "status": "error",
  "mensaje": "Error de validación: La fecha de fin debe ser posterior a la fecha de inicio",
  "data": {
    "fechaFin": "La fecha de fin debe ser posterior a la fecha de inicio"
  }
}
```

### 400 Bad Request - No Aplicable
```json
{
  "status": "error",
  "mensaje": "El monto del servicio ($8.00) no cumple con el monto mínimo requerido ($10.00) para aplicar la promoción",
  "data": null
}
```

## 🧪 Ejemplos de Uso con cURL

### Crear promoción
```bash
curl -X POST http://localhost:8080/api/owner/promociones \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Descuento de Bienvenida",
    "descripcion": "20% en tu primer corte",
    "tipoDescuento": "PORCENTAJE",
    "valorDescuento": 20.0,
    "fechaInicio": "2025-11-01",
    "fechaFin": "2025-12-31",
    "limiteUsos": 100
  }'
```

### Listar promociones activas
```bash
curl -X GET http://localhost:8080/api/owner/promociones/activas \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Activar promoción
```bash
curl -X PATCH http://localhost:8080/api/owner/promociones/1/activar \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Eliminar promoción
```bash
curl -X DELETE http://localhost:8080/api/owner/promociones/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 📝 Notas Técnicas

### Soft Delete
- Las promociones eliminadas no se borran físicamente
- Se marcan con `deleted = true` y `deleted_at = timestamp`
- Las consultas automáticamente excluyen registros eliminados

### Transaccionalidad
- Operaciones de escritura usan `@Transactional`
- Garantiza consistencia en la base de datos

### Performance
- Relaciones lazy loading para optimizar consultas
- Índices en `empresa_id`, `servicio_id`, y campos de fecha

### Query Optimization
```java
@Query("""
    SELECT p FROM Promocion p
    WHERE p.empresa.id = :empresaId
    AND p.deleted = false
    AND p.activa = true
    AND :fechaActual BETWEEN p.fechaInicio AND p.fechaFin
    AND (p.limiteUsos IS NULL OR p.usosActuales < p.limiteUsos)
    AND (p.servicio.id = :servicioId OR p.servicio IS NULL)
    ORDER BY p.valorDescuento DESC
    """)
```

## 🚀 Mejoras Futuras

- [ ] Códigos de promoción (cupones)
- [ ] Promociones por categoría de servicio
- [ ] Promociones por público objetivo
- [ ] Estadísticas de uso de promociones
- [ ] Promociones combinables
- [ ] Notificaciones de promociones próximas a expirar
- [ ] Dashboard de efectividad de promociones
- [ ] Promociones recurrentes (semanal, mensual)
- [ ] API pública para clientes (consultar promociones vigentes)

## 🔗 Referencias

- [SERVICIOS-RESERVAS.md](SERVICIOS-RESERVAS.md) - Documentación de servicios y reservas
- [SUPABASE-SETUP.md](SUPABASE-SETUP.md) - Configuración de almacenamiento
- [ENV-SETUP.md](ENV-SETUP.md) - Configuración de variables de entorno
