#!/bin/bash
# Script de pruebas para el sistema de verificación de email
# Clipper Barber Shop

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"
EMAIL="test$(date +%s)@example.com"  # Email único con timestamp

echo -e "${BLUE}================================${NC}"
echo -e "${BLUE}TEST: Sistema de Verificación${NC}"
echo -e "${BLUE}================================${NC}"
echo ""

# Test 1: Registrar Cliente
echo -e "${GREEN}Test 1: Registrar Cliente${NC}"
echo "Email: $EMAIL"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/registro/cliente" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test",
    "lastName": "User",
    "email": "'$EMAIL'",
    "password": "Test123"
  }')

echo "$RESPONSE" | jq '.'
echo ""

# Test 2: Intentar Login SIN Verificar (debe fallar)
echo -e "${GREEN}Test 2: Login SIN Verificar (debe fallar)${NC}"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "'$EMAIL'",
    "password": "Test123"
  }')

if echo "$RESPONSE" | grep -q "verificar tu email"; then
  echo -e "${GREEN}✅ Correcto: No permite login sin verificar${NC}"
else
  echo -e "${RED}❌ Error: Debería rechazar login${NC}"
fi
echo "$RESPONSE" | jq '.'
echo ""

# Test 3: Reenviar Email de Verificación
echo -e "${GREEN}Test 3: Reenviar Email de Verificación${NC}"
RESPONSE=$(curl -s -X POST "$BASE_URL/api/registro/resend-verification" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "'$EMAIL'"
  }')

echo "$RESPONSE" | jq '.'
echo ""

# Instrucciones para verificar manualmente
echo -e "${BLUE}================================${NC}"
echo -e "${BLUE}Próximos pasos manuales:${NC}"
echo -e "${BLUE}================================${NC}"
echo ""
echo "1. Revisa tu email: $EMAIL"
echo "2. Haz clic en el enlace de verificación"
echo "3. Después, ejecuta el siguiente comando para hacer login:"
echo ""
echo -e "${GREEN}curl -X POST $BASE_URL/api/auth/login \\${NC}"
echo -e "${GREEN}  -H \"Content-Type: application/json\" \\${NC}"
echo -e "${GREEN}  -d '{${NC}"
echo -e "${GREEN}    \"email\": \"$EMAIL\",${NC}"
echo -e "${GREEN}    \"password\": \"Test123\"${NC}"
echo -e "${GREEN}  }'${NC}"
echo ""

# Verificar en BD (requiere psql)
echo -e "${BLUE}Verificar en Base de Datos:${NC}"
echo -e "${GREEN}psql -U clipper -d clipperdb -c \"SELECT email, email_verified, activo, verification_token FROM usuarios WHERE email='$EMAIL';\"${NC}"
echo ""
