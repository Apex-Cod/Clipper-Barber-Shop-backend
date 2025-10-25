# 📸 Upload de Imágenes en Servicios

## 📋 Descripción

El endpoint de creación y actualización de servicios ahora soporta **subida directa de imágenes** usando `multipart/form-data`. La imagen se sube automáticamente a Supabase Storage y se almacena la URL en el servicio.

## 🔧 Implementación

### Endpoint Combinado

Los endpoints POST y PUT ahora aceptan tanto los datos del servicio como la imagen en una sola petición:

```
POST   /api/owner/servicios
PUT    /api/owner/servicios/{id}
Content-Type: multipart/form-data
```

## 📡 Uso del Endpoint

### Crear Servicio con Imagen

```bash
curl -X POST http://localhost:8080/api/owner/servicios \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F 'servicioData={
    "empresaId": 1,
    "name": "Corte Premium",
    "description": "Corte de cabello premium con productos de alta calidad",
    "duration": 45,
    "price": 30.00,
    "categoria": "Cortes",
    "publicoObjetivo": "HOMBRES"
  }' \
  -F 'image=@/path/to/image.jpg'
```

### Crear Servicio sin Imagen (Solo con URL)

```bash
curl -X POST http://localhost:8080/api/owner/servicios \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F 'servicioData={
    "empresaId": 1,
    "name": "Corte Básico",
    "description": "Corte de cabello básico",
    "duration": 30,
    "price": 15.00,
    "categoria": "Cortes",
    "publicoObjetivo": "HOMBRES",
    "imageUrl": "https://supabase.../existing-image.jpg"
  }'
```

### Actualizar Servicio con Nueva Imagen

```bash
curl -X PUT http://localhost:8080/api/owner/servicios/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F 'servicioData={
    "name": "Corte Premium Actualizado",
    "description": "Nueva descripción",
    "duration": 45,
    "price": 35.00,
    "categoria": "Cortes",
    "publicoObjetivo": "HOMBRES"
  }' \
  -F 'image=@/path/to/new-image.jpg'
```

## 🎯 Campos del Request

### servicioData (JSON String - Requerido)

```json
{
  "empresaId": 1,              // Requerido al crear
  "name": "String",            // Requerido, 2-100 caracteres
  "description": "String",     // Opcional, max 500 caracteres
  "duration": 30,              // Requerido, 5-480 minutos
  "price": 15.00,              // Requerido, > 0
  "categoria": "String",       // Opcional, max 50 caracteres
  "publicoObjetivo": "HOMBRES",// Requerido: HOMBRES, MUJERES, UNISEX, NIÑOS, NIÑAS
  "imageUrl": "String"         // Opcional, max 500 caracteres (si no se sube imagen)
}
```

### image (File - Opcional)

- **Tipo**: Imagen (jpg, jpeg, png, gif, webp)
- **Tamaño máximo**: 5 MB
- **Nombre**: Se genera automáticamente con formato `servicio-{timestamp}-{originalName}`
- **Ubicación**: Supabase Storage en `Barber-Services/servicios/`

## ✅ Validaciones

### Validaciones de Imagen

- ✅ Formato debe ser imagen (content-type: image/*)
- ✅ Tamaño máximo: 5 MB
- ✅ No puede estar vacía si se envía
- ❌ Si falla la subida, el servicio NO se crea/actualiza

### Validaciones de Datos

- ✅ Todas las validaciones de `ServicioRequest` se aplican
- ✅ JSON debe ser válido
- ❌ Si el JSON es inválido, retorna error 400

## 🔄 Flujo de Procesamiento

### Crear Servicio

1. **Recibir datos**: Parse JSON de `servicioData`
2. **Validar permisos**: Verificar que el OWNER tenga acceso a la empresa
3. **Subir imagen** (si existe):
   - Validar formato y tamaño
   - Generar nombre único
   - Subir a Supabase `Barber-Services/servicios/`
   - Obtener URL pública
4. **Crear servicio**: Guardar en BD con URL de imagen
5. **Retornar respuesta**: 201 Created con datos del servicio

### Actualizar Servicio

1. **Buscar servicio**: Por ID
2. **Validar acceso**: Verificar ownership
3. **Subir nueva imagen** (si existe):
   - Validar y subir a Supabase
   - Reemplazar URL antigua
4. **Actualizar campos**: Guardar cambios
5. **Retornar respuesta**: 200 OK con datos actualizados

## 📁 Estructura de Almacenamiento

```
Supabase Storage: Barber-Services
├── servicios/
│   ├── servicio-1730000000-corte-premium.jpg
│   ├── servicio-1730000100-barba-diseno.jpg
│   ├── servicio-1-1730000200-actualizado.jpg
│   └── ...
└── defaults/
    ├── default-hombres-corte.jpg
    ├── default-mujeres-corte.jpg
    └── ...
```

**Patrón de nombre**: `servicio-{id/timestamp}-{timestamp}-{originalName}`

## 🌐 Ejemplos con JavaScript/Fetch

### Ejemplo con FormData

```javascript
const formData = new FormData();

// Agregar datos del servicio
const servicioData = {
  empresaId: 1,
  name: "Corte Premium",
  description: "Corte con productos premium",
  duration: 45,
  price: 30.00,
  categoria: "Cortes",
  publicoObjetivo: "HOMBRES"
};
formData.append('servicioData', JSON.stringify(servicioData));

// Agregar imagen desde input file
const fileInput = document.querySelector('input[type="file"]');
if (fileInput.files[0]) {
  formData.append('image', fileInput.files[0]);
}

// Enviar request
fetch('http://localhost:8080/api/owner/servicios', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
    // NO incluir Content-Type, el navegador lo maneja con multipart/form-data
  },
  body: formData
})
.then(res => res.json())
.then(data => console.log('Servicio creado:', data))
.catch(err => console.error('Error:', err));
```

### Ejemplo con React

```jsx
const handleSubmit = async (e) => {
  e.preventDefault();
  
  const formData = new FormData();
  formData.append('servicioData', JSON.stringify({
    empresaId: 1,
    name: servicioName,
    description: servicioDescription,
    duration: servicioDuration,
    price: servicioPrice,
    categoria: servicioCategoria,
    publicoObjetivo: servicioPublico
  }));
  
  if (selectedImage) {
    formData.append('image', selectedImage);
  }
  
  try {
    const response = await fetch('/api/owner/servicios', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    });
    
    const result = await response.json();
    console.log('Success:', result);
  } catch (error) {
    console.error('Error:', error);
  }
};
```

## ❌ Manejo de Errores

### Error al Subir Imagen

```json
{
  "status": "error",
  "mensaje": "Error al subir la imagen: El archivo debe ser una imagen",
  "data": null
}
```

### Error en JSON

```json
{
  "status": "error",
  "mensaje": "Error al procesar los datos del servicio: Unexpected character...",
  "data": null
}
```

### Error de Validación

```json
{
  "status": "error",
  "mensaje": "Error de validación",
  "data": {
    "name": "El nombre del servicio es obligatorio",
    "price": "El precio debe ser mayor a 0"
  }
}
```

### Error de Permisos

```json
{
  "status": "error",
  "mensaje": "No tiene permisos para crear servicios en esta empresa",
  "data": null
}
```

## 📝 Notas Importantes

1. **Compatibilidad**: Los endpoints antiguos (sin imagen) siguen funcionando
2. **Imagen Opcional**: Puedes crear/actualizar servicios sin imagen
3. **URL vs Imagen**: Si envías ambos, la imagen tiene prioridad
4. **Transaccional**: Si falla la subida, el servicio NO se crea/actualiza
5. **Seguridad**: Solo el OWNER de la empresa puede subir imágenes
6. **Formato**: Usar `multipart/form-data`, NO `application/json`

## 🚀 Próximas Mejoras

- [ ] Redimensionamiento automático de imágenes
- [ ] Generación de thumbnails
- [ ] Soporte para múltiples imágenes por servicio
- [ ] Compresión automática de imágenes
- [ ] Validación de dimensiones mínimas/máximas
- [ ] Eliminación automática de imagen antigua al actualizar

## 🔗 Referencias

- [SERVICIOS-RESERVAS.md](SERVICIOS-RESERVAS.md) - Documentación completa de servicios
- [SUPABASE-SETUP.md](SUPABASE-SETUP.md) - Configuración de Supabase Storage
- [ENV-SETUP.md](ENV-SETUP.md) - Variables de entorno
