# 🔧 Configuración de Variables de Entorno

## 📋 Archivos de Entorno Disponibles

El proyecto incluye tres archivos de configuración de entorno:

- **`.env`** - Configuración local activa (NO incluido en git)
- **`.env.dev`** - Plantilla para desarrollo
- **`.env.prod`** - Plantilla para producción

## 🚀 Inicio Rápido

### 1. Configuración Inicial

Copia el archivo de plantilla correspondiente:

```bash
# Para desarrollo local
cp .env.dev .env

# O para producción
cp .env.prod .env
```

### 2. Editar Variables

Abre el archivo `.env` y configura tus valores:

```bash
nano .env
# o
code .env
```

### 3. Configurar Variables Críticas

#### Base de Datos
```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=clipperdb
DB_USERNAME=clipper
DB_PASSWORD=tu_password_aqui
```

#### JWT (Seguridad)
```properties
# ⚠️ IMPORTANTE: Cambiar en producción
JWT_SECRET=tu_clave_secreta_minimo_64_caracteres_aleatorios
JWT_EXPIRATION_MS=3600000
```

#### Email (SMTP)
```properties
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=tu_app_password
```

**Nota para Gmail**: Debes generar una "App Password":
1. Ve a tu cuenta de Google > Seguridad
2. Activa "Verificación en 2 pasos"
3. Busca "Contraseñas de aplicaciones"
4. Genera una nueva para "Mail"
5. Usa esa contraseña de 16 caracteres

#### Supabase Storage
```properties
SUPABASE_URL=https://tu-proyecto.supabase.co
SUPABASE_API_KEY=tu_service_role_key_aqui
SUPABASE_BUCKET_EMPRESAS=clipper-images
SUPABASE_BUCKET_SERVICIOS=Barber-Services
```

**Obtener credenciales de Supabase**:
1. Ve a tu proyecto en [supabase.com](https://supabase.com)
2. Settings > API
3. Copia el **Project URL**
4. Copia el **service_role key** (⚠️ NO el anon key)

## 📝 Uso en Spring Boot

El proyecto está configurado para leer las variables de entorno automáticamente.

### Opción 1: Usando Spring Boot con archivo .env (Recomendado)

Agrega esta dependencia en `pom.xml`:

```xml
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>4.0.0</version>
</dependency>
```

Luego en `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=${JWT_EXPIRATION_MS}

spring.mail.host=${MAIL_HOST}
spring.mail.port=${MAIL_PORT}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

supabase.url=${SUPABASE_URL}
supabase.api-key=${SUPABASE_API_KEY}
supabase.storage.bucket.empresas=${SUPABASE_BUCKET_EMPRESAS}
supabase.storage.bucket.servicios=${SUPABASE_BUCKET_SERVICIOS}
```

### Opción 2: Variables de Entorno del Sistema

```bash
# Linux/Mac
export DB_HOST=localhost
export DB_PORT=5432
export JWT_SECRET=tu_secret

# Windows (PowerShell)
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:JWT_SECRET="tu_secret"

# Windows (CMD)
set DB_HOST=localhost
set DB_PORT=5432
set JWT_SECRET=tu_secret
```

### Opción 3: Perfil Spring

Crea archivos específicos por entorno:
- `application-dev.properties`
- `application-prod.properties`

Ejecuta con:
```bash
# Desarrollo
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Producción
java -jar app.jar --spring.profiles.active=prod
```

## 🐳 Docker y Docker Compose

### docker-compose.yml

```yaml
version: '3.8'
services:
  app:
    image: clipper-barber-shop
    env_file:
      - .env
    environment:
      - DB_HOST=${DB_HOST}
      - DB_PORT=${DB_PORT}
      - JWT_SECRET=${JWT_SECRET}
    ports:
      - "8080:8080"
```

Ejecutar:
```bash
docker-compose --env-file .env up
```

## ☁️ Despliegue en Producción

### Railway / Render / Heroku

Configura las variables de entorno en el panel de configuración:

1. Ve a Settings > Environment Variables
2. Agrega cada variable manualmente:
   - `DB_HOST`
   - `DB_PORT`
   - `JWT_SECRET`
   - etc.

### AWS Elastic Beanstalk

```bash
eb setenv DB_HOST=your-db-host \
         DB_PORT=5432 \
         JWT_SECRET=your-secret
```

### Kubernetes

Crea un Secret:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: clipper-secrets
type: Opaque
stringData:
  DB_HOST: your-db-host
  JWT_SECRET: your-secret
  SUPABASE_API_KEY: your-key
```

Referencia en el Deployment:

```yaml
envFrom:
  - secretRef:
      name: clipper-secrets
```

## 🔒 Seguridad

### ⚠️ NUNCA hacer:
- ❌ Subir `.env` con valores reales a Git
- ❌ Compartir el `JWT_SECRET` públicamente
- ❌ Usar el `service_role` key de Supabase en el frontend
- ❌ Hardcodear contraseñas en el código
- ❌ Usar `hibernate.ddl-auto=create` en producción

### ✅ Siempre hacer:
- ✅ Agregar `.env` al `.gitignore`
- ✅ Usar contraseñas fuertes (min 16 caracteres)
- ✅ Rotar secrets periódicamente
- ✅ Usar HTTPS en producción
- ✅ Habilitar SSL en la base de datos
- ✅ Usar secrets managers (AWS Secrets Manager, Vault, etc.)

## 🧪 Verificar Configuración

### Script de Verificación

Crea un archivo `check-env.sh`:

```bash
#!/bin/bash

echo "🔍 Verificando variables de entorno..."

required_vars=(
  "DB_HOST"
  "DB_PORT"
  "DB_NAME"
  "JWT_SECRET"
  "SUPABASE_URL"
  "SUPABASE_API_KEY"
)

missing=0
for var in "${required_vars[@]}"; do
  if [ -z "${!var}" ]; then
    echo "❌ Falta: $var"
    ((missing++))
  else
    echo "✅ Configurado: $var"
  fi
done

if [ $missing -eq 0 ]; then
  echo "✅ Todas las variables están configuradas"
  exit 0
else
  echo "❌ Faltan $missing variables"
  exit 1
fi
```

Ejecutar:
```bash
source .env && bash check-env.sh
```

## 📚 Referencias

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [12 Factor App - Config](https://12factor.net/config)
- [Supabase API Keys](https://supabase.com/docs/guides/api#api-keys)
- [Docker Environment Variables](https://docs.docker.com/compose/environment-variables/)

## 🆘 Troubleshooting

### Error: "Could not resolve placeholder"
```
Caused by: java.lang.IllegalArgumentException: Could not resolve placeholder 'DB_HOST'
```

**Solución**: 
- Verifica que el archivo `.env` existe
- Verifica que las variables están exportadas
- Usa `spring-dotenv` dependency

### Error: "Unable to create initial connections of pool"
```
HikariPool: Exception during pool initialization
```

**Solución**:
- Verifica `DB_HOST`, `DB_PORT`, `DB_NAME`
- Verifica que PostgreSQL está corriendo
- Verifica usuario y contraseña

### Error: "Invalid signature" (JWT)
```
io.jsonwebtoken.security.SignatureException: Invalid signature
```

**Solución**:
- El `JWT_SECRET` cambió
- Regenera todos los tokens

## 📞 Soporte

Si tienes problemas con la configuración:
1. Revisa los logs: `tail -f logs/spring.log`
2. Verifica las variables: `echo $DB_HOST`
3. Consulta la documentación oficial de Spring Boot
