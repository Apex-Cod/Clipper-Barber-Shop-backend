# Sistema de Recomendaciones e Historial

Sistema simple de recomendaciones para clientes basado en el historial de reservas existente.

## 📋 Características

### 1. **Recomendaciones Personalizadas**
- Basadas en el historial de reservas completadas del cliente
- Considera servicios ya utilizados
- Detecta categorías preferidas del cliente
- Utiliza calificaciones de reseñas

### 2. **Filtrado por Género**
- Servicios para HOMBRES
- Servicios para MUJERES
- Servicios UNISEX
- Servicios para NIÑOS
- Servicios para NIÑAS

### 3. **Algoritmo Simple**
El sistema analiza:
- ✅ **Historial de reservas**: Cuenta cuántas veces el cliente ha usado cada servicio/empresa
- ✅ **Calificaciones**: Usa las reseñas para ordenar por mejor valorados
- ✅ **Categorías preferidas**: Detecta qué tipos de servicios usa más el cliente
- ✅ **Público objetivo**: Filtra por género si se especifica

## 🔌 Endpoints API

### Para Clientes (requiere rol CLIENT)

#### 1. Obtener servicios recomendados
```http
GET /api/client/recomendaciones/servicios?publicoObjetivo=HOMBRES&limit=10
```

**Parámetros opcionales:**
- `publicoObjetivo`: HOMBRES, MUJERES, UNISEX, NIÑOS, NIÑAS
- `limit`: Cantidad de resultados (default: 10)

**Respuesta:**
```json
{
  "success": true,
  "message": "Servicios recomendados obtenidos exitosamente",
  "data": [
    {
      "id": 1,
      "nombre": "Corte de cabello clásico",
      "descripcion": "Corte tradicional con tijeras",
      "precio": 15000.0,
      "duracion": 30,
      "categoria": "CORTE",
      "publicoObjetivo": "HOMBRES",
      "imageUrl": "https://...",
      "empresaId": 5,
      "empresaNombre": "Barbería El Estilo",
      "empresaDireccion": "Calle 123",
      "empresaLogoUrl": "https://...",
      "calificacionPromedio": 4.8,
      "totalResenias": 45,
      "vecesUsadoPorCliente": 3,
      "motivoRecomendacion": "Ya lo has usado 3 veces • Categoría de tu preferencia • Excelentemente calificado"
    }
  ]
}
```

#### 2. Obtener empresas recomendadas
```http
GET /api/client/recomendaciones/empresas?publicoObjetivo=UNISEX&limit=5
```

**Parámetros opcionales:**
- `publicoObjetivo`: HOMBRES, MUJERES, UNISEX, NIÑOS, NIÑAS
- `limit`: Cantidad de resultados (default: 10)

**Respuesta:**
```json
{
  "success": true,
  "message": "Empresas recomendadas obtenidas exitosamente",
  "data": [
    {
      "id": 5,
      "nombre": "Barbería El Estilo",
      "direccion": "Calle 123 #45-67",
      "telefono": "3001234567",
      "email": "contacto@elestilo.com",
      "logoUrl": "https://...",
      "bannerUrl": "https://...",
      "descripcion": "Barbería tradicional con servicios modernos",
      "publicoObjetivo": "HOMBRES",
      "calificacionPromedio": 4.7,
      "totalResenias": 120,
      "vecesVisitadaPorCliente": 5,
      "motivoRecomendacion": "Ya la has visitado 5 veces • Excelentemente calificada • Muy popular",
      "serviciosDestacados": [
        {
          "id": 1,
          "nombre": "Corte clásico",
          "precio": 15000.0,
          "categoria": "CORTE"
        }
      ]
    }
  ]
}
```

#### 3. Mejores servicios por género (sin personalización)
```http
GET /api/client/recomendaciones/servicios/por-genero?publicoObjetivo=MUJERES&limit=10
```

**Parámetros:**
- `publicoObjetivo`: HOMBRES, MUJERES, UNISEX, NIÑOS, NIÑAS (requerido)
- `limit`: Cantidad de resultados (default: 10)

Este endpoint retorna los servicios mejor calificados para un género específico, sin considerar el historial personal del cliente.

## 🧮 Cómo Funciona el Algoritmo

### Recomendación de Servicios

1. **Análisis de historial**
   - Cuenta cuántas veces el cliente usó cada servicio (solo reservas COMPLETED)
   - Identifica las 3 categorías más usadas

2. **Cálculo de calificaciones**
   - Promedia las calificaciones de las reseñas
   - Cuenta el total de reseñas

3. **Filtrado**
   - Filtra por público objetivo si se especifica
   - Incluye siempre servicios UNISEX

4. **Ordenamiento**
   - Primero: Por calificación promedio (descendente)
   - Segundo: Por veces usado por el cliente (descendente)

5. **Motivos de recomendación**
   - "Ya lo has usado X veces"
   - "Categoría de tu preferencia"
   - "Excelentemente calificado" (≥ 4.5)
   - "Bien calificado" (≥ 4.0)
   - "Popular" (> 20 reseñas)

### Recomendación de Empresas

Similar al de servicios pero:
- Cuenta visitas a cada empresa
- Calcula calificación promedio de la empresa
- Incluye servicios destacados de cada empresa

## 📊 Uso del Historial Existente

El sistema **no crea tablas nuevas**, usa directamente:
- ✅ `reservas`: Para contar servicios/empresas usadas
- ✅ `resenias`: Para obtener calificaciones
- ✅ `servicios`: Para información de servicios
- ✅ `empresas`: Para información de empresas

## 🎯 Casos de Uso

### 1. Cliente nuevo (sin historial)
```
GET /api/client/recomendaciones/servicios
```
Retorna servicios mejor calificados en general.

### 2. Cliente con historial
```
GET /api/client/recomendaciones/servicios
```
Retorna servicios basados en sus preferencias detectadas.

### 3. Búsqueda específica por género
```
GET /api/client/recomendaciones/servicios?publicoObjetivo=MUJERES
```
Retorna servicios para mujeres o unisex, ordenados por el historial del cliente.

### 4. Ver lo más popular por género
```
GET /api/client/recomendaciones/servicios/por-genero?publicoObjetivo=HOMBRES
```
Retorna los servicios mejor calificados para hombres, sin personalización.

## 🔒 Seguridad

- Todos los endpoints requieren autenticación
- Solo accesible para usuarios con rol `CLIENT`
- Cada cliente solo ve sus propias recomendaciones personalizadas

## 🚀 Ejemplos de Integración

### JavaScript/TypeScript
```typescript
// Obtener servicios recomendados
const response = await fetch('/api/client/recomendaciones/servicios?limit=5', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const data = await response.json();
console.log(data.data); // Array de servicios recomendados
```

### Filtrar por género
```typescript
const serviciosHombres = await fetch(
  '/api/client/recomendaciones/servicios?publicoObjetivo=HOMBRES&limit=8',
  { headers: { 'Authorization': `Bearer ${token}` }}
).then(r => r.json());
```

## 📈 Mejoras Futuras (Opcionales)

Si en el futuro se quiere mejorar el sistema, se podría:
- Agregar proximidad geográfica (distancia)
- Considerar horarios de preferencia
- Agregar descuentos/promociones personalizadas
- Machine Learning para patrones más complejos
- Recomendaciones colaborativas (usuarios similares)

## 🏗️ Estructura del Código

```
recomendacion/
├── adapters/
│   └── in/
│       └── web/
│           └── RecomendacionClientController.java
├── application/
│   ├── dto/
│   │   ├── EmpresaRecomendadaDTO.java
│   │   ├── ServicioRecomendadoDTO.java
│   │   └── ServicioDestacadoDTO.java
│   └── service/
│       └── RecomendacionService.java
```

## ✅ Ventajas de este Enfoque

1. **Simple**: Fácil de entender y mantener
2. **Sin tablas adicionales**: Usa las existentes
3. **Rápido**: Consultas eficientes
4. **Flexible**: Fácil de extender
5. **Probado**: Usa patrones conocidos
