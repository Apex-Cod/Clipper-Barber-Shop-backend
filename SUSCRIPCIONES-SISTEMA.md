# 📅 Sistema de Suscripciones - Verificación Mensual/Anual

## 📋 Descripción General

El sistema de suscripciones integra los **Planes** con los **Pagos** para garantizar que las empresas solo puedan usar los servicios si tienen una suscripción **activa y pagada**.

## 🔄 Flujo Completo del Sistema

### 1. **Creación de Suscripción**
```
Owner selecciona un Plan → Se crea Suscripción (estado: PENDIENTE_PAGO)
```

- El OWNER selecciona un plan (GRATUITO, BASICO, PREMIUM)
- Se crea una `Suscripcion` con:
  - `fecha_inicio`: Hoy
  - `fecha_fin`: Hoy + duración del plan (meses)
  - `estado`: `PENDIENTE_PAGO`
  - `plan_id`: ID del plan seleccionado
  - `empresa_id`: ID de la empresa

**Endpoint:**
```http
POST /api/owner/suscripciones
{
  "empresaId": 1,
  "planId": 2
}
```

---

### 2. **Procesamiento del Pago (PayPal)**
```
Owner paga con PayPal → Webhook recibe confirmación → Suscripción se ACTIVA
```

- El owner procede al pago mediante PayPal
- Se crea un registro en la tabla `pagos`:
  - `empresa_id`: ID de la empresa
  - `suscripcion_id`: ID de la suscripción creada
  - `monto`: Precio del plan
  - `estado`: `PENDIENTE`
  - `metodo_pago`: `PAYPAL`

- **Cuando PayPal confirma el pago exitoso** (webhook):
  1. El pago cambia a `estado = COMPLETADO`
  2. Se llama a `SuscripcionService.activarSuscripcion()`
  3. La suscripción cambia a `estado = ACTIVA`
  4. Se registra `fecha_activacion = now()`
  5. Se guarda el `monto_pagado`

**Integración con PaymentService** (actualizar):
```java
// En PaymentService, después de confirmar el pago:
if (pago.getSuscripcion() != null) {
    suscripcionService.activarSuscripcion(
        pago.getSuscripcion().getId(), 
        pago.getMonto()
    );
}
```

---

### 3. **Verificación Continua (Cada Acción)**
```
Usuario intenta acción → PlanLimitService verifica suscripción → ✅/❌
```

**ANTES** de permitir cualquier acción (crear usuario, reserva, servicio, promoción), el sistema **SIEMPRE verifica**:

#### ✅ Verificación de Suscripción Activa
```java
// En PlanLimitService (ya implementado)
private void verificarSuscripcionActiva(Empresa empresa) {
    suscripcionService.obtenerSuscripcionActiva(empresa.getId());
    // Lanza SuscripcionException si:
    // - No existe suscripción activa
    // - La fecha_fin < fecha actual (expiró)
}
```

#### ⚠️ Casos de Error:
1. **Sin suscripción activa**:
   ```
   "La empresa no tiene una suscripción activa. 
    Por favor, contrata un plan para continuar usando el servicio."
   ```

2. **Suscripción expirada**:
   ```
   "Tu suscripción expiró el 2024-11-01. 
    Por favor, renueva tu plan para continuar usando el servicio."
   ```

3. **Límite del plan excedido**:
   ```
   "Has alcanzado el límite de usuarios (5/5) permitido 
    por tu plan BASICO. Actualiza a PREMIUM para más."
   ```

---

### 4. **Verificación Automática Diaria (Expiración)**
```
Job Programado (00:00) → Marca suscripciones expiradas
```

Se debe crear un **job programado** (usando `@Scheduled` de Spring) que ejecute diariamente:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class SuscripcionScheduledTasks {
    
    private final SuscripcionService suscripcionService;
    
    // Ejecutar todos los días a las 00:00
    @Scheduled(cron = "0 0 0 * * *")
    public void marcarSuscripcionesExpiradas() {
        log.info("Iniciando verificación de suscripciones expiradas...");
        int totalExpiradas = suscripcionService.marcarSuscripcionesExpiradas();
        log.info("Total de suscripciones expiradas: {}", totalExpiradas);
    }
    
    // Notificar suscripciones próximas a vencer (7 días antes)
    @Scheduled(cron = "0 0 9 * * *") // Todos los días a las 9:00
    public void notificarProximasAVencer() {
        List<Suscripcion> proximasAVencer = 
            suscripcionService.obtenerSuscripcionesProximasAVencer(7);
        
        proximasAVencer.forEach(suscripcion -> {
            log.info("Suscripción próxima a vencer: Empresa {} - {} días restantes", 
                suscripcion.getEmpresa().getNombre(), 
                suscripcion.getDiasRestantes());
            
            // TODO: Enviar email de notificación
            // emailService.enviarNotificacionVencimiento(suscripcion);
        });
    }
}
```

**Configuración en `application.properties`:**
```properties
# Habilitar scheduling
spring.task.scheduling.enabled=true
```

---

### 5. **Renovación de Suscripción**

Hay dos formas de renovar:

#### A) **Renovación Manual (Owner)**
```http
POST /api/owner/suscripciones/{id}/renovar
```

Crea una **nueva suscripción** que:
- Inicia el día siguiente al fin de la actual
- Mantiene el mismo plan
- Estado: `PENDIENTE_PAGO` (debe pagar nuevamente)

#### B) **Renovación Automática** (Futuro)
Si `auto_renovar = true`:
- Cuando la suscripción está por vencer (3 días antes)
- Se intenta cobrar automáticamente con PayPal
- Si el pago es exitoso, se crea y activa la nueva suscripción

---

## 🗄️ Modelo de Datos

### Tabla: `suscripciones`
```sql
CREATE TABLE suscripciones (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id),
    plan_id BIGINT NOT NULL REFERENCES planes(id),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE_PAGO',
    -- Estados: ACTIVA, EXPIRADA, CANCELADA, PENDIENTE_PAGO
    monto_pagado DECIMAL(10,2),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_activacion TIMESTAMP,
    fecha_cancelacion TIMESTAMP,
    auto_renovar BOOLEAN DEFAULT FALSE,
    notas VARCHAR(500),
    deleted BOOLEAN DEFAULT FALSE
);
```

### Tabla: `pagos` (actualizada)
```sql
CREATE TABLE pagos (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL REFERENCES empresas(id),
    suscripcion_id BIGINT REFERENCES suscripciones(id), -- NUEVA COLUMNA
    reserva_id BIGINT REFERENCES reservas(id),
    monto DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    metodo_pago VARCHAR(20) NOT NULL,
    paypal_payment_id VARCHAR(255),
    fecha_pago TIMESTAMP,
    -- ... otros campos
);
```

---

## 🔧 Endpoints Principales

### **Gestión de Suscripciones (OWNER)**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/owner/suscripciones` | Crear nueva suscripción |
| `GET` | `/api/owner/suscripciones/activa?empresaId={id}` | Obtener suscripción activa |
| `GET` | `/api/owner/suscripciones/historial?empresaId={id}` | Ver historial completo |
| `GET` | `/api/owner/suscripciones/info?empresaId={id}` | Info (días restantes, estado) |
| `POST` | `/api/owner/suscripciones/{id}/cancelar` | Cancelar suscripción |
| `POST` | `/api/owner/suscripciones/{id}/renovar` | Renovar suscripción |

---

## 📊 Ejemplo de Flujo Completo

### **Mes 1: Empresa se registra**
```
1. Owner crea empresa
2. Owner selecciona Plan BASICO ($29.99/mes)
3. Sistema crea suscripción (PENDIENTE_PAGO)
4. Owner paga con PayPal
5. Webhook activa suscripción
   - Estado: ACTIVA
   - fecha_fin: 2024-12-09
```

### **Durante el mes: Owner usa el sistema**
```
6. Owner intenta crear usuario
   ✅ PlanLimitService verifica:
      - ¿Tiene suscripción activa? SÍ
      - ¿Expiró? NO (fecha_fin: 2024-12-09 > hoy: 2024-11-15)
      - ¿Límite de usuarios? 2/5 OK
   → Usuario creado exitosamente
```

### **Día 2024-12-02 (7 días antes de vencer)**
```
7. Job programado detecta suscripción próxima a vencer
8. Sistema envía email: "Tu suscripción vence en 7 días"
```

### **Día 2024-12-10 (expiró)**
```
9. Job programado (00:00) marca suscripción como EXPIRADA
10. Owner intenta crear servicio
    ❌ PlanLimitService verifica:
       - ¿Tiene suscripción activa? SÍ
       - ¿Expiró? SÍ (fecha_fin: 2024-12-09 < hoy: 2024-12-10)
    → Error: "Tu suscripción expiró. Por favor, renueva tu plan."
```

### **Owner renueva**
```
11. Owner hace clic en "Renovar plan"
12. Sistema crea nueva suscripción (PENDIENTE_PAGO)
    - fecha_inicio: 2024-12-10
    - fecha_fin: 2025-01-10
13. Owner paga
14. Suscripción se activa
15. Owner puede volver a usar el sistema ✅
```

---

## 🔐 Seguridad

- Solo `OWNER` puede gestionar suscripciones de su empresa
- `ADMIN` no accede a suscripciones (solo a planes)
- Cada verificación valida que la suscripción no haya sido borrada lógicamente
- Webhook de PayPal debe validar firma para prevenir fraude

---

## 📈 Métricas y Consultas Útiles

### Ver suscripciones activas
```sql
SELECT * FROM v_suscripciones_activas;
```

### Suscripciones próximas a vencer (7 días)
```sql
SELECT * FROM v_suscripciones_activas 
WHERE dias_restantes <= 7;
```

### Ejecutar manualmente el job de expiración
```sql
SELECT marcar_suscripciones_expiradas();
```

### Empresas sin suscripción activa
```sql
SELECT e.id, e.nombre 
FROM empresas e
LEFT JOIN suscripciones s ON e.id = s.empresa_id 
  AND s.estado = 'ACTIVA' 
  AND s.deleted = FALSE
WHERE s.id IS NULL;
```

---

## ✅ Checklist de Implementación

- [x] Entidad `Suscripcion` con soft delete
- [x] Enum `EstadoSuscripcion`
- [x] `SuscripcionService` con lógica de negocio
- [x] `SuscripcionRepositoryPort` y adapters
- [x] Integración con `PlanLimitService`
- [x] `SuscripcionOwnerController` (6 endpoints)
- [x] DTOs y Mapper
- [x] Migración SQL
- [x] SecurityConfig actualizado
- [ ] **Job programado** para expirar suscripciones
- [ ] **Integración con PaymentService** (activar tras pago)
- [ ] **Sistema de notificaciones** (emails)
- [ ] **Tests unitarios**

---

## 🚀 Próximos Pasos

1. **Crear `SuscripcionScheduledTasks`** para ejecutar jobs automáticos
2. **Actualizar `PaymentService`** para activar suscripciones tras pago exitoso
3. **Implementar sistema de notificaciones** (email) para:
   - Suscripción creada
   - Suscripción activada (pago confirmado)
   - Suscripción próxima a vencer (7, 3, 1 día)
   - Suscripción expirada
4. **Dashboard de suscripciones** en el frontend
5. **Tests de integración** con PayPal sandbox

---

## 📞 Soporte

Para cualquier duda sobre el sistema de suscripciones, consultar:
- `SuscripcionService.java` - Lógica principal
- `PlanLimitService.java` - Validaciones
- `migration-suscripciones.sql` - Estructura de BD
