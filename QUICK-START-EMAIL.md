# 🚀 INICIO RÁPIDO - Sistema de Verificación de Email

## ⚡ Configuración en 5 Minutos

### 1️⃣ Base de Datos (1 min)

```bash
# Ejecutar migración
psql -U clipper -d clipperdb -f init/migration-email-verification.sql
```

### 2️⃣ Configurar Email (2 min)

#### Opción A: Gmail (Recomendado para desarrollo)

1. Ve a https://myaccount.google.com/apppasswords
2. Genera una contraseña de aplicación
3. Edita `src/main/resources/application.properties`:

```properties
spring.mail.username=tu-email@gmail.com
spring.mail.password=xxxx xxxx xxxx xxxx
```

#### Opción B: Mailtrap (Para testing sin enviar emails reales)

1. Regístrate en https://mailtrap.io/ (gratis)
2. Obtén credenciales SMTP
3. Edita `application.properties`:

```properties
spring.mail.host=smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=tu-username
spring.mail.password=tu-password
```

### 3️⃣ Compilar y Ejecutar (2 min)

```bash
# Compilar
./mvnw clean install -DskipTests

# Ejecutar
./mvnw spring-boot:run
```

## ✅ Verificar que Funciona

### Test Automático

```bash
# Ejecutar script de pruebas
./test-email-verification.sh
```

### Test Manual

```bash
# 1. Registrar usuario
curl -X POST http://localhost:8080/api/registro/cliente \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test",
    "lastName": "User",
    "email": "tu-email@gmail.com",
    "password": "Test123"
  }'

# 2. Revisa tu email y haz clic en el enlace

# 3. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tu-email@gmail.com",
    "password": "Test123"
  }'

# ✅ Deberías recibir un JWT token
```

## 📚 Documentación

- 📖 **Guía Completa:** [EMAIL-VERIFICATION-SYSTEM.md](EMAIL-VERIFICATION-SYSTEM.md)
- ⚙️ **Setup Detallado:** [SETUP-EMAIL-VERIFICATION.md](SETUP-EMAIL-VERIFICATION.md)
- 📊 **Resumen Visual:** [RESUMEN-EMAIL-VERIFICATION.md](RESUMEN-EMAIL-VERIFICATION.md)

## 🆘 Problemas Comunes

### Email no se envía

```bash
# Verificar logs
tail -f logs/spring.log | grep -i mail

# Verificar configuración
cat src/main/resources/application.properties | grep mail
```

### Token inválido

```sql
-- Verificar en BD
psql -U clipper -d clipperdb
SELECT email, email_verified, verification_token, verification_token_expiry 
FROM usuarios WHERE email = 'tu-email@gmail.com';
```

### Login rechazado

```bash
# Verificar estado del usuario
psql -U clipper -d clipperdb -c "
  SELECT email, email_verified, activo, deleted 
  FROM usuarios WHERE email = 'tu-email@gmail.com';
"
```

**Solución rápida (solo desarrollo):**
```sql
UPDATE usuarios 
SET email_verified = true, activo = true 
WHERE email = 'tu-email@gmail.com';
```

## 🎯 Características Implementadas

✅ Email de verificación con enlace de un solo clic  
✅ Token con expiración de 24 horas  
✅ Reenvío de email de verificación  
✅ Email de bienvenida después de verificar  
✅ Validación en login (solo usuarios verificados)  
✅ Normalización de email (case-insensitive)  
✅ Envío asíncrono (no bloquea el registro)  

## 🔗 Endpoints API

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/registro/cliente` | Registrar cliente + enviar email |
| POST | `/api/registro/empleado` | Registrar empleado + enviar email |
| POST | `/api/registro/empresa` | Registrar empresa + enviar email |
| GET | `/api/registro/verify?token=xxx` | Verificar email (un clic) |
| POST | `/api/registro/resend-verification` | Reenviar email |
| POST | `/api/auth/login` | Login (valida email verificado) |

## 💡 Flujo del Usuario

```
Registro → Email enviado → Usuario hace clic → Email verificado → Login habilitado
```

## 🎨 Frontend (Pendiente)

Crear estas páginas:
- `/verification/success` - Email verificado exitosamente
- `/verification/error` - Error al verificar email

## 📞 Soporte

Si tienes problemas:
1. Revisa la documentación completa
2. Verifica los logs de la aplicación
3. Verifica la configuración de email
4. Ejecuta `./test-email-verification.sh`

---

**¡Todo listo para usar! 🎉**

Ver documentación completa en: [EMAIL-VERIFICATION-SYSTEM.md](EMAIL-VERIFICATION-SYSTEM.md)
