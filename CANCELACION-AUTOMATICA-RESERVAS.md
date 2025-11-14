# Cancelación Automática de Reservas

## 📋 Descripción

Sistema de cancelación automática de reservas que se ejecuta periódicamente para gestionar reservas que no fueron completadas dentro del tiempo establecido.

## ⚙️ Configuración

### Activación del Scheduling

Se habilitó el scheduling en la aplicación mediante la anotación `@EnableScheduling` en la clase principal:

```java
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class ClipperBarberShopApplication {
    // ...
}
```

## 🔄 Funcionamiento

### Tarea Programada

**Clase:** `ReservaScheduledService`  
**Ubicación:** `apex.code.clipperBarberShop.reserva.application.service`  
**Frecuencia:** Cada minuto (configurado con cron: `"0 */1 * * * *"`)

### Lógica de Cancelación

El sistema cancela automáticamente las reservas que cumplen **todas** las siguientes condiciones:

1. **Estado:** `PENDING` o `CONFIRMED`
2. **No eliminadas:** `deleted = false`
3. **Tiempo expirado:** La hora de finalización de la reserva + 5 minutos de tolerancia ya pasó

#### Cálculo de Expiración

```
Hora de Finalización = reservationDate + duracionMinutos + 5 minutos de tolerancia
```

**Ejemplo:**
- Reserva programada: 14:00
- Duración del servicio: 30 minutos
- Hora de finalización: 14:30
- Tolerancia: 5 minutos
- Se cancela automáticamente después de: 14:35

## 📨 Notificaciones WebSocket

Cuando una reserva se cancela automáticamente, se envían notificaciones en tiempo real a:

1. **Cliente** - El usuario que creó la reserva
2. **Empleado** - El empleado asignado a la reserva
3. **Empresa** - Todos los usuarios de la empresa (owner, admins, etc.)

### Datos de la Notificación

```json
{
  "id": 123,
  "empresaId": 1,
  "servicioNombre": "Corte de Cabello",
  "fechaReserva": "2025-11-14T14:00:00",
  "estadoAnterior": "CONFIRMED",
  "motivoCancelacion": "CANCELACION_AUTOMATICA_POR_EXPIRACION"
}
```

## 📊 Logging

El sistema registra información detallada en los logs:

- **INFO:** Número de reservas encontradas para cancelar
- **INFO:** Detalle de cada reserva cancelada (ID, estado anterior, fecha)
- **INFO:** Resumen del proceso completado
- **ERROR:** Cualquier error en el proceso de cancelación o envío de notificaciones

### Ejemplo de Logs

```
INFO  - Encontradas 3 reservas expiradas para cancelación automática
INFO  - Reserva ID 45 cancelada automáticamente. Estado anterior: CONFIRMED, Fecha: 2025-11-14T10:00
INFO  - Reserva ID 52 cancelada automáticamente. Estado anterior: PENDING, Fecha: 2025-11-14T11:30
INFO  - Reserva ID 63 cancelada automáticamente. Estado anterior: CONFIRMED, Fecha: 2025-11-14T13:00
INFO  - Proceso de cancelación automática completado. 3 reservas canceladas
```

## 🔒 Transaccionalidad

La operación de cancelación es transaccional (`@Transactional`), lo que garantiza:

- ✅ Todas las reservas se cancelan o ninguna
- ✅ Consistencia de datos en caso de error
- ✅ Rollback automático si ocurre algún problema

## 🛡️ Manejo de Errores

El sistema está diseñado para ser resiliente:

1. **Captura de excepciones globales:** No afecta la ejecución de otras tareas programadas
2. **Logging detallado:** Facilita la depuración
3. **Notificaciones con try-catch:** Si falla el envío de notificaciones, no afecta la cancelación de la reserva
4. **Validaciones defensivas:** Verifica que los datos existan antes de usarlos

## 📝 Configuración del Cron

Actualmente configurado para ejecutarse **cada minuto**:

```java
@Scheduled(cron = "0 */1 * * * *")
```

### Alternativas de Configuración

Si deseas cambiar la frecuencia, puedes usar:

```java
// Cada 5 minutos
@Scheduled(cron = "0 */5 * * * *")

// Cada 10 minutos
@Scheduled(cron = "0 */10 * * * *")

// Cada hora en el minuto 0
@Scheduled(cron = "0 0 * * * *")

// Cada día a las 2:00 AM
@Scheduled(cron = "0 0 2 * * *")
```

### Formato del Cron

```
┌───────────── segundo (0-59)
│ ┌───────────── minuto (0-59)
│ │ ┌───────────── hora (0-23)
│ │ │ ┌───────────── día del mes (1-31)
│ │ │ │ ┌───────────── mes (1-12)
│ │ │ │ │ ┌───────────── día de la semana (0-7, 0 y 7 = domingo)
│ │ │ │ │ │
* * * * * *
```

## 🎯 Estados de Reserva

El sistema respeta la siguiente lógica de estados:

| Estado Actual | ¿Se Cancela Automáticamente? | Motivo |
|--------------|------------------------------|---------|
| `PENDING` | ✅ Sí | Reserva no confirmada y expirada |
| `CONFIRMED` | ✅ Sí | Reserva confirmada pero no completada a tiempo |
| `COMPLETED` | ❌ No | Reserva ya finalizada |
| `CANCELLED` | ❌ No | Ya está cancelada |
| `RESCHEDULED` | ❌ No | Tiene nueva fecha programada |

## 🧪 Testing

Para probar la funcionalidad:

1. **Crear una reserva** con fecha/hora en el pasado
2. **Esperar 1 minuto** para que se ejecute la tarea programada
3. **Verificar en los logs** que la reserva fue cancelada
4. **Verificar en la base de datos** que el estado cambió a `CANCELLED`
5. **Verificar en el cliente WebSocket** que llegó la notificación

## ⚠️ Consideraciones

- **Rendimiento:** La tarea lee todas las reservas cada minuto. En producción con muchas reservas, considera optimizar la consulta.
- **Zona horaria:** Asegúrate de que el servidor y la base de datos usen la misma zona horaria.
- **Testing:** En desarrollo, puedes cambiar el cron a una frecuencia más alta (ej: cada 10 segundos) para testing.

## 🚀 Próximas Mejoras

Posibles mejoras para el futuro:

1. **Optimización de consultas:** Usar query específica en lugar de `findAll()`
2. **Notificación por email:** Además de WebSocket, enviar email al cliente
3. **Métricas:** Agregar métricas de cuántas reservas se cancelan automáticamente
4. **Configuración dinámica:** Permitir configurar el tiempo de tolerancia desde properties
5. **Historial:** Guardar un registro de cancelaciones automáticas en una tabla de auditoría

## 📚 Referencias

- [Spring Scheduling](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)
- [Cron Expression Generator](https://crontab.guru/)
- [Spring @Scheduled Annotation](https://www.baeldung.com/spring-scheduled-tasks)
