# 📋 Módulo de Configuración de Empresa

## 🎯 Funcionalidades

Este módulo permite a las empresas configurar completamente su perfil, incluyendo:

- ✅ Información básica (nombre, email, teléfono, descripción)
- ✅ Ubicación y geolocalización
- ✅ Horarios de atención por día
- ✅ Logo y banner (almacenados en Supabase)
- ✅ Redes sociales y sitio web
- ✅ Público objetivo

## 🏗️ Arquitectura

El módulo sigue **Arquitectura Hexagonal (Puertos y Adaptadores)**:

```
empresa/
├── adapters/
│   ├── in/
│   │   └── web/
│   │       └── EmpresaConfigController.java
│   └── out/
│       └── storage/
│           └── SupabaseStorageAdapter.java
├── application/
│   ├── dto/
│   │   ├── EmpresaConfigResponse.java
│   │   ├── ActualizarInformacionBasicaRequest.java
│   │   ├── ActualizarHorariosRequest.java
│   │   └── ActualizarUbicacionRequest.java
│   └── service/
│       └── EmpresaConfigService.java
├── domain/
│   └── port/
│       ├── in/
│       │   └── EmpresaConfigUseCase.java
│       └── out/
│           └── StoragePort.java
└── config/
    └── SupabaseConfig.java
```

## 📡 Endpoints API

> **Nota:** Todos los endpoints ahora usan `/mi-empresa` en lugar de `/{empresaId}`, ya que el ID de la empresa se detecta automáticamente del usuario OWNER autenticado.

### 1. Obtener Configuración

```http
GET /api/empresas/mi-empresa/configuracion
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Configuración obtenida",
  "data": {
    "id": 1,
    "nombre": "Barbería El Clásico",
    "direccion": "Calle Principal 123",
    "telefono": "+1234567890",
    "email": "contacto@elclasico.com",
    "estado": "ACTIVA",
    "publicoObjetivo": "HOMBRES",
    "latitud": 19.4326,
    "longitud": -99.1332,
    "horarioLunes": "09:00-18:00",
    "horarioMartes": "09:00-18:00",
    "horarioMiercoles": "09:00-18:00",
    "horarioJueves": "09:00-18:00",
    "horarioViernes": "09:00-20:00",
    "horarioSabado": "10:00-16:00",
    "horarioDomingo": "CERRADO",
    "logoUrl": "https://proyecto.supabase.co/storage/v1/object/public/clipper-images/empresas/1/logo/uuid.jpg",
    "bannerUrl": "https://proyecto.supabase.co/storage/v1/object/public/clipper-images/empresas/1/banner/uuid.jpg",
    "descripcion": "Barbería tradicional con más de 20 años de experiencia",
    "sitioWeb": "https://www.elclasico.com",
    "redesSociales": "{\"facebook\":\"elclasico\",\"instagram\":\"@elclasico\"}"
  }
}
```

### 2. Actualizar Información Básica

```http
PUT /api/empresas/mi-empresa/informacion-basica
Authorization: Bearer {token}
Content-Type: application/json
```

**Body:**
```json
{
  "nombre": "Barbería El Clásico",
  "direccion": "Calle Principal 123",
  "telefono": "+1234567890",
  "email": "contacto@elclasico.com",
  "descripcion": "Barbería tradicional con más de 20 años",
  "sitioWeb": "https://www.elclasico.com",
  "redesSociales": "{\"facebook\":\"elclasico\",\"instagram\":\"@elclasico\"}",
  "publicoObjetivo": "HOMBRES"
}
```

### 3. Actualizar Horarios

```http
PUT /api/empresas/mi-empresa/horarios
Authorization: Bearer {token}
Content-Type: application/json
```

**Body:**
```json
{
  "horarioLunes": "09:00-18:00",
  "horarioMartes": "09:00-18:00",
  "horarioMiercoles": "09:00-18:00",
  "horarioJueves": "09:00-18:00",
  "horarioViernes": "09:00-20:00",
  "horarioSabado": "10:00-16:00",
  "horarioDomingo": "CERRADO"
}
```

**Formatos de horario válidos:**
- Horario simple: `"09:00-18:00"`
- Con pausa: `"09:00-13:00,15:00-19:00"`
- Cerrado: `"CERRADO"`
- Sin configurar: `null`

### 4. Actualizar Ubicación

```http
PUT /api/empresas/mi-empresa/ubicacion
Authorization: Bearer {token}
Content-Type: application/json
```

**Body:**
```json
{
  "latitud": 19.4326,
  "longitud": -99.1332,
  "direccion": "Calle Principal 123, Col. Centro"
}
```

### 5. Subir Logo

```http
POST /api/empresas/mi-empresa/logo
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Body (form-data):**
- `file`: Archivo de imagen (JPG, PNG, GIF, WEBP)

**Validaciones:**
- ✅ Máximo 5 MB
- ✅ Solo imágenes
- ✅ Formatos: JPEG, PNG, GIF, WEBP

### 6. Subir Banner

```http
POST /api/empresas/mi-empresa/banner
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Body (form-data):**
- `file`: Archivo de imagen

### 7. Eliminar Logo

```http
DELETE /api/empresas/mi-empresa/logo
Authorization: Bearer {token}
```

### 8. Eliminar Banner

```http
DELETE /api/empresas/mi-empresa/banner
Authorization: Bearer {token}
```

## 🔐 Permisos

Todos los endpoints requieren autenticación y rol **OWNER**:

- ✅ **OWNER**: Solo puede gestionar su propia empresa
- ⚡ **Auto-detección**: No necesita especificar `empresaId`, se detecta automáticamente del usuario autenticado
- 🔒 **Seguridad**: Cada OWNER solo puede modificar la empresa asociada a su usuario

### Validaciones de Seguridad:

1. El usuario debe tener rol `OWNER`
2. El usuario debe tener una empresa asociada (`usuario.empresa != null`)
3. Solo puede modificar su propia empresa

## 🖼️ Gestión de Imágenes

### Supabase Storage

Las imágenes se almacenan en **Supabase Storage** con la siguiente estructura:

```
clipper-images/
└── empresas/
    └── {empresaId}/
        ├── logo/
        │   └── {uuid}.jpg
        └── banner/
            └── {uuid}.jpg
```

### Características:

- ✅ Nombres únicos con UUID
- ✅ URLs públicas accesibles
- ✅ Eliminación automática al reemplazar
- ✅ Validación de tipo y tamaño
- ✅ Estructura organizada por empresa

### Flujo de Subida:

1. **Validar** archivo (tipo, tamaño)
2. **Eliminar** imagen anterior (si existe)
3. **Generar** nombre único (UUID)
4. **Subir** a Supabase Storage
5. **Guardar** URL y path en BD

## 🗄️ Campos en Base de Datos

### Nuevos campos en tabla `empresas`:

```sql
-- Geolocalización
latitud DECIMAL(10,8),
longitud DECIMAL(11,8),

-- Horarios
horario_lunes VARCHAR(50),
horario_martes VARCHAR(50),
horario_miercoles VARCHAR(50),
horario_jueves VARCHAR(50),
horario_viernes VARCHAR(50),
horario_sabado VARCHAR(50),
horario_domingo VARCHAR(50),

-- Imágenes
logo_url VARCHAR(500),
logo_path VARCHAR(255),
banner_url VARCHAR(500),
banner_path VARCHAR(255),

-- Información adicional
descripcion VARCHAR(1000),
sitio_web VARCHAR(255),
redes_sociales VARCHAR(1000),
publico_objetivo VARCHAR(20) NOT NULL DEFAULT 'UNISEX'
```

## 🧪 Ejemplos de Uso

### Con cURL:

#### Actualizar información básica:
```bash
curl -X PUT \
  http://localhost:8080/api/empresas/mi-empresa/informacion-basica \
  -H "Authorization: Bearer tu-token" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Barbería El Clásico",
    "email": "contacto@elclasico.com",
    "telefono": "+1234567890",
    "descripcion": "Barbería tradicional",
    "publicoObjetivo": "HOMBRES"
  }'
```

#### Subir logo:
```bash
curl -X POST \
  http://localhost:8080/api/empresas/mi-empresa/logo \
  -H "Authorization: Bearer tu-token" \
  -F "file=@/path/to/logo.jpg"
```

### Con JavaScript (Frontend):

```javascript
// Subir logo (no necesita empresaId, se detecta automáticamente)
const formData = new FormData();
formData.append('file', fileInput.files[0]);

const response = await fetch('/api/empresas/mi-empresa/logo', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
});

const result = await response.json();
console.log('Logo URL:', result.data.logoUrl);
```

## 📝 Notas Importantes

1. **Redes Sociales**: Se almacenan como JSON string
   ```json
   {
     "facebook": "usuario",
     "instagram": "@usuario",
     "twitter": "@usuario",
     "tiktok": "@usuario"
   }
   ```

2. **Público Objetivo**: Valores del enum `PublicoObjetivo`
   - `HOMBRES`
   - `MUJERES`
   - `UNISEX`
   - `NIÑOS`
   - `NIÑAS`

3. **Coordenadas**: Formato decimal
   - Latitud: -90 a 90
   - Longitud: -180 a 180

4. **Imágenes**: 
   - Máximo 5 MB
   - Solo imágenes (JPEG, PNG, GIF, WEBP)
   - Se reemplazan automáticamente al subir nuevas

## 🚀 Configuración Requerida

Ver archivo **SUPABASE-SETUP.md** para:
- ✅ Crear proyecto en Supabase
- ✅ Configurar Storage bucket
- ✅ Configurar políticas RLS
- ✅ Obtener credenciales
- ✅ Configurar application.properties
