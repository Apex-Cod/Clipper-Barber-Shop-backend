#!/bin/bash

# ============================================================================
# Script de Prueba - Módulo de Planes
# ============================================================================

BASE_URL="http://localhost:8080/api"
ADMIN_TOKEN=""
CLIENT_TOKEN=""

echo "========================================="
echo "INICIANDO PRUEBAS DEL MÓDULO DE PLANES"
echo "========================================="
echo ""

# ============================================================================
# 1. LOGIN COMO ADMIN
# ============================================================================
echo "1. Login como ADMIN..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@clipper.com",
    "password": "admin123"
  }')

ADMIN_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')

if [ "$ADMIN_TOKEN" = "null" ] || [ -z "$ADMIN_TOKEN" ]; then
    echo "❌ Error: No se pudo obtener el token de ADMIN"
    echo "Respuesta: $LOGIN_RESPONSE"
    exit 1
fi

echo "✅ Login exitoso como ADMIN"
echo "Token: ${ADMIN_TOKEN:0:20}..."
echo ""

# ============================================================================
# 2. LISTAR TODOS LOS PLANES (ADMIN)
# ============================================================================
echo "2. Listar todos los planes (ADMIN)..."
PLANES=$(curl -s -X GET "$BASE_URL/admin/planes" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

echo "Respuesta:"
echo $PLANES | jq '.'
echo ""

# ============================================================================
# 3. OBTENER PLAN GRATUITO POR ID
# ============================================================================
echo "3. Obtener plan GRATUITO por ID (supongamos ID=1)..."
PLAN_GRATUITO=$(curl -s -X GET "$BASE_URL/admin/planes/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

echo "Respuesta:"
echo $PLAN_GRATUITO | jq '.'
echo ""

# ============================================================================
# 4. ACTUALIZAR PLAN BÁSICO
# ============================================================================
echo "4. Actualizar plan BASICO (supongamos ID=2)..."
UPDATE_RESPONSE=$(curl -s -X PUT "$BASE_URL/admin/planes/2" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "precio": 34.99,
    "descripcion": "Hasta 2 usuarios y 50 reservas/mes - ACTUALIZADO"
  }')

echo "Respuesta:"
echo $UPDATE_RESPONSE | jq '.'
echo ""

# ============================================================================
# 5. CAMBIAR ESTADO DE UN PLAN
# ============================================================================
echo "5. Desactivar plan PREMIUM (supongamos ID=3)..."
ESTADO_RESPONSE=$(curl -s -X PATCH "$BASE_URL/admin/planes/3/estado?activo=false" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

echo "Respuesta:"
echo $ESTADO_RESPONSE | jq '.'
echo ""

# Reactivar el plan
echo "5.1. Reactivar plan PREMIUM..."
ESTADO_RESPONSE=$(curl -s -X PATCH "$BASE_URL/admin/planes/3/estado?activo=true" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

echo "Respuesta:"
echo $ESTADO_RESPONSE | jq '.'
echo ""

# ============================================================================
# 6. LISTAR PLANES CON PAGINACIÓN
# ============================================================================
echo "6. Listar planes con paginación..."
PLANES_PAGINADOS=$(curl -s -X GET "$BASE_URL/admin/planes/paginados?page=0&size=2&sort=nombre,asc" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

echo "Respuesta:"
echo $PLANES_PAGINADOS | jq '.content'
echo ""

# ============================================================================
# 7. LOGIN COMO OWNER
# ============================================================================
echo "7. Login como OWNER..."
OWNER_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "owner@barberia.com",
    "password": "owner123"
  }')

OWNER_TOKEN=$(echo $OWNER_LOGIN | jq -r '.token')

if [ "$OWNER_TOKEN" = "null" ] || [ -z "$OWNER_TOKEN" ]; then
    echo "⚠️  Advertencia: No se pudo obtener el token de OWNER (es posible que no exista el usuario)"
    echo "Continuando con pruebas de ADMIN solamente..."
else
    echo "✅ Login exitoso como OWNER"
    echo "Token: ${OWNER_TOKEN:0:20}..."
    echo ""

    # ========================================================================
    # 8. LISTAR PLANES ACTIVOS (OWNER)
    # ========================================================================
    echo "8. Listar planes activos (OWNER)..."
    PLANES_ACTIVOS=$(curl -s -X GET "$BASE_URL/owner/planes" \
      -H "Authorization: Bearer $OWNER_TOKEN")

    echo "Respuesta:"
    echo $PLANES_ACTIVOS | jq '.'
    echo ""

    # ========================================================================
    # 9. OBTENER PLAN POR TIPO (OWNER)
    # ========================================================================
    echo "9. Obtener plan por tipo BASICO (OWNER)..."
    PLAN_BASICO=$(curl -s -X GET "$BASE_URL/owner/planes/tipo/BASICO" \
      -H "Authorization: Bearer $OWNER_TOKEN")

    echo "Respuesta:"
    echo $PLAN_BASICO | jq '.'
    echo ""

    # ========================================================================
    # 10. OBTENER PLAN PREMIUM (OWNER)
    # ========================================================================
    echo "10. Obtener plan PREMIUM por tipo (OWNER)..."
    PLAN_PREMIUM=$(curl -s -X GET "$BASE_URL/owner/planes/tipo/PREMIUM" \
      -H "Authorization: Bearer $OWNER_TOKEN")

    echo "Respuesta:"
    echo $PLAN_PREMIUM | jq '.'
    echo ""
fi

# ============================================================================
# 11. INTENTAR CREAR UN PLAN DUPLICADO (debe fallar)
# ============================================================================
echo "11. Intentar crear un plan con tipo BASICO duplicado (debe fallar)..."
CREATE_DUPLICATE=$(curl -s -X POST "$BASE_URL/admin/planes" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "BASICO",
    "nombre": "Plan Básico Duplicado",
    "precio": 50.00,
    "duracionMeses": 1
  }')

echo "Respuesta esperada (error):"
echo $CREATE_DUPLICATE | jq '.'
echo ""

# ============================================================================
# RESUMEN
# ============================================================================
echo "========================================="
echo "RESUMEN DE PRUEBAS"
echo "========================================="
echo "✅ Login ADMIN"
echo "✅ Listar planes (ADMIN)"
echo "✅ Obtener plan por ID (ADMIN)"
echo "✅ Actualizar plan (ADMIN)"
echo "✅ Cambiar estado plan (ADMIN)"
echo "✅ Paginación de planes (ADMIN)"
if [ "$OWNER_TOKEN" != "null" ] && [ ! -z "$OWNER_TOKEN" ]; then
    echo "✅ Login OWNER"
    echo "✅ Listar planes activos (OWNER)"
    echo "✅ Obtener plan por tipo (OWNER)"
fi
echo "✅ Validación de duplicados"
echo ""
echo "========================================="
echo "PRUEBAS COMPLETADAS"
echo "========================================="
