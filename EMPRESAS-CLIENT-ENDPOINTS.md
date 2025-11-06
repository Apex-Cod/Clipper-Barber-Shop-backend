# Endpoints de Listado de Empresas para Clientes

## Descripción

Se agregaron endpoints en el `ServicioClientController` para que los usuarios con rol **CLIENT** puedan listar y consultar las empresas activas en el sistema.

## Endpoints Agregados

### 1. Listar Todas las Empresas (Sin Paginación)

**Endpoint:** `GET /api/client/servicios/empresas`

**Rol:** `CLIENT`

**Descripción:** Obtiene la lista completa de todas las empresas activas (no borradas).

**Request:**
```bash
GET /api/client/servicios/empresas
Authorization: Bearer {jwt_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Empresas obtenidas",
  "data": [
    {
      "id": 1,
      "nombre": "Barbería El Corte Perfecto",
      "direccion": "Calle Principal 123",
      "telefono": "+1234567890",
      "email": "info@corteperfecto.com",
      "descripcion": "La mejor barbería de la ciudad",
      "publicoObjetivo": "UNISEX",
      "horarioLunes": "09:00-18:00",
      "horarioMartes": "09:00-18:00",
      "horarioMiercoles": "09:00-18:00",
      "horarioJueves": "09:00-18:00",
      "horarioViernes": "09:00-20:00",
      "horarioSabado": "10:00-16:00",
      "horarioDomingo": "CERRADO",
      "logoUrl": "https://storage.supabase.co/empresas/1/logo.jpg",
      "bannerUrl": "https://storage.supabase.co/empresas/1/banner.jpg",
      "latitud": 40.7128,
      "longitud": -74.0060,
      "sitioWeb": "https://corteperfecto.com",
      "redesSociales": "{\"facebook\":\"fb.com/corteperfecto\",\"instagram\":\"@corteperfecto\"}"
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### 2. Listar Empresas con Paginación

**Endpoint:** `GET /api/client/servicios/empresas/paginadas`

**Rol:** `CLIENT`

**Descripción:** Obtiene las empresas activas con paginación para manejar grandes volúmenes de datos.

**Request:**
```bash
GET /api/client/servicios/empresas/paginadas?page=0&size=10&sort=nombre,asc
Authorization: Bearer {jwt_token}
```

**Query Parameters:**
- `page` (opcional, default: 0): Número de página (base 0)
- `size` (opcional, default: 20): Cantidad de elementos por página
- `sort` (opcional): Campo y dirección de ordenamiento (ej: `nombre,asc`)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Empresas obtenidas",
  "data": {
    "content": [
      {
        "id": 1,
        "nombre": "Barbería El Corte Perfecto",
        "direccion": "Calle Principal 123",
        "telefono": "+1234567890",
        "email": "info@corteperfecto.com",
        "descripcion": "La mejor barbería de la ciudad",
        "publicoObjetivo": "UNISEX",
        "horarioLunes": "09:00-18:00",
        "horarioMartes": "09:00-18:00",
        "horarioMiercoles": "09:00-18:00",
        "horarioJueves": "09:00-18:00",
        "horarioViernes": "09:00-20:00",
        "horarioSabado": "10:00-16:00",
        "horarioDomingo": "CERRADO",
        "logoUrl": "https://storage.supabase.co/empresas/1/logo.jpg",
        "bannerUrl": "https://storage.supabase.co/empresas/1/banner.jpg",
        "latitud": 40.7128,
        "longitud": -74.0060,
        "sitioWeb": "https://corteperfecto.com",
        "redesSociales": "{\"facebook\":\"fb.com/corteperfecto\",\"instagram\":\"@corteperfecto\"}"
      }
    ],
    "pageable": {
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "pageNumber": 0,
      "pageSize": 10,
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "last": false,
    "totalPages": 5,
    "totalElements": 47,
    "first": true,
    "size": 10,
    "number": 0,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "numberOfElements": 10,
    "empty": false
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### 3. Obtener una Empresa por ID

**Endpoint:** `GET /api/client/servicios/empresas/{id}`

**Rol:** `CLIENT`

**Descripción:** Obtiene los detalles de una empresa específica por su ID (solo si no está borrada).

**Request:**
```bash
GET /api/client/servicios/empresas/1
Authorization: Bearer {jwt_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Empresa obtenida",
  "data": {
    "id": 1,
    "nombre": "Barbería El Corte Perfecto",
    "direccion": "Calle Principal 123",
    "telefono": "+1234567890",
    "email": "info@corteperfecto.com",
    "descripcion": "La mejor barbería de la ciudad",
    "publicoObjetivo": "UNISEX",
    "horarioLunes": "09:00-18:00",
    "horarioMartes": "09:00-18:00",
    "horarioMiercoles": "09:00-18:00",
    "horarioJueves": "09:00-18:00",
    "horarioViernes": "09:00-20:00",
    "horarioSabado": "10:00-16:00",
    "horarioDomingo": "CERRADO",
    "logoUrl": "https://storage.supabase.co/empresas/1/logo.jpg",
    "bannerUrl": "https://storage.supabase.co/empresas/1/banner.jpg",
    "latitud": 40.7128,
    "longitud": -74.0060,
    "sitioWeb": "https://corteperfecto.com",
    "redesSociales": "{\"facebook\":\"fb.com/corteperfecto\",\"instagram\":\"@corteperfecto\"}"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Response (404 NOT FOUND):**
```json
{
  "success": false,
  "message": "Empresa no encontrada con ID: 1",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Cambios Realizados

### 1. Capa de Repositorio

**`EmpresaRepositoryPort.java`**
```java
// Se agregaron métodos para listar empresas activas
Optional<Empresa> findByIdAndDeletedFalse(Long id);
List<Empresa> findAllByDeletedFalse();
Page<Empresa> findAllByDeletedFalse(Pageable pageable);
```

**`SpringDataEmpresaRepository.java`**
```java
// Se agregaron queries de Spring Data JPA
Optional<Empresa> findByIdAndDeletedFalse(Long id);
List<Empresa> findAllByDeletedFalse();
Page<Empresa> findAllByDeletedFalse(Pageable pageable);
```

**`EmpresaPersistenceAdapter.java`**
```java
// Se implementaron los métodos del repositorio port
@Override
public Optional<Empresa> findByIdAndDeletedFalse(Long id) {
    return repo.findByIdAndDeletedFalse(id);
}

@Override
public List<Empresa> findAllByDeletedFalse() {
    return repo.findAllByDeletedFalse();
}

@Override
public Page<Empresa> findAllByDeletedFalse(Pageable pageable) {
    return repo.findAllByDeletedFalse(pageable);
}
```

### 2. Capa de Aplicación

**Nuevo archivo:** `EmpresaResponse.java` (DTO)
- DTO para respuestas de empresa
- Incluye todos los campos de la entidad Empresa
- Método estático `fromEntity()` para convertir de entidad a DTO

**Nuevo archivo:** `EmpresaClientService.java`
- Servicio para lógica de negocio relacionada con empresas para clientes
- Métodos:
  - `listarEmpresas()`: Lista todas las empresas activas
  - `listarEmpresasPaginadas(Pageable)`: Lista empresas con paginación
  - `obtenerEmpresaPorId(Long)`: Obtiene una empresa por ID

### 3. Capa de Presentación

**`ServicioClientController.java`**
- Se inyectó `EmpresaClientService`
- Se agregaron 3 nuevos endpoints para empresas
- Se mantuvieron los 4 endpoints existentes para servicios
- Todos los endpoints están protegidos con `@PreAuthorize("hasRole('CLIENT')")`

---

## Estructura de Archivos

```
src/main/java/apex/code/clipperBarberShop/
├── register/
│   ├── application/
│   │   ├── dto/
│   │   │   └── EmpresaResponse.java (NUEVO)
│   │   └── service/
│   │       └── EmpresaClientService.java (NUEVO)
│   ├── adapters/
│   │   └── out/
│   │       └── persistence/
│   │           ├── EmpresaPersistenceAdapter.java (ACTUALIZADO)
│   │           └── SpringDataEmpresaRepository.java (ACTUALIZADO)
│   └── domain/
│       └── port/
│           └── out/
│               └── EmpresaRepositoryPort.java (ACTUALIZADO)
└── servicio/
    └── adapters/
        └── in/
            └── web/
                └── ServicioClientController.java (ACTUALIZADO)
```

---

## Validaciones y Seguridad

✅ **Soft Delete:** Solo se muestran empresas con `deleted = false`

✅ **Autorización:** Todos los endpoints requieren rol `CLIENT`

✅ **JWT:** Autenticación mediante token JWT en el header `Authorization`

✅ **Transacciones:** Los métodos del servicio están marcados con `@Transactional(readOnly = true)`

✅ **Paginación:** Soporte para paginación en grandes volúmenes de datos

---

## Casos de Uso

### 1. App Móvil - Vista de Empresas Disponibles
```
Cliente abre la app → Lista empresas (GET /empresas/paginadas) → 
Selecciona empresa → Ver detalles (GET /empresas/{id}) → 
Ver servicios de la empresa (GET /empresa/{empresaId})
```

### 2. Búsqueda de Empresas por Ubicación
```
Cliente busca empresas cercanas → App obtiene lista (GET /empresas) → 
Filtra por latitud/longitud → Muestra empresas ordenadas por distancia
```

### 3. Navegación de Catálogo
```
Cliente explora el catálogo → Lista paginada (GET /empresas/paginadas?page=0&size=10) → 
Navega por páginas → Selecciona empresa de interés
```

---

## Pruebas

### Ejemplo de cURL - Listar Empresas

```bash
curl -X GET "http://localhost:8080/api/client/servicios/empresas" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

### Ejemplo de cURL - Listar Empresas Paginadas

```bash
curl -X GET "http://localhost:8080/api/client/servicios/empresas/paginadas?page=0&size=5&sort=nombre,asc" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

### Ejemplo de cURL - Obtener Empresa por ID

```bash
curl -X GET "http://localhost:8080/api/client/servicios/empresas/1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

---

## Notas Adicionales

- Los horarios están en formato String (ej: "09:00-18:00", "CERRADO")
- Las redes sociales están en formato JSON String
- El público objetivo es un enum: `UNISEX`, `HOMBRES`, `MUJERES`
- Las URLs de logo y banner apuntan a Supabase Storage
- La latitud y longitud pueden usarse para mostrar ubicación en mapas

---

## Próximas Mejoras Sugeridas

1. **Filtros Avanzados:**
   - Filtrar por público objetivo
   - Filtrar por ubicación (radio de búsqueda)
   - Filtrar por disponibilidad (abierto/cerrado)

2. **Búsqueda:**
   - Búsqueda por nombre de empresa
   - Búsqueda por ubicación geográfica

3. **Ordenamiento:**
   - Ordenar por distancia al cliente
   - Ordenar por calificación (si existe)
   - Ordenar por popularidad

4. **Cache:**
   - Implementar caché para lista de empresas
   - TTL configurable
