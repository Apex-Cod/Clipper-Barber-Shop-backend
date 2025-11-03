# 🔧 Configuración Inicial - Módulo de Pagos

## ⚡ Setup Rápido (5 minutos)

### Paso 1: Obtener Credenciales de PayPal

1. Ve a https://developer.paypal.com/dashboard/
2. Inicia sesión con tu cuenta de PayPal (o crea una gratis)
3. Click en "My Apps & Credentials"
4. En la sección "Sandbox" (para desarrollo):
   - Si no tienes una app, click en "Create App"
   - Nombre sugerido: "Clipper-Barber-Shop-Dev"
   - Click en "Create App"
5. Copia las credenciales:
   - **Client ID**: Algo como `AaB123...`
   - **Secret**: Click en "Show" y copia el valor

### Paso 2: Configurar Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto (si no existe):

```bash
# PayPal Configuration (SANDBOX)
PAYPAL_MODE=sandbox
PAYPAL_CLIENT_ID=pega-tu-client-id-aqui
PAYPAL_CLIENT_SECRET=pega-tu-secret-aqui
PAYPAL_SUCCESS_URL=http://localhost:8080/api/payments/success
PAYPAL_CANCEL_URL=http://localhost:8080/api/payments/cancel

# Frontend URL (ajusta según tu frontend)
APP_FRONTEND_URL=http://localhost:3000
```

**⚠️ IMPORTANTE**: 
- NO subas el archivo `.env` a git
- Ya está en `.gitignore`

### Paso 3: Actualizar Base de Datos

```bash
# Opción A: Usando psql
psql -U clipper -d clipperdb -f init/migration-payments.sql

# Opción B: Usando pgAdmin u otra herramienta visual
# - Abre el archivo init/migration-payments.sql
# - Ejecuta el script completo
```

### Paso 4: Compilar y Ejecutar

```bash
# Compilar el proyecto
mvn clean install

# Ejecutar el servidor
mvn spring-boot:run
```

Si todo está correcto, verás en los logs:
```
Started ClipperBarberShopApplication in X.XXX seconds
```

### Paso 5: Verificar que Funciona

```bash
# Test básico de conexión
curl http://localhost:8080/actuator/health

# Respuesta esperada:
# {"status":"UP"}
```

## 🧪 Crear Cuentas de Prueba en PayPal

Para probar pagos, necesitas una cuenta de prueba de PayPal:

### 1. Ir a Sandbox Accounts

1. En https://developer.paypal.com/dashboard/
2. Click en "Sandbox" → "Accounts"

### 2. Crear Cuenta Personal (Comprador)

Si no tienes ninguna cuenta de prueba:

1. Click en "Create Account"
2. Tipo: **Personal** (Buyer Account)
3. Email: Se genera automáticamente (ej: `sb-xxxxx@personal.example.com`)
4. Password: Usa uno fácil como `Test1234`
5. Balance: $1000 (o lo que quieras para pruebas)
6. Click en "Create"

### 3. Ver Credenciales de la Cuenta de Prueba

1. Click en los "..." de la cuenta creada
2. Click en "View/Edit Account"
3. Tab "Funding" → verás tarjetas de prueba generadas automáticamente
4. Anota el **email** y **password**

**Estas son las credenciales que usarás para "comprar" en el flujo de pago.**

## 🎮 Flujo de Prueba Completo

### 1. Obtener Token JWT

Primero, inicia sesión como cliente:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "tu-usuario-cliente",
    "password": "tu-password"
  }'
```

Guarda el token JWT de la respuesta.

### 2. Crear una Reserva

```bash
# Asume que ya tienes una reserva creada con ID 1
# Si no, crea una primero
```

### 3. Crear el Pago

```bash
curl -X POST http://localhost:8080/api/client/pagos \
  -H "Authorization: Bearer TU_JWT_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "reservaId": 1,
    "monto": 25.50,
    "moneda": "USD",
    "descripcion": "Pago de corte de cabello"
  }'
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Pago creado exitosamente...",
  "data": {
    "id": 1,
    "approvalUrl": "https://www.sandbox.paypal.com/checkoutnow?token=EC-XXXXX",
    "estado": "PENDIENTE",
    ...
  }
}
```

### 4. Aprobar el Pago en PayPal

1. **Copia** la `approvalUrl` de la respuesta
2. **Abre** esa URL en tu navegador
3. **Inicia sesión** con tu cuenta de prueba de PayPal:
   - Email: `sb-xxxxx@personal.example.com`
   - Password: `Test1234` (o el que configuraste)
4. **Aprueba** el pago
5. PayPal te redirigirá a:
   ```
   http://localhost:8080/api/payments/success?paymentId=PAYID-XXX&token=EC-XXX&PayerID=XXX&reservaId=1
   ```
6. El backend te redirigirá automáticamente a tu frontend:
   ```
   http://localhost:3000/payment/success?paymentId=PAYID-XXX&PayerID=XXX&reservaId=1
   ```

### 5. Ejecutar el Pago

En tu frontend (o con curl), ejecuta:

```bash
curl -X POST "http://localhost:8080/api/client/pagos/execute?paymentId=PAYID-XXX&PayerID=XXX" \
  -H "Authorization: Bearer TU_JWT_TOKEN_AQUI"
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Pago completado exitosamente",
  "data": {
    "pagoId": 1,
    "estado": "COMPLETADO",
    "paypalSaleId": "SALE-XXX"
  }
}
```

### 6. Verificar el Pago

```bash
curl -X GET http://localhost:8080/api/client/pagos/1 \
  -H "Authorization: Bearer TU_JWT_TOKEN_AQUI"
```

Deberías ver el pago con estado `COMPLETADO`.

## 📊 Ver Estadísticas (Como Owner)

```bash
# Login como OWNER
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "tu-usuario-owner",
    "password": "tu-password"
  }'

# Ver estadísticas
curl -X GET http://localhost:8080/api/owner/pagos/estadisticas \
  -H "Authorization: Bearer TU_JWT_TOKEN_OWNER"
```

## ❌ Troubleshooting

### Error: "The import com.paypal cannot be resolved"

**Solución**: 
```bash
mvn clean compile
```
Esto descargará las dependencias de PayPal.

### Error: "PayPal authentication failed"

**Causas comunes**:
1. Client ID o Secret incorrectos
2. Modo incorrecto (live vs sandbox)
3. Credenciales no cargadas desde `.env`

**Solución**:
1. Verifica que el archivo `.env` existe y tiene las credenciales correctas
2. Verifica que `PAYPAL_MODE=sandbox`
3. Reinicia la aplicación

### Error: "Reserva no encontrada"

**Solución**: 
Asegúrate de que la reserva existe y pertenece al cliente autenticado:
```bash
curl -X GET http://localhost:8080/api/client/reservas \
  -H "Authorization: Bearer TU_JWT_TOKEN"
```

### El callback no funciona

**Problema**: PayPal no puede redirigir a localhost

**Solución temporal**: 
- Usa ngrok para exponer tu servidor local:
  ```bash
  ngrok http 8080
  ```
- Actualiza las URLs en PayPal config:
  ```properties
  PAYPAL_SUCCESS_URL=https://tu-url-ngrok.ngrok.io/api/payments/success
  PAYPAL_CANCEL_URL=https://tu-url-ngrok.ngrok.io/api/payments/cancel
  ```

## 🚀 Pasar a Producción

### 1. Crear App de Producción en PayPal

1. En https://developer.paypal.com/dashboard/
2. Ve a "Live" (no Sandbox)
3. Crea una nueva app
4. Completa el proceso de revisión de PayPal

### 2. Actualizar Configuración

```properties
PAYPAL_MODE=live
PAYPAL_CLIENT_ID=tu-client-id-de-produccion
PAYPAL_CLIENT_SECRET=tu-secret-de-produccion
PAYPAL_SUCCESS_URL=https://tudominio.com/api/payments/success
PAYPAL_CANCEL_URL=https://tudominio.com/api/payments/cancel
APP_FRONTEND_URL=https://tudominio.com
```

### 3. Consideraciones de Seguridad

- ✅ Usa HTTPS obligatoriamente
- ✅ Configura CORS correctamente
- ✅ Implementa rate limiting
- ✅ Monitorea transacciones
- ✅ Implementa webhooks de PayPal
- ✅ Realiza backups regulares de la BD

## 📚 Recursos Adicionales

- **Documentación completa**: `PAYMENTS-MODULE.md`
- **Guía rápida**: `PAYMENTS-QUICKSTART.md`
- **Resumen del módulo**: `PAYMENTS-SUMMARY.md`
- **Script de pruebas**: `test-payments.sh`

## 💬 ¿Necesitas Ayuda?

- Revisa los logs de la aplicación
- Consulta la documentación de PayPal: https://developer.paypal.com/docs/
- Verifica el estado de PayPal: https://status.paypal.com/

---

**¡Listo!** Ahora puedes procesar pagos con PayPal 🎉
