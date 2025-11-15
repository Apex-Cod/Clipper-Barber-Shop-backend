#!/bin/bash

# Script de prueba para endpoints de reportes
# Requiere: jq (JSON processor)

BASE_URL="http://localhost:8080/api"
EMPRESA_ID=1
FECHA_INICIO="2024-01-01"
FECHA_FIN="2024-12-31"

echo "=== Test Módulo de Reportes ==="
echo ""

# Paso 1: Login como OWNER
echo "1. Autenticando como OWNER..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "owner@example.com",
    "password": "password123"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token')

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
  echo "❌ Error: No se pudo obtener el token"
  echo "Respuesta: $LOGIN_RESPONSE"
  exit 1
fi

echo "✅ Token obtenido"
echo ""

# Paso 2: Reporte de Ingresos
echo "2. Probando reporte de ingresos..."
INGRESOS=$(curl -s -X GET "$BASE_URL/reportes/ingresos?empresaId=$EMPRESA_ID&fechaInicio=$FECHA_INICIO&fechaFin=$FECHA_FIN" \
  -H "Authorization: Bearer $TOKEN")

echo "$INGRESOS" | jq '.'
echo ""

# Paso 3: Reporte de Servicios
echo "3. Probando reporte de servicios..."
SERVICIOS=$(curl -s -X GET "$BASE_URL/reportes/servicios?empresaId=$EMPRESA_ID&fechaInicio=$FECHA_INICIO&fechaFin=$FECHA_FIN" \
  -H "Authorization: Bearer $TOKEN")

echo "$SERVICIOS" | jq '.'
echo ""

# Paso 4: Reporte de Empleados
echo "4. Probando reporte de empleados..."
EMPLEADOS=$(curl -s -X GET "$BASE_URL/reportes/empleados?empresaId=$EMPRESA_ID&fechaInicio=$FECHA_INICIO&fechaFin=$FECHA_FIN" \
  -H "Authorization: Bearer $TOKEN")

echo "$EMPLEADOS" | jq '.'
echo ""

# Paso 5: Reporte de Clientes
echo "5. Probando reporte de clientes..."
CLIENTES=$(curl -s -X GET "$BASE_URL/reportes/clientes?empresaId=$EMPRESA_ID&fechaInicio=$FECHA_INICIO&fechaFin=$FECHA_FIN" \
  -H "Authorization: Bearer $TOKEN")

echo "$CLIENTES" | jq '.'
echo ""

# Paso 6: Reporte Consolidado
echo "6. Probando reporte consolidado..."
CONSOLIDADO=$(curl -s -X GET "$BASE_URL/reportes/consolidado?empresaId=$EMPRESA_ID&fechaInicio=$FECHA_INICIO&fechaFin=$FECHA_FIN" \
  -H "Authorization: Bearer $TOKEN")

echo "$CONSOLIDADO" | jq '.'
echo ""

# Paso 7: Test de seguridad - Intentar acceder con otro owner
echo "7. Probando seguridad (acceso no autorizado)..."
UNAUTHORIZED=$(curl -s -X GET "$BASE_URL/reportes/consolidado?empresaId=999&fechaInicio=$FECHA_INICIO&fechaFin=$FECHA_FIN" \
  -H "Authorization: Bearer $TOKEN")

echo "$UNAUTHORIZED" | jq '.'
echo ""

# Paso 8: Test sin autenticación
echo "8. Probando sin autenticación..."
NO_AUTH=$(curl -s -X GET "$BASE_URL/reportes/consolidado?empresaId=$EMPRESA_ID&fechaInicio=$FECHA_INICIO&fechaFin=$FECHA_FIN")

echo "$NO_AUTH" | jq '.'
echo ""

echo "=== Pruebas Completadas ==="
