#!/bin/bash

# Script de prueba para endpoints de empresas (CLIENT)
# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración
BASE_URL="${BASE_URL:-http://localhost:8080}"
JWT_TOKEN="${JWT_TOKEN:-}"

# Función para imprimir encabezados
print_header() {
    echo -e "\n${BLUE}================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================================${NC}\n"
}

# Función para imprimir resultado
print_result() {
    local status=$1
    local message=$2
    if [ $status -eq 0 ]; then
        echo -e "${GREEN}✓ $message${NC}"
    else
        echo -e "${RED}✗ $message${NC}"
    fi
}

# Verificar JWT Token
if [ -z "$JWT_TOKEN" ]; then
    echo -e "${RED}Error: JWT_TOKEN no está configurado${NC}"
    echo -e "${YELLOW}Uso: JWT_TOKEN='tu_token_aqui' $0${NC}"
    echo -e "${YELLOW}O: export JWT_TOKEN='tu_token_aqui' && $0${NC}"
    exit 1
fi

print_header "PRUEBAS DE ENDPOINTS DE EMPRESAS - ROL CLIENT"

# Test 1: Listar todas las empresas
print_header "TEST 1: Listar todas las empresas"
echo -e "${YELLOW}Endpoint:${NC} GET /api/client/servicios/empresas"
response=$(curl -s -w "\n%{http_code}" \
  -X GET "$BASE_URL/api/client/servicios/empresas" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json")

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" -eq 200 ]; then
    print_result 0 "Status: $http_code"
    echo -e "${GREEN}Response:${NC}"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
else
    print_result 1 "Status: $http_code (esperado: 200)"
    echo -e "${RED}Response:${NC}"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
fi

# Test 2: Listar empresas con paginación
print_header "TEST 2: Listar empresas con paginación (page=0, size=5)"
echo -e "${YELLOW}Endpoint:${NC} GET /api/client/servicios/empresas/paginadas?page=0&size=5&sort=nombre,asc"
response=$(curl -s -w "\n%{http_code}" \
  -X GET "$BASE_URL/api/client/servicios/empresas/paginadas?page=0&size=5&sort=nombre,asc" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json")

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" -eq 200 ]; then
    print_result 0 "Status: $http_code"
    echo -e "${GREEN}Response:${NC}"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
    
    # Extraer información de paginación
    total_elements=$(echo "$body" | jq -r '.data.totalElements // "N/A"' 2>/dev/null)
    total_pages=$(echo "$body" | jq -r '.data.totalPages // "N/A"' 2>/dev/null)
    current_page=$(echo "$body" | jq -r '.data.number // "N/A"' 2>/dev/null)
    page_size=$(echo "$body" | jq -r '.data.size // "N/A"' 2>/dev/null)
    
    echo -e "\n${BLUE}Información de Paginación:${NC}"
    echo -e "  Total elementos: $total_elements"
    echo -e "  Total páginas: $total_pages"
    echo -e "  Página actual: $current_page"
    echo -e "  Tamaño de página: $page_size"
else
    print_result 1 "Status: $http_code (esperado: 200)"
    echo -e "${RED}Response:${NC}"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
fi

# Test 3: Obtener empresa por ID (asumiendo ID 1 existe)
print_header "TEST 3: Obtener empresa por ID"
echo -e "${YELLOW}Endpoint:${NC} GET /api/client/servicios/empresas/1"
response=$(curl -s -w "\n%{http_code}" \
  -X GET "$BASE_URL/api/client/servicios/empresas/1" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json")

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" -eq 200 ]; then
    print_result 0 "Status: $http_code"
    echo -e "${GREEN}Response:${NC}"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
    
    # Extraer información de la empresa
    nombre=$(echo "$body" | jq -r '.data.nombre // "N/A"' 2>/dev/null)
    direccion=$(echo "$body" | jq -r '.data.direccion // "N/A"' 2>/dev/null)
    telefono=$(echo "$body" | jq -r '.data.telefono // "N/A"' 2>/dev/null)
    
    echo -e "\n${BLUE}Información de la Empresa:${NC}"
    echo -e "  Nombre: $nombre"
    echo -e "  Dirección: $direccion"
    echo -e "  Teléfono: $telefono"
elif [ "$http_code" -eq 404 ]; then
    print_result 0 "Status: $http_code (empresa no existe, esperado)"
    echo -e "${YELLOW}Response:${NC}"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
else
    print_result 1 "Status: $http_code (esperado: 200 o 404)"
    echo -e "${RED}Response:${NC}"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
fi

# Test 4: Obtener empresa con ID inexistente
print_header "TEST 4: Obtener empresa con ID inexistente (999999)"
echo -e "${YELLOW}Endpoint:${NC} GET /api/client/servicios/empresas/999999"
response=$(curl -s -w "\n%{http_code}" \
  -X GET "$BASE_URL/api/client/servicios/empresas/999999" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json")

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" -eq 404 ] || [ "$http_code" -eq 500 ]; then
    print_result 0 "Status: $http_code (esperado error)"
    echo -e "${GREEN}Response:${NC}"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
else
    print_result 1 "Status: $http_code (esperado: 404 o 500)"
    echo -e "${RED}Response:${NC}"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
fi

# Test 5: Acceso sin token (debe fallar con 401)
print_header "TEST 5: Acceso sin token (debe fallar)"
echo -e "${YELLOW}Endpoint:${NC} GET /api/client/servicios/empresas"
response=$(curl -s -w "\n%{http_code}" \
  -X GET "$BASE_URL/api/client/servicios/empresas" \
  -H "Content-Type: application/json")

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" -eq 401 ] || [ "$http_code" -eq 403 ]; then
    print_result 0 "Status: $http_code (esperado: no autorizado)"
    echo -e "${GREEN}Response:${NC}"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
else
    print_result 1 "Status: $http_code (esperado: 401 o 403)"
    echo -e "${RED}Response:${NC}"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
fi

# Test 6: Paginación - Segunda página
print_header "TEST 6: Paginación - Segunda página (page=1, size=3)"
echo -e "${YELLOW}Endpoint:${NC} GET /api/client/servicios/empresas/paginadas?page=1&size=3"
response=$(curl -s -w "\n%{http_code}" \
  -X GET "$BASE_URL/api/client/servicios/empresas/paginadas?page=1&size=3" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json")

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" -eq 200 ]; then
    print_result 0 "Status: $http_code"
    echo -e "${GREEN}Response:${NC}"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
else
    print_result 1 "Status: $http_code (esperado: 200)"
    echo -e "${RED}Response:${NC}"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
fi

# Test 7: Ordenamiento descendente por nombre
print_header "TEST 7: Ordenamiento descendente por nombre"
echo -e "${YELLOW}Endpoint:${NC} GET /api/client/servicios/empresas/paginadas?page=0&size=5&sort=nombre,desc"
response=$(curl -s -w "\n%{http_code}" \
  -X GET "$BASE_URL/api/client/servicios/empresas/paginadas?page=0&size=5&sort=nombre,desc" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json")

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" -eq 200 ]; then
    print_result 0 "Status: $http_code"
    echo -e "${GREEN}Response:${NC}"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
else
    print_result 1 "Status: $http_code (esperado: 200)"
    echo -e "${RED}Response:${NC}"
    echo "$body" | jq '.' 2>/dev/null || echo "$body"
fi

# Resumen final
print_header "RESUMEN DE PRUEBAS"
echo -e "${BLUE}Tests completados${NC}"
echo -e "${YELLOW}Verifica que los endpoints respondan correctamente según tu configuración${NC}"
echo -e "\n${GREEN}Nota:${NC} Si algunos tests fallan con 404, asegúrate de que existan empresas en la BD"
