# Módulo de Reportes

Sistema de reportes empresariales para owners. Proporciona análisis detallados de ingresos, servicios, empleados y clientes.

## 🎯 Características

- **Solo para OWNERS**: Acceso exclusivo a reportes de su propia empresa
- **5 tipos de reportes**: Ingresos, servicios, empleados, clientes y consolidado
- **Rango de fechas**: Análisis de cualquier período de tiempo
- **Métricas detalladas**: Totales, promedios, porcentajes, rankings
- **Datos visuales**: Incluye URLs de imágenes de perfil
- **Respuesta estructurada**: DTOs optimizados para frontend

## 📋 Endpoints

Todos los endpoints requieren autenticación con rol `OWNER`.

### 1. Reporte de Ingresos

Análisis financiero detallado con desglose por día y por servicio.

```http
GET /api/reportes/ingresos?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-01-31
```

**Parámetros:**
- `empresaId` (Long): ID de la empresa
- `fechaInicio` (LocalDate): Fecha de inicio (formato: YYYY-MM-DD)
- `fechaFin` (LocalDate): Fecha de fin (formato: YYYY-MM-DD)

**Respuesta:**
```json
{
  "success": true,
  "message": "Reporte de ingresos generado",
  "data": {
    "ingresoTotal": 15000.00,
    "totalReservas": 50,
    "reservasCompletadas": 45,
    "reservasCanceladas": 5,
    "ingresoPromedio": 333.33,
    "tasaCompletadas": 90.0,
    "ingresosPorDia": [
      {
        "fecha": "2024-01-15",
        "ingreso": 500.00,
        "cantidad": 2
      }
    ],
    "ingresosPorServicio": [
      {
        "servicioNombre": "Corte de Cabello",
        "ingreso": 8000.00,
        "cantidad": 30,
        "porcentaje": 53.33
      }
    ]
  }
}
```

### 2. Reporte de Servicios

Estadísticas de servicios más populares y rentables.

```http
GET /api/reportes/servicios?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-01-31
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Reporte de servicios generado",
  "data": {
    "serviciosEstadisticas": [
      {
        "servicioId": 1,
        "servicioNombre": "Corte de Cabello",
        "totalReservas": 30,
        "ingresoTotal": 8000.00,
        "porcentajeUso": 60.0
      }
    ],
    "servicioMasPopular": {
      "servicioId": 1,
      "servicioNombre": "Corte de Cabello",
      "totalReservas": 30,
      "ingresoTotal": 8000.00,
      "porcentajeUso": 60.0
    }
  }
}
```

### 3. Reporte de Empleados

Rendimiento y estadísticas de cada empleado.

```http
GET /api/reportes/empleados?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-01-31
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Reporte de empleados generado",
  "data": {
    "empleadosEstadisticas": [
      {
        "empleadoId": 10,
        "empleadoNombre": "Juan Pérez",
        "profileImageUrl": "https://example.com/profile.jpg",
        "totalReservas": 25,
        "ingresoGenerado": 7500.00,
        "promedioCalificacion": 4.8,
        "servicioMasOfrecido": "Corte de Cabello"
      }
    ],
    "mejorEmpleado": {
      "empleadoId": 10,
      "empleadoNombre": "Juan Pérez",
      "profileImageUrl": "https://example.com/profile.jpg",
      "totalReservas": 25,
      "ingresoGenerado": 7500.00,
      "promedioCalificacion": 4.8,
      "servicioMasOfrecido": "Corte de Cabello"
    }
  }
}
```

### 4. Reporte de Clientes

Análisis de comportamiento de clientes.

```http
GET /api/reportes/clientes?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-01-31
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Reporte de clientes generado",
  "data": {
    "totalClientes": 40,
    "clientesNuevos": 10,
    "clientesRecurrentes": 30,
    "tasaRetencion": 75.0,
    "topClientes": [
      {
        "clienteId": 5,
        "clienteNombre": "María García",
        "profileImageUrl": "https://example.com/maria.jpg",
        "totalReservas": 8,
        "gastoTotal": 2400.00,
        "servicioFavorito": "Corte de Cabello"
      }
    ]
  }
}
```

### 5. Reporte Consolidado

Dashboard completo con todas las métricas clave.

```http
GET /api/reportes/consolidado?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-01-31
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Reporte consolidado generado",
  "data": {
    "ingresoTotal": 15000.00,
    "totalReservas": 50,
    "tasaCompletadas": 90.0,
    "ingresoPromedio": 333.33,
    "totalServicios": 5,
    "servicioMasPopular": "Corte de Cabello",
    "totalEmpleados": 3,
    "mejorEmpleadoNombre": "Juan Pérez",
    "mejorEmpleadoIngresos": 7500.00,
    "totalClientes": 40,
    "clientesNuevos": 10,
    "clientesRecurrentes": 30,
    "tasaRetencion": 75.0,
    "clienteTopNombre": "María García",
    "clienteTopGasto": 2400.00,
    "promedioReservasPorCliente": 1.25,
    "promedioCalificacionGeneral": 4.7
  }
}
```

## 🔒 Seguridad

### Autenticación
Todos los endpoints requieren:
- Token JWT válido
- Rol: `OWNER`

### Autorización
- El owner solo puede acceder a reportes de su propia empresa
- Validación automática del `ownerId` vs empresa solicitada
- Excepción: `UnauthorizedException` si intenta acceder a otra empresa

## 🏗️ Arquitectura

Sigue arquitectura hexagonal:

```
reporte/
├── domain/
│   └── port/
│       ├── in/
│       │   └── ReporteUseCase.java (Casos de uso)
│       └── out/
│           └── ReporteRepositoryPort.java (Puerto de salida)
├── application/
│   ├── dto/
│   │   ├── ReporteIngresosResponse.java
│   │   ├── ReporteServiciosResponse.java
│   │   ├── ReporteEmpleadosResponse.java
│   │   ├── ReporteClientesResponse.java
│   │   └── ReporteConsolidadoResponse.java
│   └── service/
│       └── ReporteService.java (Lógica de negocio)
└── adapters/
    ├── in/
    │   └── web/
    │       └── ReporteController.java (REST API)
    └── out/
        └── persistence/
            └── ReporteRepositoryAdapter.java (Acceso a datos)
```

## 💡 Casos de Uso

### Dashboard del Owner
```javascript
// Obtener vista general mensual
GET /api/reportes/consolidado?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-01-31

// Muestra en un dashboard:
// - Ingresos totales y promedio
// - Mejor empleado y su rendimiento
// - Cliente más frecuente
// - Servicio más popular
// - Tasa de retención
```

### Análisis de Rendimiento
```javascript
// Ver quién genera más ingresos
GET /api/reportes/empleados?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-01-31

// Decisiones:
// - Bonos por desempeño
// - Identificar necesidad de capacitación
// - Balanceo de carga de trabajo
```

### Optimización de Servicios
```javascript
// Identificar servicios más rentables
GET /api/reportes/servicios?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-01-31

// Acciones:
// - Promocionar servicios populares
// - Descontinuar servicios poco solicitados
// - Ajustar precios según demanda
```

### Estrategia de Marketing
```javascript
// Analizar comportamiento de clientes
GET /api/reportes/clientes?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-01-31

// Campañas:
// - Programas de lealtad para recurrentes
// - Ofertas de bienvenida para nuevos
// - Reactivación de clientes inactivos
```

### Análisis Financiero
```javascript
// Desglose detallado de ingresos
GET /api/reportes/ingresos?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-01-31

// Insights:
// - Días con mayor facturación
// - Servicios más rentables
// - Tasa de cancelación
// - Proyecciones futuras
```

## 📊 Métricas Calculadas

### Ingresos
- **Ingreso Total**: Suma de precios de reservas COMPLETADAS
- **Ingreso Promedio**: Ingreso total / Reservas completadas
- **Tasa Completadas**: (Completadas / Total) * 100
- **Ingresos por Día**: Agrupación temporal con conteos
- **Ingresos por Servicio**: Suma y porcentaje por cada servicio

### Servicios
- **Porcentaje de Uso**: (Reservas servicio / Total reservas) * 100
- **Servicio Más Popular**: Mayor cantidad de reservas
- **Ingresos por Servicio**: Suma de precios del servicio

### Empleados
- **Ingreso Generado**: Suma de reservas completadas del empleado
- **Promedio Calificación**: Media de reseñas del empleado
- **Servicio Más Ofrecido**: Moda en reservas del empleado
- **Mejor Empleado**: Mayor ingreso generado

### Clientes
- **Clientes Nuevos**: Primera reserva en el período
- **Clientes Recurrentes**: Más de una reserva histórica
- **Tasa de Retención**: (Recurrentes / Total) * 100
- **Gasto Total**: Suma de reservas completadas del cliente
- **Top Clientes**: Ordenados por gasto total (máximo 10)

## 🛠️ Desarrollo

### Agregar Nuevo Reporte

1. **Definir caso de uso en ReporteUseCase:**
```java
ReporteNuevoResponse obtenerReporteNuevo(
    Long empresaId, 
    LocalDate fechaInicio, 
    LocalDate fechaFin, 
    String ownerId
);
```

2. **Crear DTO de respuesta:**
```java
@Builder
public record ReporteNuevoResponse(
    // Campos del reporte
) {}
```

3. **Implementar en ReporteService:**
```java
@Override
public ReporteNuevoResponse obtenerReporteNuevo(...) {
    validarAccesoEmpresa(empresaId, ownerId);
    List<Reserva> reservas = repositoryPort.findByEmpresaAndPeriod(...);
    // Lógica de cálculo
    return ReporteNuevoResponse.builder()...build();
}
```

4. **Agregar endpoint en ReporteController:**
```java
@GetMapping("/nuevo")
public ResponseEntity<ApiResponse<ReporteNuevoResponse>> obtenerReporteNuevo(...) {
    ReporteNuevoResponse reporte = reporteUseCase.obtenerReporteNuevo(...);
    return ResponseEntity.ok(ApiResponse.success("Reporte nuevo generado", reporte));
}
```

## ⚠️ Consideraciones

### Rendimiento
- **Grandes volúmenes**: Considerar paginación o caché para períodos largos
- **Consultas pesadas**: Los cálculos se hacen en memoria con Streams
- **Optimización**: Índices en `reservation_date` y `estado_reserva`

### Validaciones
- **Fechas**: fechaInicio <= fechaFin (validar en frontend)
- **Rango máximo**: Considerar límite (ej: 1 año) para evitar sobrecarga
- **EmpresaId**: Validar que pertenece al owner autenticado

### Datos Faltantes
- Si no hay reservas: retorna totales en 0 y listas vacías
- Si no hay reseñas: promedio calificación es 0.0
- Si no hay imágenes: URLs son null

## 🔄 Integración con Frontend

### React/React Native Example
```javascript
const fetchReporteConsolidado = async (empresaId, fechaInicio, fechaFin) => {
  const token = await getAuthToken();
  const response = await fetch(
    `/api/reportes/consolidado?empresaId=${empresaId}&fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }
  );
  const data = await response.json();
  return data.data; // ReporteConsolidadoResponse
};

// Uso en componente
const Dashboard = () => {
  const [reporte, setReporte] = useState(null);
  
  useEffect(() => {
    fetchReporteConsolidado(
      empresaId, 
      '2024-01-01', 
      '2024-01-31'
    ).then(setReporte);
  }, []);
  
  return (
    <View>
      <Text>Ingresos: ${reporte?.ingresoTotal}</Text>
      <Text>Mejor Empleado: {reporte?.mejorEmpleadoNombre}</Text>
      <Text>Tasa Completadas: {reporte?.tasaCompletadas}%</Text>
    </View>
  );
};
```

## 📝 Notas Técnicas

- **Estado de reservas**: Solo se incluyen reservas con `deleted = false`
- **Zona horaria**: Las fechas se manejan en `LocalDate` (sin hora)
- **Precisión decimal**: BigDecimal con 2 decimales para montos
- **Ordenamiento**: Top rankings limitan a 10 elementos máximo
- **Seguridad**: Todas las operaciones pasan por validación de ownership

## 🧪 Testing

### Pruebas con cURL

```bash
# Obtener token (ajustar endpoint de login)
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"owner@example.com","password":"password"}' \
  | jq -r '.data.token')

# Reporte consolidado
curl -X GET "http://localhost:8080/api/reportes/consolidado?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-12-31" \
  -H "Authorization: Bearer $TOKEN"

# Reporte de empleados
curl -X GET "http://localhost:8080/api/reportes/empleados?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-12-31" \
  -H "Authorization: Bearer $TOKEN"
```

## 📚 Referencias

- **Entidades relacionadas**: Reserva, Empresa, User (Employee/Client), Servicio, Resenia
- **Repositorios usados**: ReservaRepositoryPort
- **DTOs base**: ApiResponse (respuesta estándar)
- **Seguridad**: SecurityConfig con JWT y roles
