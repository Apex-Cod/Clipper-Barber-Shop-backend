#!/bin/bash

# Script de pruebas para el módulo de Pagos con PayPal
# Asegúrate de tener el servidor corriendo en http://localhost:8080

echo "🧪 Script de Pruebas - Módulo de Pagos con PayPal"
echo "=================================================="
echo ""

# Configuración
BASE_URL="http://localhost:8080"
CLIENT_TOKEN="tu-jwt-token-client-aqui"
OWNER_TOKEN="tu-jwt-token-owner-aqui"

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para hacer requests
make_request() {
    local method=$1
    local endpoint=$2
    local token=$3
    local data=$4
    
    echo -e "${BLUE}➤ $method $endpoint${NC}"
    
    if [ -n "$data" ]; then
        response=$(curl -s -X $method "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d "$data")
    else
        response=$(curl -s -X $method "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $token")
    fi
    
    echo "$response" | jq '.' 2>/dev/null || echo "$response"
    echo ""
}

# Test 1: Crear un pago (CLIENT)
echo -e "${GREEN}Test 1: Crear un pago para una reserva${NC}"
echo "========================================"
PAGO_DATA='{
  "reservaId": 1,
  "monto": 25.50,
  "moneda": "USD",
  "descripcion": "Pago de corte de cabello"
}'
make_request "POST" "/api/client/pagos" "$CLIENT_TOKEN" "$PAGO_DATA"
echo -e "${BLUE}📝 Copia el 'approvalUrl' y ábrelo en el navegador para aprobar el pago${NC}"
echo ""

# Test 2: Listar pagos del cliente (CLIENT)
echo -e "${GREEN}Test 2: Listar todos los pagos del cliente${NC}"
echo "==========================================="
make_request "GET" "/api/client/pagos" "$CLIENT_TOKEN"

# Test 3: Obtener pago de una reserva (CLIENT)
echo -e "${GREEN}Test 3: Obtener pago de una reserva específica${NC}"
echo "==============================================="
make_request "GET" "/api/client/pagos/reserva/1" "$CLIENT_TOKEN"

# Test 4: Obtener pago por ID (CLIENT)
echo -e "${GREEN}Test 4: Obtener detalles de un pago${NC}"
echo "===================================="
make_request "GET" "/api/client/pagos/1" "$CLIENT_TOKEN"

# Test 5: Listar todos los pagos (OWNER)
echo -e "${GREEN}Test 5: Listar todos los pagos de la empresa (OWNER)${NC}"
echo "===================================================="
make_request "GET" "/api/owner/pagos" "$OWNER_TOKEN"

# Test 6: Obtener estadísticas (OWNER)
echo -e "${GREEN}Test 6: Obtener estadísticas de pagos (OWNER)${NC}"
echo "============================================="
make_request "GET" "/api/owner/pagos/estadisticas" "$OWNER_TOKEN"

# Test 7: Listar pagos completados (OWNER)
echo -e "${GREEN}Test 7: Listar pagos completados (OWNER)${NC}"
echo "========================================"
make_request "GET" "/api/owner/pagos/estado/COMPLETADO" "$OWNER_TOKEN"

# Test 8: Listar pagos pendientes (OWNER)
echo -e "${GREEN}Test 8: Listar pagos pendientes (OWNER)${NC}"
echo "======================================"
make_request "GET" "/api/owner/pagos/estado/PENDIENTE" "$OWNER_TOKEN"

# Test 9: Listar pagos paginados (OWNER)
echo -e "${GREEN}Test 9: Listar pagos paginados (OWNER)${NC}"
echo "======================================"
make_request "GET" "/api/owner/pagos/paginados?page=0&size=10" "$OWNER_TOKEN"

echo ""
echo -e "${GREEN}✅ Pruebas completadas${NC}"
echo ""
echo -e "${BLUE}📋 Notas:${NC}"
echo "1. Reemplaza CLIENT_TOKEN y OWNER_TOKEN con tokens JWT válidos"
echo "2. Asegúrate de tener reservas existentes en la base de datos"
echo "3. Para probar la ejecución de pagos:"
echo "   - Crea un pago (Test 1)"
echo "   - Abre el approvalUrl en el navegador"
echo "   - Aprueba el pago con tu cuenta de prueba de PayPal"
echo "   - PayPal redirigirá a /api/payments/success"
echo "   - Ejecuta: curl -X POST 'http://localhost:8080/api/client/pagos/execute?paymentId=PAYID-xxx&PayerID=PAYER-xxx' -H 'Authorization: Bearer \$CLIENT_TOKEN'"
echo ""
echo -e "${BLUE}🔧 Configuración de PayPal:${NC}"
echo "1. Obtén tus credenciales en: https://developer.paypal.com/dashboard/"
echo "2. Configura PAYPAL_CLIENT_ID y PAYPAL_CLIENT_SECRET en application.properties"
echo "3. Usa PAYPAL_MODE=sandbox para pruebas"
echo ""
echo -e "${BLUE}👤 Usuarios de prueba de PayPal:${NC}"
echo "Crea cuentas de prueba en: https://developer.paypal.com/dashboard/accounts"
