# 📋 Guía de Configuración de Supabase Storage

## 🎯 Pasos para Configurar Supabase

### 1. Crear Proyecto en Supabase

1. Ve a [https://supabase.com](https://supabase.com)
2. Inicia sesión o crea una cuenta
3. Click en "New Project"
4. Completa los datos:
   - **Name**: clipper-barber-shop (o el nombre que prefieras)
   - **Database Password**: Guarda esta contraseña de forma segura
   - **Region**: Selecciona la región más cercana
5. Click en "Create new project"

### 2. Crear Buckets de Storage

Necesitas crear **DOS buckets** para el proyecto:

#### Bucket 1: clipper-images (Para empresas)

1. En el panel izquierdo, click en **Storage**
2. Click en **"Create a new bucket"**
3. Configurar el bucket:
   - **Name**: `clipper-images`
   - **Public**: ✅ Marcar como público (para que las imágenes sean accesibles)
   - **File size limit**: 5 MB (opcional)
   - **Allowed MIME types**: `image/jpeg, image/png, image/webp, image/gif`
4. Click en **"Create bucket"**

#### Bucket 2: Barber-Services (Para servicios)

1. Click en **"Create a new bucket"** nuevamente
2. Configurar el bucket:
   - **Name**: `Barber-Services`
   - **Public**: ✅ Marcar como público
   - **File size limit**: 5 MB (opcional)
   - **Allowed MIME types**: `image/jpeg, image/png, image/webp, image/gif`
3. Click en **"Create bucket"**

### 3. Configurar Políticas de Acceso (RLS - Row Level Security)

⚠️ **IMPORTANTE**: Si usas el **service_role key** (recomendado para backend), las políticas RLS se **omiten automáticamente**. Solo necesitas configurarlas si usas el **anon key**.

#### Opción A: Usar Service Role Key (Recomendado - Backend)

Si usas el **service_role key** en tu backend, NO necesitas configurar políticas RLS ya que este key bypasea todas las políticas. Esto es **más seguro y sencillo** para aplicaciones backend.

#### Opción B: Usar Anon Key con Políticas RLS

Si prefieres usar el **anon key**, necesitas configurar estas políticas para **AMBOS buckets**:

##### Políticas para clipper-images (Empresas):

```sql
-- Política para INSERT (subir archivos)
CREATE POLICY "Permitir subida de imágenes empresas"
ON storage.objects FOR INSERT
TO authenticated
WITH CHECK (bucket_id = 'clipper-images');

-- Política para SELECT (leer archivos públicamente)
CREATE POLICY "Permitir lectura pública de imágenes empresas"
ON storage.objects FOR SELECT
TO public
USING (bucket_id = 'clipper-images');

-- Política para DELETE (eliminar archivos)
CREATE POLICY "Permitir eliminación de imágenes empresas"
ON storage.objects FOR DELETE
TO authenticated
USING (bucket_id = 'clipper-images');
```

##### Políticas para Barber-Services (Servicios):

```sql
-- Política para INSERT (subir archivos)
CREATE POLICY "Permitir subida de imágenes servicios"
ON storage.objects FOR INSERT
TO authenticated
WITH CHECK (bucket_id = 'Barber-Services');

-- Política para SELECT (leer archivos públicamente)
CREATE POLICY "Permitir lectura pública de imágenes servicios"
ON storage.objects FOR SELECT
TO public
USING (bucket_id = 'Barber-Services');

-- Política para DELETE (eliminar archivos)
CREATE POLICY "Permitir eliminación de imágenes servicios"
ON storage.objects FOR DELETE
TO authenticated
USING (bucket_id = 'Barber-Services');
```

**Nota**: Estas políticas se configuran en **Storage** > **Policies** > **New policy**

### 4. Obtener Credenciales

1. Ve a **Settings** (⚙️) en el panel izquierdo
2. Click en **API**
3. Copia los siguientes valores:

   - **Project URL**: `https://tuproyecto.supabase.co`
   - **🔑 service_role key** (Recomendado para backend): `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
     - ⚠️ **NO expongas esta key en el frontend**
     - ✅ Bypasea políticas RLS
     - ✅ Más simple de usar en backend
   
   **O alternativamente:**
   
   - **anon/public key** (Si prefieres usar políticas RLS): `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
     - ✅ Segura para frontend
     - ⚠️ Requiere configurar políticas RLS

### 5. Configurar application.properties

Agrega estas propiedades a tu archivo `application.properties`:

```properties
# Supabase Configuration
supabase.url=https://tuproyecto.supabase.co
# IMPORTANTE: Usa service_role key (no anon key) para operaciones backend
# El service_role key bypasea las políticas RLS
supabase.api-key=tu-service-role-key-aqui

# Buckets de Storage
supabase.storage.bucket.empresas=clipper-images
supabase.storage.bucket.servicios=Barber-Services
```

**⚠️ IMPORTANTE**: 
- Usa el **service_role key** (no el anon key) para operaciones desde el backend
- El service_role key bypasea las políticas RLS, lo que simplifica la configuración
- **NUNCA** expongas el service_role key en el frontend o en repositorios públicos
- Considera usar variables de entorno para las credenciales en producción 
- Reemplaza `tuproyecto` con tu URL real de Supabase
- Reemplaza `tu-api-key-aqui` con tu API Key (anon/public)
- NO uses la Service Role Key en el frontend, solo en backend

### 6. Configuración Avanzada (Opcional)

#### Estructura de carpetas recomendada:

```
clipper-images/                    (Bucket para empresas)
├── empresas/
│   ├── 1/
│   │   ├── logo/
│   │   │   └── uuid.jpg
│   │   └── banner/
│   │       └── uuid.jpg
│   ├── 2/
│   │   ├── logo/
│   │   └── banner/

Barber-Services/                   (Bucket para servicios)
├── servicios/
│   ├── 1/
│   │   └── uuid.jpg
│   ├── 2/
│   │   └── uuid.jpg
```

#### Límites de tamaño por tipo de imagen:

En el código ya está configurado:
- **Máximo**: 5 MB por archivo
- **Formatos permitidos**: JPEG, PNG, GIF, WEBP

## 🔐 Seguridad

### Mejores Prácticas:

1. **Nunca expongas la Service Role Key**: Solo usa la anon/public key
2. **Variables de entorno**: En producción, usa variables de entorno:
   ```bash
   export SUPABASE_URL=https://tuproyecto.supabase.co
   export SUPABASE_API_KEY=tu-api-key
   ```
3. **Validación de archivos**: El código ya valida:
   - Tipo de archivo (solo imágenes)
   - Tamaño máximo (5MB)
   - Nombres únicos (UUID)

## 📡 Endpoints Disponibles

Una vez configurado, estos son los endpoints que funcionarán:

### Subir Logo
```bash
POST /api/empresas/{empresaId}/logo
Content-Type: multipart/form-data
Body: file=@logo.jpg
```

### Subir Banner
```bash
POST /api/empresas/{empresaId}/banner
Content-Type: multipart/form-data
Body: file=@banner.jpg
```

### Eliminar Logo
```bash
DELETE /api/empresas/{empresaId}/logo
```

### Eliminar Banner
```bash
DELETE /api/empresas/{empresaId}/banner
```

### Obtener Configuración
```bash
GET /api/empresas/{empresaId}/configuracion
```

## 🧪 Probar la Configuración

### Con cURL:

```bash
# Subir logo
curl -X POST \
  http://localhost:8080/api/empresas/1/logo \
  -H "Authorization: Bearer tu-jwt-token" \
  -F "file=@/path/to/logo.jpg"
```

### Con Postman:

1. **Method**: POST
2. **URL**: `http://localhost:8080/api/empresas/1/logo`
3. **Headers**: 
   - `Authorization: Bearer tu-jwt-token`
4. **Body**: 
   - Type: `form-data`
   - Key: `file` (tipo File)
   - Value: Selecciona tu imagen

## 🔧 Troubleshooting

### Error: "Bucket not found"
- Verifica que el nombre del bucket sea correcto en `application.properties`
- Verifica que el bucket exista en Supabase Storage

### Error: "Unauthorized"
- Verifica que el API Key sea correcto
- Verifica las políticas RLS en Supabase

### Error: "File too large"
- El límite es 5MB por defecto
- Puedes cambiarlo en `SupabaseStorageAdapter.validateImageFile()`

### Error: "Invalid file type"
- Solo se permiten imágenes
- Formatos válidos: JPG, PNG, GIF, WEBP

## 📚 Recursos Adicionales

- [Documentación Supabase Storage](https://supabase.com/docs/guides/storage)
- [API Reference Storage](https://supabase.com/docs/reference/javascript/storage-from-upload)
- [Row Level Security (RLS)](https://supabase.com/docs/guides/auth/row-level-security)
