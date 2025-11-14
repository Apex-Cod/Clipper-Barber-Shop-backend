# Sistema de Imágenes de Perfil de Usuarios

## Descripción General

Este módulo permite a todos los usuarios del sistema (especialmente empleados) tener una imagen de perfil opcional. Las imágenes se almacenan en Supabase Storage en el bucket `Clipper-User`.

## Características

### 1. Campo Opcional
- Campo `profile_image_url` en la tabla `usuarios`
- Longitud máxima: 500 caracteres
- **Opcional**: Los usuarios pueden o no tener imagen de perfil

### 2. Almacenamiento en Supabase

**Bucket:** `Clipper-User`

**Estructura de carpetas:**
```
Clipper-User/
└── profiles/
    └── {userId}/
        └── {uuid}.{extension}
```

**Ejemplo:**
```
Clipper-User/profiles/user-123-abc/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg
```

### 3. Validaciones

#### Tipo de Archivo
- Solo se aceptan imágenes (`Content-Type: image/*`)
- Formatos comunes: JPG, PNG, GIF, WebP, etc.

#### Tamaño
- Tamaño máximo: **5 MB**
- Se valida antes de subir a Supabase

#### Seguridad
- Las imágenes se almacenan con nombres únicos (UUID)
- Solo usuarios autenticados pueden subir/eliminar imágenes

### 4. Permisos

#### Subir/Actualizar Imagen
- ✅ **Cualquier usuario** puede actualizar su propia imagen
- ✅ **ADMIN** puede actualizar la imagen de cualquier usuario
- ✅ **OWNER** puede actualizar la imagen de usuarios de su empresa

#### Eliminar Imagen
- ✅ **Cualquier usuario** puede eliminar su propia imagen
- ✅ **ADMIN** puede eliminar la imagen de cualquier usuario
- ✅ **OWNER** puede eliminar la imagen de usuarios de su empresa

## Endpoints

### 1. Subir/Actualizar Imagen de Perfil

#### Para cualquier usuario (con permisos)
```http
POST /api/users/{id}/profile-image
Content-Type: multipart/form-data
Authorization: Bearer {token}

file: [imagen_archivo]
```

**Respuesta exitosa:**
```json
{
  "success": true,
  "message": "Imagen de perfil actualizada",
  "data": {
    "id": "user-123",
    "name": "Juan",
    "lastName": "Pérez",
    "email": "juan@example.com",
    "role": "EMPLOYEE",
    "profileImageUrl": "https://supabase-url/storage/v1/object/public/Clipper-User/profiles/user-123/abc123.jpg",
    ...
  }
}
```

#### Para el usuario autenticado
```http
POST /api/users/me/profile-image
Content-Type: multipart/form-data
Authorization: Bearer {token}

file: [imagen_archivo]
```

### 2. Eliminar Imagen de Perfil

#### Para cualquier usuario (con permisos)
```http
DELETE /api/users/{id}/profile-image
Authorization: Bearer {token}
```

**Respuesta exitosa:**
```json
{
  "success": true,
  "message": "Imagen de perfil eliminada",
  "data": {
    "id": "user-123",
    "name": "Juan",
    "lastName": "Pérez",
    "profileImageUrl": null,
    ...
  }
}
```

#### Para el usuario autenticado
```http
DELETE /api/users/me/profile-image
Authorization: Bearer {token}
```

### 3. Obtener Perfil con Imagen

La URL de la imagen se incluye automáticamente en todos los endpoints que devuelven información de usuario:

```http
GET /api/users/me
GET /api/users/{id}
GET /api/users/empresa/{empresaId}
GET /api/users
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Usuario obtenido",
  "data": {
    "id": "user-123",
    "name": "Juan",
    "lastName": "Pérez",
    "email": "juan@example.com",
    "role": "EMPLOYEE",
    "profileImageUrl": "https://supabase-url/storage/v1/object/public/Clipper-User/profiles/user-123/abc123.jpg",
    ...
  }
}
```

## Comportamiento del Sistema

### Al Subir una Nueva Imagen

1. **Validación**: Se valida tipo y tamaño del archivo
2. **Eliminación de imagen anterior**: Si el usuario ya tenía una imagen, se elimina automáticamente de Supabase
3. **Generación de nombre único**: Se genera un UUID para el nombre del archivo
4. **Subida a Supabase**: Se sube al bucket `Clipper-User` en `profiles/{userId}/{uuid}.{ext}`
5. **Actualización en BD**: Se guarda la URL pública en la base de datos
6. **Respuesta**: Se devuelve el usuario actualizado con la nueva URL

### Al Eliminar una Imagen

1. **Validación de permisos**: Se verifica que el usuario tenga permisos
2. **Eliminación de Supabase**: Se elimina el archivo del bucket
3. **Actualización en BD**: Se establece `profile_image_url` a `NULL`
4. **Respuesta**: Se devuelve el usuario actualizado sin imagen

### Al Eliminar un Usuario (Soft Delete)

- La imagen NO se elimina automáticamente de Supabase
- Esto permite restaurar el usuario con su imagen intacta
- Si se desea eliminar la imagen, debe hacerse manualmente antes de eliminar el usuario

## Caso de Uso Principal: Empleados

### ¿Por qué es importante?

Los **clientes** necesitan poder identificar visualmente a los **empleados** al momento de:
- Ver la lista de empleados disponibles
- Seleccionar un empleado para una reserva
- Ver quién atendió sus reservas anteriores

### Flujo Típico

1. **OWNER registra empleado** sin imagen
2. **OWNER o el empleado** sube la foto de perfil
3. **Cliente** ve la lista de empleados con sus fotos
4. **Cliente** puede identificar fácilmente al empleado que prefiere

## Configuración

### application.properties

```properties
# Supabase Configuration
supabase.url=https://your-project.supabase.co
supabase.api-key=your-supabase-key
supabase.storage.bucket.usuarios=Clipper-User
```

### Configuración del Bucket en Supabase

1. Crear bucket `Clipper-User` en Supabase Storage
2. Configurar como público o con políticas de acceso apropiadas
3. Permitir subida de imágenes (JPG, PNG, GIF, WebP)
4. Configurar límite de tamaño (recomendado: 5 MB)

## Migración de Base de Datos

### SQL Migration

```sql
-- Agregar campo de imagen de perfil a usuarios
ALTER TABLE usuarios 
ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500);

COMMENT ON COLUMN usuarios.profile_image_url IS 
'URL pública de la imagen de perfil del usuario en Supabase Storage (opcional)';
```

**Ubicación:** `init/migration-add-profile-image.sql`

## Componentes Técnicos

### 1. Entidad

**Archivo:** `Entities/Usuario.java`

```java
@Column(name = "profile_image_url", length = 500)
private String profileImageUrl;
```

### 2. DTO

**Archivo:** `user/application/dto/UsuarioResponse.java`

```java
private String profileImageUrl;
```

### 3. Servicio

**Archivo:** `user/application/service/UserManagementService.java`

**Métodos:**
- `subirImagenPerfil(String userId, MultipartFile file, String updatedBy)`
- `eliminarImagenPerfil(String userId, String updatedBy)`

### 4. Controlador

**Archivo:** `user/adapters/in/web/UserManagementController.java`

**Endpoints:**
- `POST /api/users/{id}/profile-image`
- `POST /api/users/me/profile-image`
- `DELETE /api/users/{id}/profile-image`
- `DELETE /api/users/me/profile-image`

### 5. Adaptador de Storage

**Archivo:** `empresa/adapters/out/storage/SupabaseStorageAdapter.java`

**Métodos:**
- `uploadUserProfileImage(String userId, MultipartFile file)`
- `deleteUserProfileImage(String userId, String imageUrl)`
- `validateImageFile(MultipartFile file)`
- `generateUniqueFileName(String originalFilename)`

## Manejo de Errores

### Archivo Vacío
```json
{
  "success": false,
  "message": "El archivo está vacío"
}
```

### Tipo de Archivo Inválido
```json
{
  "success": false,
  "message": "El archivo debe ser una imagen"
}
```

### Tamaño Excedido
```json
{
  "success": false,
  "message": "El archivo no debe superar los 5MB"
}
```

### Usuario No Encontrado
```json
{
  "success": false,
  "message": "Usuario no encontrado"
}
```

### Sin Permisos
```json
{
  "success": false,
  "message": "No tiene permisos para realizar esta acción"
}
```

### Error de Supabase
```json
{
  "success": false,
  "message": "Error al subir archivo a Supabase"
}
```

## Ejemplos de Uso

### cURL - Subir Imagen

```bash
curl -X POST \
  http://localhost:8080/api/users/me/profile-image \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -F 'file=@/path/to/profile.jpg'
```

### cURL - Eliminar Imagen

```bash
curl -X DELETE \
  http://localhost:8080/api/users/me/profile-image \
  -H 'Authorization: Bearer YOUR_TOKEN'
```

### JavaScript/Fetch - Subir Imagen

```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);

fetch('/api/users/me/profile-image', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
})
.then(response => response.json())
.then(data => console.log('Imagen actualizada:', data));
```

### React Native/Expo - Subir Imagen

```javascript
import * as ImagePicker from 'expo-image-picker';

const subirImagenPerfil = async () => {
  const result = await ImagePicker.launchImageLibraryAsync({
    mediaTypes: ImagePicker.MediaTypeOptions.Images,
    allowsEditing: true,
    aspect: [1, 1],
    quality: 0.8,
  });

  if (!result.canceled) {
    const formData = new FormData();
    formData.append('file', {
      uri: result.assets[0].uri,
      type: 'image/jpeg',
      name: 'profile.jpg',
    });

    const response = await fetch('/api/users/me/profile-image', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      body: formData,
    });

    const data = await response.json();
    console.log('Imagen actualizada:', data);
  }
};
```

## Ventajas del Sistema

### 1. Identificación Visual
- Clientes pueden ver fotos de empleados
- Facilita la selección de empleado preferido
- Mejora la experiencia del usuario

### 2. Profesionalismo
- Imágenes de perfil dan imagen profesional
- Aumenta la confianza del cliente
- Personaliza la experiencia

### 3. Flexibilidad
- Campo opcional (no todos necesitan foto)
- Todos los usuarios pueden tener imagen
- Fácil de actualizar/eliminar

### 4. Seguridad
- Validación de tipo y tamaño
- Nombres únicos evitan conflictos
- Permisos basados en roles

### 5. Escalabilidad
- Almacenamiento en Supabase (cloud)
- No afecta rendimiento de la API
- Fácil de mantener y escalar

## Notas Importantes

1. **Campo Opcional**: No es obligatorio tener imagen de perfil
2. **Todos los Usuarios**: Cualquier rol puede tener imagen (CLIENT, EMPLOYEE, OWNER, ADMIN)
3. **Reemplazo Automático**: Al subir nueva imagen, la anterior se elimina automáticamente
4. **Soft Delete**: La imagen se mantiene al hacer soft delete del usuario
5. **Permisos Estrictos**: Solo el usuario, su OWNER o un ADMIN pueden modificar su imagen

## Mejoras Futuras

### Posibles Funcionalidades

1. **Redimensionamiento Automático**
   - Crear thumbnails para listas
   - Optimizar tamaño de archivos
   - Diferentes resoluciones

2. **Validación de Contenido**
   - Detectar si realmente es una cara
   - Prevenir contenido inapropiado
   - Análisis de calidad de imagen

3. **Caché de Imágenes**
   - CDN para mejor rendimiento
   - Cache en cliente
   - Lazy loading

4. **Múltiples Imágenes**
   - Galería de fotos del empleado
   - Imágenes de trabajos realizados
   - Portfolio de servicios

5. **Edición de Imágenes**
   - Recortar en el cliente
   - Filtros y efectos
   - Ajuste de brillo/contraste

## Soporte

Para problemas o preguntas sobre el sistema de imágenes de perfil:
- Revisar logs de la aplicación
- Verificar configuración de Supabase
- Comprobar permisos del bucket
- Validar tokens de autenticación

---

**Versión:** 1.0  
**Fecha:** Noviembre 2024  
**Autor:** ClipperBarberShop Team
