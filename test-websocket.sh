#!/bin/bash

# 🧪 Script de Prueba para WebSocket
# Uso: ./test-websocket.sh

# Colores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}🔔 WebSocket Test Suite${NC}"
echo -e "${BLUE}========================================${NC}\n"

# 1. Health Check
echo -e "${YELLOW}1. Health Check...${NC}"
curl -s "${BASE_URL}/api/test/websocket/health" | jq '.' 2>/dev/null || curl -s "${BASE_URL}/api/test/websocket/health"
echo -e "\n"

# 2. Enviar Notificación Global
echo -e "${YELLOW}2. Enviando notificación global...${NC}"
RESPONSE=$(curl -s "${BASE_URL}/api/test/websocket/send-global")
echo -e "${GREEN}✓${NC} ${RESPONSE}\n"

# 3. Enviar Notificación a Usuario
echo -e "${YELLOW}3. Enviando notificación a usuario 'user-123'...${NC}"
RESPONSE=$(curl -s "${BASE_URL}/api/test/websocket/send-user?userId=user-123")
echo -e "${GREEN}✓${NC} ${RESPONSE}\n"

# 4. Enviar Notificación a Empresa
echo -e "${YELLOW}4. Enviando notificación a empresa '1'...${NC}"
RESPONSE=$(curl -s "${BASE_URL}/api/test/websocket/send-empresa?empresaId=1")
echo -e "${GREEN}✓${NC} ${RESPONSE}\n"

# 5. Simular Creación de Reserva
echo -e "${YELLOW}5. Simulando creación de reserva...${NC}"
RESPONSE=$(curl -s "${BASE_URL}/api/test/websocket/simulate-reserva?empresaId=1&clientId=user-123&ownerId=owner-456")
echo -e "${GREEN}✓${NC} ${RESPONSE}\n"

echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}✅ Todas las pruebas completadas${NC}"
echo -e "${BLUE}========================================${NC}\n"

echo -e "${YELLOW}📱 Verifica tu app de Expo para ver las notificaciones${NC}\n"

echo -e "${BLUE}Endpoints disponibles:${NC}"
echo -e "  • Health: ${BASE_URL}/api/test/websocket/health"
echo -e "  • Global: ${BASE_URL}/api/test/websocket/send-global"
echo -e "  • Usuario: ${BASE_URL}/api/test/websocket/send-user?userId=X"
echo -e "  • Empresa: ${BASE_URL}/api/test/websocket/send-empresa?empresaId=X"
echo -e "  • Simular: ${BASE_URL}/api/test/websocket/simulate-reserva\n"
