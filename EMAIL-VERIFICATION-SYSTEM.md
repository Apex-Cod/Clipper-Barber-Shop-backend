# Sistema de Verificación de Email con Token

## 📧 Descripción General

Sistema robusto de verificación de correo electrónico implementado para el módulo de registro de Clipper Barber Shop. Los usuarios deben verificar su email antes de poder iniciar sesión.

## 🎯 Características

- ✅ **Generación automática de token** al registrarse
- ✅ **Envío de email** con enlace de verificación
- ✅ **Verificación con un solo clic** - el usuario solo necesita hacer clic en el enlace
- ✅ **Token con expiración** (24 horas)
- ✅ **Reenvío de email** de verificación
- ✅ **Email de bienvenida** después de verificar
- ✅ **Validación en login** - solo usuarios verificados pueden iniciar sesión
- ✅ **Envío asíncrono** de emails (no bloquea el registro)

## 🏗️ Arquitectura

### Componentes Principales

1. **EmailService** - Servicio para envío de correos
2. **VerifyEmailService** - Lógica de verificación
3. **RegistroServiceImpl** - Generación de tokens al registrar
4. **AuthService** - Validación de email verificado en login
5. **RegistroController** - Endpoints de verificación

### Flujo de Registro y Verificación

```
1. Usuario se registra
   ↓
2. Sistema genera token UUID
   ↓
3. Usuario guardado con emailVerified=false, activo=false
   ↓
4. Email enviado (asíncrono) con enlace de verificación
   ↓
5. Usuario hace clic en el enlace del email
   ↓
6. GET /api/registro/verify?token=xxx
   ↓
7. Sistema valida token y expiration
   ↓
8. Usuario marcado como emailVerified=true, activo=true
   ↓
9. Email de bienvenida enviado
   ↓
10. Usuario puede hacer login
```

## 📝 Cambios en la Base de Datos

### Nueva Tabla: `usuarios`

```sql
-- Nuevas columnas agregadas
email_verified BOOLEAN DEFAULT FALSE NOT NULL
verification_token VARCHAR(255)
verification_token_expiry TIMESTAMP

-- Índice para performance
CREATE INDEX idx_usuarios_verification_token ON usuarios(verification_token)
```

### Migración

Ejecutar el script: `init/migration-email-verification.sql`

```bash
psql -U clipper -d clipperdb -f init/migration-email-verification.sql
```

## 🔧 Configuración

### 1. Dependencias (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

### 2. Propiedades (application.properties)

```properties
# Configuración de Email (Gmail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# URLs de la aplicación
app.base-url=http://localhost:8080
app.frontend-url=http://localhost:3000
```

### 3. Obtener App Password de Gmail

1. Ve a tu cuenta de Google: https://myaccount.google.com/
2. Seguridad → Verificación en dos pasos (activar si no está activo)
3. Contraseñas de aplicaciones
4. Selecciona "Correo" y "Otro dispositivo"
5. Genera la contraseña y úsala en `spring.mail.password`

## 📡 Endpoints API

### 1. Registrar Usuario

**POST** `/api/registro/cliente`

```json
{
  "name": "Juan",
  "lastName": "Pérez",
  "email": "juan@example.com",
  "password": "Password123"
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Cliente registrado. Por favor, verifica tu email para activar tu cuenta.",
  "data": null
}
```

### 2. Verificar Email (GET)

**GET** `/api/registro/verify?token={uuid-token}`

- Usuario hace clic en el enlace del email
- Sistema verifica automáticamente
- Redirige a: 
  - `http://localhost:3000/verification/success` (éxito)
  - `http://localhost:3000/verification/error?message=...` (error)

### 3. Reenviar Email de Verificación

**POST** `/api/registro/resend-verification`

```json
{
  "email": "juan@example.com"
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Email de verificación enviado. Por favor, revisa tu bandeja de entrada.",
  "data": null
}
```

### 4. Login (Validación)

**POST** `/api/auth/login`

```json
{
  "email": "juan@example.com",
  "password": "Password123"
}
```

**Errores posibles:**
- `"Debes verificar tu email antes de iniciar sesión. Revisa tu bandeja de entrada."` - Email no verificado
- `"Usuario no encontrado"` - Credenciales inválidas
- `"Credenciales inválidas"` - Contraseña incorrecta

## 📧 Plantillas de Email

### Email de Verificación

- **Asunto:** "Verifica tu cuenta - Clipper Barber Shop"
- **Contenido:** HTML con botón de verificación
- **Botón:** "Verificar Email" → `{base-url}/api/registro/verify?token={token}`
- **Expiración:** 24 horas

### Email de Bienvenida

- **Asunto:** "¡Bienvenido a Clipper Barber Shop!"
- **Contenido:** Mensaje de bienvenida con información de la plataforma
- **Enviado:** Después de verificar exitosamente

## 🧪 Pruebas

### Caso 1: Registro y Verificación Exitosa

```bash
# 1. Registrar usuario
curl -X POST http://localhost:8080/api/registro/cliente \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "password": "Test123"
  }'

# 2. Revisar email y hacer clic en el enlace
# El enlace será algo como:
# http://localhost:8080/api/registro/verify?token=abc-123-def-456

# 3. Intentar login (debería funcionar)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123"
  }'
```

### Caso 2: Login Sin Verificar Email

```bash
# 1. Registrar usuario
curl -X POST http://localhost:8080/api/registro/cliente \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test2",
    "lastName": "User2",
    "email": "test2@example.com",
    "password": "Test123"
  }'

# 2. Intentar login sin verificar (debería fallar)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test2@example.com",
    "password": "Test123"
  }'

# Respuesta esperada:
# "Debes verificar tu email antes de iniciar sesión. Revisa tu bandeja de entrada."
```

### Caso 3: Reenviar Email de Verificación

```bash
curl -X POST http://localhost:8080/api/registro/resend-verification \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'
```

### Caso 4: Token Expirado

```bash
# Si el token expiró (después de 24 horas), al hacer clic en el enlace:
# Error: "El token de verificación ha expirado. Por favor, solicita uno nuevo."

# Solución: Reenviar email de verificación
curl -X POST http://localhost:8080/api/registro/resend-verification \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'
```

## 🔒 Seguridad

### Tokens

- **Formato:** UUID v4 (universally unique identifier)
- **Almacenamiento:** En base de datos (plain text, no es sensible como password)
- **Expiración:** 24 horas
- **Uso único:** El token se elimina después de usarse
- **No reutilizable:** Cada reenvío genera un nuevo token

### Validaciones

1. ✅ Token debe existir en BD
2. ✅ Token no debe estar expirado
3. ✅ Usuario no debe estar eliminado
4. ✅ Email no puede verificarse dos veces con el mismo token
5. ✅ Usuario debe tener email verificado para hacer login

## 🎨 Frontend (Recomendaciones)

### Páginas a Crear

1. **Página de Verificación Exitosa** (`/verification/success`)
   - Mensaje: "¡Tu email ha sido verificado exitosamente!"
   - Botón: "Ir al Login"

2. **Página de Error de Verificación** (`/verification/error`)
   - Mostrar mensaje de error de query param
   - Botón: "Reenviar Email de Verificación"

3. **Página de Login** - Agregar:
   - Mensaje si el login falla por email no verificado
   - Link: "¿No recibiste el email de verificación?"

### Componente de Reenvío

```jsx
// Ejemplo conceptual
function ResendVerificationButton({ email }) {
  const handleResend = async () => {
    await fetch('/api/registro/resend-verification', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email })
    });
    alert('Email de verificación enviado!');
  };
  
  return <button onClick={handleResend}>Reenviar Email</button>;
}
```

## 📊 Estados del Usuario

| Estado | email_verified | activo | deleted | Puede Login |
|--------|---------------|--------|---------|-------------|
| Registrado (pendiente) | false | false | false | ❌ |
| Verificado | true | true | false | ✅ |
| Inactivo | true | false | false | ❌ |
| Eliminado | * | false | true | ❌ |

## 🐛 Troubleshooting

### Email no se envía

1. Verificar credenciales de Gmail en `application.properties`
2. Verificar que la cuenta tiene verificación en 2 pasos activada
3. Verificar la contraseña de aplicación
4. Revisar logs: `logging.level.apex.code.clipperBarberShop.shared.email=DEBUG`

### Token inválido

- Verificar que el token en la URL esté completo
- Verificar que no haya espacios o caracteres especiales
- Verificar que no haya expirado (24 horas)

### Usuario no puede hacer login

1. Verificar que `email_verified = true` en la BD
2. Verificar que `activo = true`
3. Verificar que `deleted = false`

## 🚀 Despliegue a Producción

### Checklist

- [ ] Configurar SMTP real (no Gmail personal)
- [ ] Usar servicio de email transaccional (SendGrid, AWS SES, Mailgun)
- [ ] Actualizar `app.base-url` con dominio real
- [ ] Actualizar `app.frontend-url` con dominio real
- [ ] Habilitar HTTPS
- [ ] Configurar límite de rate en endpoints de email
- [ ] Agregar logs y monitoreo
- [ ] Agregar métricas (emails enviados, tasa de verificación)

### Servicios de Email Recomendados

1. **SendGrid** - Gratuito hasta 100 emails/día
2. **AWS SES** - $0.10 por 1000 emails
3. **Mailgun** - Gratuito hasta 5000 emails/mes
4. **Postmark** - Enfocado en emails transaccionales

## 📈 Mejoras Futuras

- [ ] Rate limiting para evitar spam
- [ ] Captcha en registro
- [ ] Logs de auditoría (quién se registró, cuándo verificó)
- [ ] Notificación al admin cuando se registra un nuevo usuario
- [ ] Templates de email personalizables
- [ ] Soporte para múltiples idiomas en emails
- [ ] Dashboard de métricas de verificación
- [ ] Recuperación de contraseña con email

## 📁 Archivos Modificados/Creados

### Nuevos Archivos
- `shared/email/EmailService.java`
- `shared/email/EmailServiceImpl.java`
- `register/domain/port/in/VerifyEmailUseCase.java`
- `register/domain/port/out/EmailVerificationRepositoryPort.java`
- `register/application/service/impl/VerifyEmailService.java`
- `register/application/dto/ResendVerificationRequest.java`
- `init/migration-email-verification.sql`

### Archivos Modificados
- `Entities/Usuario.java` - Agregados campos de verificación
- `ClipperBarberShopApplication.java` - Habilitado @EnableAsync
- `register/application/service/impl/RegistroServiceImpl.java` - Generación de tokens y envío de emails
- `register/adapters/in/web/RegistroController.java` - Endpoints de verificación
- `register/adapters/out/persistence/SpringDataUsuarioRepository.java` - Método findByVerificationToken
- `register/adapters/out/persistence/UsuarioPersistenceAdapter.java` - Implementación EmailVerificationRepositoryPort
- `auth/application/service/AuthService.java` - Validación de email verificado
- `pom.xml` - Dependencia spring-boot-starter-mail
- `application.properties` - Configuración de email

## 📞 Soporte

Para problemas o preguntas sobre este sistema:
1. Revisar esta documentación
2. Revisar logs de la aplicación
3. Verificar configuración de email
4. Contactar al equipo de desarrollo
