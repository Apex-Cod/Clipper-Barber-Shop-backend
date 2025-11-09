#!/bin/bash

# Script de pruebas para el módulo de Reseñas
# ============================================

BASE_URL="http://localhost:8080"
CLIENT_TOKEN=""
OWNER_TOKEN=""

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   TESTS - MÓDULO DE RESEÑAS${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Función para imprimir resultado
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ $2${NC}"
    else
        echo -e "${RED}✗ $2${NC}"
    fi
}

# ============================================
# 1. CONFIGURACIÓN
# ============================================
echo -e "${BLUE}1. CONFIGURACIÓN${NC}"
read -p "Token de CLIENT: " CLIENT_TOKEN
read -p "Token de OWNER: " OWNER_TOKEN
read -p "ID de Reserva COMPLETADA: " RESERVA_ID
read -p "ID de Empresa: " EMPRESA_ID
echo ""

# ============================================
# 2. TESTS PÚBLICOS (Sin autenticación)
# ============================================
echo -e "${BLUE}2. TESTS PÚBLICOS${NC}"

# Test 2.1: Listar reseñas de empresa
echo "2.1 Listar reseñas de empresa..."
RESPONSE=$(curl -s -X GET "${BASE_URL}/api/public/empresas/${EMPRESA_ID}/resenias")
echo "$RESPONSE" | jq '.' > /dev/null 2>&1
print_result $? "GET /api/public/empresas/${EMPRESA_ID}/resenias"
echo "$RESPONSE" | jq '.data | length' | xargs echo "  Reseñas encontradas:"

# Test 2.2: Estadísticas públicas
echo "2.2 Estadísticas públicas..."
RESPONSE=$(curl -s -X GET "${BASE_URL}/api/public/empresas/${EMPRESA_ID}/resenias/estadisticas")
echo "$RESPONSE" | jq '.' > /dev/null 2>&1
print_result $? "GET /api/public/empresas/${EMPRESA_ID}/resenias/estadisticas"
echo "$RESPONSE" | jq '.data.totalResenias' | xargs echo "  Total reseñas:"
echo "$RESPONSE" | jq '.data.promedioCalificacion' | xargs echo "  Promedio:"

echo ""

# ============================================
# 3. TESTS CLIENTE
# ============================================
echo -e "${BLUE}3. TESTS CLIENTE${NC}"

# Test 3.1: Crear reseña
echo "3.1 Crear reseña..."
CREATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/client/resenias" \
  -H "Authorization: Bearer ${CLIENT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"reservaId\": ${RESERVA_ID},
    \"calificacionServicio\": 5,
    \"calificacionEmpleado\": 4,
    \"comentario\": \"Excelente servicio de prueba desde script\"
  }")
echo "$CREATE_RESPONSE" | jq '.' > /dev/null 2>&1
print_result $? "POST /api/client/resenias"
RESENIA_ID=$(echo "$CREATE_RESPONSE" | jq -r '.data.id')
echo "  ID de reseña creada: $RESENIA_ID"

# Test 3.2: Obtener reseña creada
echo "3.2 Obtener reseña creada..."
RESPONSE=$(curl -s -X GET "${BASE_URL}/api/client/resenias/${RESENIA_ID}" \
  -H "Authorization: Bearer ${CLIENT_TOKEN}")
echo "$RESPONSE" | jq '.' > /dev/null 2>&1
print_result $? "GET /api/client/resenias/${RESENIA_ID}"

# Test 3.3: Actualizar reseña
echo "3.3 Actualizar reseña..."
RESPONSE=$(curl -s -X PUT "${BASE_URL}/api/client/resenias/${RESENIA_ID}" \
  -H "Authorization: Bearer ${CLIENT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "calificacionServicio": 5,
    "calificacionEmpleado": 5,
    "comentario": "Actualizado: Todo perfecto!"
  }')
echo "$RESPONSE" | jq '.' > /dev/null 2>&1
print_result $? "PUT /api/client/resenias/${RESENIA_ID}"

# Test 3.4: Listar mis reseñas
echo "3.4 Listar mis reseñas..."
RESPONSE=$(curl -s -X GET "${BASE_URL}/api/client/resenias" \
  -H "Authorization: Bearer ${CLIENT_TOKEN}")
echo "$RESPONSE" | jq '.' > /dev/null 2>&1
print_result $? "GET /api/client/resenias"
echo "$RESPONSE" | jq '.data | length' | xargs echo "  Mis reseñas:"

# Test 3.5: Obtener reseña por reserva
echo "3.5 Obtener reseña por reserva..."
RESPONSE=$(curl -s -X GET "${BASE_URL}/api/client/resenias/reserva/${RESERVA_ID}" \
  -H "Authorization: Bearer ${CLIENT_TOKEN}")
echo "$RESPONSE" | jq '.' > /dev/null 2>&1
print_result $? "GET /api/client/resenias/reserva/${RESERVA_ID}"

# Test 3.6: Intentar crear reseña duplicada (debe fallar)
echo "3.6 Intentar crear reseña duplicada..."
RESPONSE=$(curl -s -X POST "${BASE_URL}/api/client/resenias" \
  -H "Authorization: Bearer ${CLIENT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"reservaId\": ${RESERVA_ID},
    \"calificacionServicio\": 4,
    \"calificacionEmpleado\": 4,
    \"comentario\": \"Duplicado\"
  }")
if echo "$RESPONSE" | jq -r '.success' | grep -q "false"; then
    print_result 0 "Validación duplicado correcta (esperado fallo)"
else
    print_result 1 "Validación duplicado falló"
fi

echo ""

# ============================================
# 4. TESTS OWNER
# ============================================
echo -e "${BLUE}4. TESTS OWNER${NC}"

# Test 4.1: Listar todas las reseñas
echo "4.1 Listar todas las reseñas..."
RESPONSE=$(curl -s -X GET "${BASE_URL}/api/owner/resenias" \
  -H "Authorization: Bearer ${OWNER_TOKEN}")
echo "$RESPONSE" | jq '.' > /dev/null 2>&1
print_result $? "GET /api/owner/resenias"
echo "$RESPONSE" | jq '.data | length' | xargs echo "  Total reseñas:"

# Test 4.2: Reseñas paginadas
echo "4.2 Reseñas paginadas..."
RESPONSE=$(curl -s -X GET "${BASE_URL}/api/owner/resenias/paginadas?page=0&size=5" \
  -H "Authorization: Bearer ${OWNER_TOKEN}")
echo "$RESPONSE" | jq '.' > /dev/null 2>&1
print_result $? "GET /api/owner/resenias/paginadas"
echo "$RESPONSE" | jq '.data.totalElements' | xargs echo "  Total elementos:"
echo "$RESPONSE" | jq '.data.totalPages' | xargs echo "  Total páginas:"

# Test 4.3: Estadísticas de empresa
echo "4.3 Estadísticas de empresa..."
RESPONSE=$(curl -s -X GET "${BASE_URL}/api/owner/resenias/estadisticas/empresa" \
  -H "Authorization: Bearer ${OWNER_TOKEN}")
echo "$RESPONSE" | jq '.' > /dev/null 2>&1
print_result $? "GET /api/owner/resenias/estadisticas/empresa"
echo "$RESPONSE" | jq '.data.totalResenias' | xargs echo "  Total reseñas:"
echo "$RESPONSE" | jq '.data.promedioCalificacion' | xargs echo "  Promedio:"
echo "$RESPONSE" | jq '.data.resenias5Estrellas' | xargs echo "  5 estrellas:"
echo "$RESPONSE" | jq '.data.resenias4Estrellas' | xargs echo "  4 estrellas:"
echo "$RESPONSE" | jq '.data.resenias3Estrellas' | xargs echo "  3 estrellas:"

# Test 4.4: Obtener reseña por ID (owner)
echo "4.4 Obtener reseña por ID..."
RESPONSE=$(curl -s -X GET "${BASE_URL}/api/owner/resenias/${RESENIA_ID}" \
  -H "Authorization: Bearer ${OWNER_TOKEN}")
echo "$RESPONSE" | jq '.' > /dev/null 2>&1
print_result $? "GET /api/owner/resenias/${RESENIA_ID}"

echo ""

# ============================================
# 5. VALIDACIONES
# ============================================
echo -e "${BLUE}5. TESTS DE VALIDACIÓN${NC}"

# Test 5.1: Calificación inválida (0)
echo "5.1 Calificación inválida (0)..."
RESPONSE=$(curl -s -X POST "${BASE_URL}/api/client/resenias" \
  -H "Authorization: Bearer ${CLIENT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "reservaId": 99999,
    "calificacionServicio": 0,
    "comentario": "Test"
  }')
if echo "$RESPONSE" | jq -r '.success' | grep -q "false"; then
    print_result 0 "Validación calificación < 1 correcta"
else
    print_result 1 "Validación calificación < 1 falló"
fi

# Test 5.2: Calificación inválida (6)
echo "5.2 Calificación inválida (6)..."
RESPONSE=$(curl -s -X POST "${BASE_URL}/api/client/resenias" \
  -H "Authorization: Bearer ${CLIENT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "reservaId": 99999,
    "calificacionServicio": 6,
    "comentario": "Test"
  }')
if echo "$RESPONSE" | jq -r '.success' | grep -q "false"; then
    print_result 0 "Validación calificación > 5 correcta"
else
    print_result 1 "Validación calificación > 5 falló"
fi

# Test 5.3: Comentario muy largo
echo "5.3 Comentario muy largo (>1000 chars)..."
LONG_COMMENT=$(printf 'a%.0s' {1..1001})
RESPONSE=$(curl -s -X POST "${BASE_URL}/api/client/resenias" \
  -H "Authorization: Bearer ${CLIENT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"reservaId\": 99999,
    \"calificacionServicio\": 5,
    \"comentario\": \"${LONG_COMMENT}\"
  }")
if echo "$RESPONSE" | jq -r '.success' | grep -q "false"; then
    print_result 0 "Validación comentario largo correcta"
else
    print_result 1 "Validación comentario largo falló"
fi

echo ""

# ============================================
# 6. LIMPIEZA
# ============================================
echo -e "${BLUE}6. LIMPIEZA${NC}"
read -p "¿Deseas eliminar la reseña de prueba? (s/n): " DELETE_CONFIRM

if [ "$DELETE_CONFIRM" = "s" ]; then
    echo "Eliminando reseña ${RESENIA_ID}..."
    RESPONSE=$(curl -s -X DELETE "${BASE_URL}/api/client/resenias/${RESENIA_ID}" \
      -H "Authorization: Bearer ${CLIENT_TOKEN}")
    echo "$RESPONSE" | jq '.' > /dev/null 2>&1
    print_result $? "DELETE /api/client/resenias/${RESENIA_ID}"
fi

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}   TESTS COMPLETADOS${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "Para más información, consulta:"
echo "  - MODULO-RESENIAS.md"
echo "  - resenia/README.md"
