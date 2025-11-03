# Quick Start - Módulo de Pagos con PayPal

## ⚡ Inicio Rápido

### 1. Configuración de PayPal Sandbox

1. Ve a https://developer.paypal.com/dashboard/
2. Inicia sesión con tu cuenta de PayPal
3. Ve a "My Apps & Credentials"
4. En "Sandbox", crea una nueva App o usa una existente
5. Copia el **Client ID** y **Secret**

### 2. Configurar Variables de Entorno

Crea o actualiza tu archivo `.env`:

```bash
# PayPal Configuration
PAYPAL_MODE=sandbox
PAYPAL_CLIENT_ID=tu-client-id-de-paypal
PAYPAL_CLIENT_SECRET=tu-secret-de-paypal
PAYPAL_SUCCESS_URL=http://localhost:8080/api/payments/success
PAYPAL_CANCEL_URL=http://localhost:8080/api/payments/cancel

# Frontend URL
APP_FRONTEND_URL=http://localhost:3000
```

### 3. Ejecutar Migración de Base de Datos

```bash
psql -U clipper -d clipperdb -f init/migration-payments.sql
```

O ejecuta el script directamente en tu gestor de BD.

### 4. Compilar y Ejecutar

```bash
mvn clean install
mvn spring-boot:run
```

### 5. Probar el Módulo

#### Crear un Pago (Cliente)

```bash
curl -X POST http://localhost:8080/api/client/pagos \
  -H "Authorization: Bearer {tu-jwt-token}" \
  -H "Content-Type: application/json" \
  -d '{
    "reservaId": 1,
    "monto": 25.50,
    "moneda": "USD",
    "descripcion": "Pago de corte de cabello"
  }'
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "approvalUrl": "https://www.sandbox.paypal.com/checkoutnow?token=EC-xxxxx"
  }
}
```

#### Abrir approvalUrl en el navegador
El usuario aprueba el pago y PayPal redirige a:
```
http://localhost:8080/api/payments/success?paymentId=xxx&PayerID=xxx&reservaId=1
```

#### Ejecutar el Pago

```bash
curl -X POST "http://localhost:8080/api/client/pagos/execute?paymentId=PAYID-xxx&PayerID=PAYER-xxx" \
  -H "Authorization: Bearer {tu-jwt-token}"
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "estado": "COMPLETADO",
    "paypalSaleId": "SALE-123456"
  }
}
```

## 🧪 Usuarios de Prueba de PayPal

En Sandbox, usa las cuentas de prueba que PayPal proporciona:

- **Email**: sb-xxxxx@personal.example.com (proporcionado en el dashboard)
- **Password**: (proporcionado en el dashboard)

## 📊 Consultar Estadísticas (Owner)

```bash
curl -X GET http://localhost:8080/api/owner/pagos/estadisticas \
  -H "Authorization: Bearer {tu-jwt-token-owner}"
```

## 🔍 Endpoints Disponibles

### Cliente (CLIENT)
- `POST /api/client/pagos` - Crear pago
- `POST /api/client/pagos/execute` - Ejecutar pago
- `GET /api/client/pagos` - Listar mis pagos
- `GET /api/client/pagos/{id}` - Ver pago
- `GET /api/client/pagos/reserva/{reservaId}` - Ver pago de reserva
- `DELETE /api/client/pagos/{id}` - Cancelar pago

### Dueño (OWNER)
- `GET /api/owner/pagos` - Listar todos los pagos
- `GET /api/owner/pagos/paginados` - Listar paginado
- `GET /api/owner/pagos/estado/{estado}` - Filtrar por estado
- `GET /api/owner/pagos/estadisticas` - Estadísticas
- `GET /api/owner/pagos/{id}` - Ver pago
- `DELETE /api/owner/pagos/{id}` - Eliminar pago

### Callbacks (Público)
- `GET /api/payments/success` - Callback de éxito
- `GET /api/payments/cancel` - Callback de cancelación

## 📖 Documentación Completa

Ver `PAYMENTS-MODULE.md` para documentación detallada.

## ⚠️ Notas Importantes

1. **Sandbox Mode**: Usar solo para desarrollo/pruebas
2. **HTTPS**: Requerido en producción
3. **Validaciones**: Siempre validar que la reserva pertenece al usuario
4. **Estados**: Un pago solo puede ejecutarse si está PENDIENTE
5. **Webhooks**: Considerar implementar webhooks de PayPal para notificaciones asíncronas

## 🚀 Producción

Para pasar a producción:

1. Cambia `PAYPAL_MODE=live`
2. Usa credenciales de producción
3. Actualiza URLs de callback a tu dominio real
4. Configura HTTPS
5. Implementa webhooks de PayPal

## 📞 Soporte

- Documentación: `PAYMENTS-MODULE.md`
- PayPal Docs: https://developer.paypal.com/docs/
- Issues: GitHub Issues del proyecto
