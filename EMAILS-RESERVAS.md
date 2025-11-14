# Sistema de Notificaciones por Email para Reservas

## 📋 Descripción

Sistema automatizado de envío de correos electrónicos para gestión de reservas, que incluye:
1. **Email de confirmación** al crear una reserva
2. **Email recordatorio** 15 minutos antes de la cita
3. **Email de reserva completada** cuando se finaliza una cita
4. **Email de reserva cancelada** cuando se cancela una cita
5. **Email de reserva reprogramada** cuando se cambia la fecha/hora

## 📧 Tipos de Emails

### 1. Email de Confirmación de Reserva

**Cuándo se envía:** Inmediatamente después de crear una nueva reserva

**Destinatario:** Cliente que realizó la reserva

**Contenido:**
- ✅ Confirmación de reserva exitosa
- 📋 Detalles completos de la reserva:
  - Fecha y hora
  - Servicio contratado
  - Duración
  - Profesional asignado
  - Precio
  - Dirección de la empresa
  - Número de reserva
- ⚠️ **RECORDATORIO IMPORTANTE:** Llegar 15 minutos antes
- 📬 Aviso de recordatorio futuro

**Asunto:** `✅ Confirmación de Reserva - [Nombre Empresa]`

### 2. Email Recordatorio (15 minutos antes)

**Cuándo se envía:** 15 minutos antes de la hora programada de la reserva

**Destinatario:** Cliente con reserva próxima

**Contenido:**
- ⏰ Alerta visual destacada
- 🕐 Hora de la cita en formato grande
- 📋 Resumen de la cita:
  - Servicio
  - Profesional
  - Dirección
  - Número de reserva
- 🏃‍♂️ Recordatorio urgente para dirigirse al establecimiento

**Asunto:** `⏰ Recordatorio: Tu cita es en 15 minutos - [Nombre Empresa]`

### 3. Email de Reserva Completada

**Cuándo se envía:** Cuando un empleado o propietario marca la reserva como completada

**Destinatario:** Cliente que recibió el servicio

**Contenido:**
- ✅ Confirmación de servicio completado
- 📋 Detalles de la reserva finalizada:
  - Fecha y hora
  - Servicio prestado
  - Profesional que atendió
- 🙏 Agradecimiento por confiar en el servicio
- 💬 Invitación a compartir su experiencia

**Asunto:** `✅ Reserva Completada - [Nombre Empresa]`

**Diseño:**
- Header verde (#4CAF50) - Éxito
- Icono de check (✅) destacado
- Mensaje de agradecimiento

### 4. Email de Reserva Cancelada

**Cuándo se envía:** 
- Cuando el cliente cancela su reserva
- Cuando el propietario/empleado cancela la reserva
- Cuando el sistema cancela automáticamente por no presentarse (5 min después de la hora)

**Destinatario:** Cliente de la reserva cancelada

**Contenido:**
- ❌ Notificación de cancelación
- 📋 Detalles de la reserva cancelada:
  - Fecha y hora
  - Servicio
- 📝 Motivo de cancelación (opcional)
- 💼 Invitación a agendar nueva cita

**Asunto:** `❌ Reserva Cancelada - [Nombre Empresa]`

**Diseño:**
- Header rojo (#f44336) - Alerta
- Icono de X (❌) destacado
- Sección opcional para motivo de cancelación

**Casos especiales:**
- **Cancelación automática:** Motivo = "La reserva fue cancelada automáticamente por no presentarse en el horario programado."

### 5. Email de Reserva Reprogramada

**Cuándo se envía:** Cuando se cambia la fecha/hora de una reserva existente

**Destinatario:** Cliente de la reserva

**Contenido:**
- 🔄 Notificación de cambio de fecha
- 📋 Fecha anterior (tachada)
- 📋 Nueva fecha y hora (destacada):
  - Servicio
  - Profesional asignado
  - Nueva fecha (en color naranja)
  - Nueva hora (en color naranja)
- ⏰ Recordatorio de llegar 15 minutos antes

**Asunto:** `🔄 Reserva Reprogramada - [Nombre Empresa]`

**Diseño:**
- Header naranja (#FF9800) - Cambio
- Icono de recarga (🔄) destacado
- Comparación visual: fecha anterior vs nueva fecha
- Caja de recordatorio amarilla

## ⚙️ Implementación Técnica

### Servicios Modificados

#### 1. `EmailService.java` (Interface)
```java
// Emails existentes
void sendReservaConfirmacionEmail(Reserva reserva, String clientName, String clientEmail, 
                                  String employeeName, String serviceName, 
                                  String empresaName, String empresaDireccion);

void sendReservaRecordatorioEmail(Reserva reserva, String clientName, String clientEmail,
                                  String employeeName, String serviceName,
                                  String empresaName, String empresaDireccion);

// ⭐ Nuevos emails agregados
void sendReservaCompletadaEmail(Reserva reserva, String clientName, String clientEmail,
                                String employeeName, String serviceName, String empresaName);

void sendReservaCanceladaEmail(Reserva reserva, String clientName, String clientEmail,
                               String serviceName, String empresaName, String motivo);

void sendReservaReprogramadaEmail(Reserva reserva, String clientName, String clientEmail,
                                  String employeeName, String serviceName, 
                                  String empresaName, String fechaAnterior);
```

#### 2. `EmailServiceImpl.java`
**Métodos implementados:**
- `sendReservaConfirmacionEmail()` - Confirmación
- `sendReservaRecordatorioEmail()` - Recordatorio
- `sendReservaCompletadaEmail()` ⭐ - Completada
- `sendReservaCanceladaEmail()` ⭐ - Cancelada
- `sendReservaReprogramadaEmail()` ⭐ - Reprogramada

**Templates HTML:**
- `buildReservaConfirmacionTemplate()` - Template confirmación
- `buildReservaRecordatorioTemplate()` - Template recordatorio
- `buildReservaCompletadaTemplate()` ⭐ - Template completada
- `buildReservaCanceladaTemplate()` ⭐ - Template cancelada
- `buildReservaReprogramadaTemplate()` ⭐ - Template reprogramada

**Características:**
- ✅ Envío asíncrono con `@Async`
- ✅ Templates HTML responsive
- ✅ Formato de fecha localizado (español)
- ✅ Manejo de errores sin bloquear flujo
- ✅ Logging detallado
- ✅ Diseño diferenciado por color según tipo de email

#### 3. `ReservaClientService.java`
**Integraciones de email:**
- ✅ Confirmación al crear reserva
- ✅ Cancelación cuando cliente cancela ⭐
- ✅ Reprogramación cuando cliente reprograma ⭐

#### 4. `ReservaOwnerService.java`
**Integraciones de email:** ⭐
- ✅ Completada cuando owner marca como completada
- ✅ Cancelación cuando owner cancela
- ✅ Reprogramación cuando owner reprograma

**Nueva dependencia:**
```java
private final EmailService emailService;
```

#### 5. `ReservaEmployeeService.java`
**Integraciones de email:** ⭐
- ✅ Completada cuando empleado marca como completada
- ✅ Cancelación cuando empleado cancela

**Nueva dependencia:**
```java
private final EmailService emailService;
```

#### 6. `ReservaScheduledService.java`
**Integraciones:**
- ✅ Recordatorio 15 min antes (existente)
- ✅ Cancelación automática por no presentarse ⭐

**Nuevo envío de email:**
```java
// Al cancelar automáticamente por expiración
emailService.sendReservaCanceladaEmail(
    reserva, clientName, clientEmail, serviceName, empresaName,
    "La reserva fue cancelada automáticamente por no presentarse en el horario programado."
);
```

## 🔄 Flujos de Funcionamiento

### Flujo de Confirmación
```
1. Cliente crea reserva
   ↓
2. Reserva se guarda en BD
   ↓
3. Se envía email de confirmación (asíncrono) ✅
   ↓
4. Se envía notificación WebSocket
   ↓
5. Se retorna respuesta al cliente
```

### Flujo de Recordatorio
```
1. Tarea programada ejecuta cada minuto
   ↓
2. Busca reservas entre 14-16 min en el futuro
   ↓
3. Filtra por recordatorioEnviado = false
   ↓
4. Para cada reserva:
   - Obtiene datos del cliente
   - Obtiene datos del empleado
   - Envía email recordatorio ✅
   - Marca recordatorioEnviado = true
   ↓
5. Registra en logs
```

### Flujo de Completar Reserva ⭐
```
1. Empleado/Owner marca reserva como completada
   ↓
2. Actualiza status = "COMPLETED" en BD
   ↓
3. Reinicia recordatorioEnviado = false
   ↓
4. Envía email de completación ✅
   ↓
5. Envía notificación WebSocket
   ↓
6. Retorna respuesta
```

### Flujo de Cancelar Reserva ⭐
```
1. Cliente/Owner/Empleado cancela reserva
   ↓
2. Actualiza status = "CANCELLED" en BD
   ↓
3. Reinicia recordatorioEnviado = false
   ↓
4. Envía email de cancelación con motivo ✅
   ↓
5. Envía notificación WebSocket
   ↓
6. Retorna respuesta (o void)
```

### Flujo de Reprogramar Reserva ⭐
```
1. Cliente/Owner reprograma reserva
   ↓
2. Guarda fecha anterior
   ↓
3. Actualiza fecha/hora/empleado en BD
   ↓
4. Actualiza status = "RESCHEDULED"
   ↓
5. Reinicia recordatorioEnviado = false (nuevo recordatorio)
   ↓
6. Envía email de reprogramación con comparación ✅
   ↓
7. Envía notificación WebSocket
   ↓
8. Retorna respuesta
```

### Flujo de Cancelación Automática ⭐
```
1. Tarea programada ejecuta cada minuto
   ↓
2. Busca reservas expiradas (5 min después de fin)
   ↓
3. Para cada reserva expirada:
   - Actualiza status = "CANCELLED"
   - Reinicia recordatorioEnviado = false
   - Envía email de cancelación automática ✅
   - Envía notificación WebSocket
   ↓
4. Registra en logs
```

## 📊 Estados de Reserva Elegibles

### Email de Confirmación
- ✅ Se envía para **todas** las reservas nuevas

### Email Recordatorio
- ✅ `PENDING` - Reserva pendiente de confirmación
- ✅ `CONFIRMED` - Reserva confirmada
- ❌ `COMPLETED` - Reserva ya finalizada (no envía)
- ❌ `CANCELLED` - Reserva cancelada (no envía)

### Email de Completada ⭐
- ✅ Cuando cambia a `COMPLETED` desde cualquier estado activo
- 👥 Puede ser marcada por: Owner o Empleado

### Email de Cancelada ⭐
- ✅ Cuando cambia a `CANCELLED` desde cualquier estado
- 👥 Puede ser iniciada por: Cliente, Owner, Empleado, o Sistema (automático)
- 📝 Incluye motivo de cancelación (opcional)

### Email de Reprogramada ⭐
- ✅ Cuando cambia a `RESCHEDULED` con nueva fecha
- 👥 Puede ser iniciada por: Cliente o Owner
- 📅 Muestra comparación de fechas (anterior vs nueva)

## 🎨 Diseño de los Emails

### Características de los Templates

**Email de Confirmación:**
- 🎨 Header azul (`#2c3e50`)
- 📋 Tabla de información con bordes
- ⚠️ Caja de alerta amarilla para recordatorio de 15 min
- 📱 Responsive y compatible con clientes de email

**Email Recordatorio:**
- 🎨 Header rojo (`#e74c3c`) - Urgencia
- ⏰ Hora en tamaño 48px - Alta visibilidad
- 🔴 Caja de alerta roja destacada
- 📱 Responsive y compatible con clientes de email

**Email Completada:** ⭐
- 🎨 Header verde (`#4CAF50`) - Éxito
- ✅ Icono de check destacado (48px)
- 💚 Borde verde en info box
- 🙏 Mensaje de agradecimiento

**Email Cancelada:** ⭐
- 🎨 Header rojo (`#f44336`) - Alerta
- ❌ Icono de X destacado (48px)
- 🔴 Borde rojo en info box
- 📝 Sección opcional para motivo
- 💼 Invitación a reagendar

**Email Reprogramada:** ⭐
- 🎨 Header naranja (`#FF9800`) - Cambio
- 🔄 Icono de recarga destacado (48px)
- 🧡 Borde naranja en info box
- 📅 Comparación visual: fecha anterior (tachada) vs nueva (destacada en naranja)
- ⏰ Caja de recordatorio amarilla

### Elementos Comunes
- ✂️ Emoji destacado en header (48px)
- 📊 Información estructurada en cajas
- 🎯 Footer con copyright y año
- 🌐 Codificación UTF-8
- 💻 Fuentes seguras (Arial, sans-serif)
- 📱 Diseño responsive

## 🕐 Configuración de Tareas Programadas

### Recordatorios (cada minuto)
```java
@Scheduled(cron = "0 */1 * * * *")
public void enviarRecordatoriosReservas()
```

**Lógica de ventana de tiempo:**
- **Límite inferior:** Ahora + 14 minutos
- **Límite superior:** Ahora + 16 minutos
- **Ventana:** 2 minutos de margen para asegurar envío

**Ejemplo:**
- Hora actual: 14:00
- Busca reservas entre: 14:14 y 14:16
- Reserva a las 14:15 → ✅ Recibe recordatorio

## 📝 Logging

### Email de Confirmación
```
INFO  - Email de confirmación de reserva enviado a: cliente@email.com
ERROR - Error al enviar email de confirmación de reserva a: cliente@email.com
```

### Email Recordatorio
```
INFO  - Encontradas 3 reservas próximas (15 min) para enviar recordatorios
INFO  - Recordatorio enviado para reserva ID: 123 - Cliente: cliente@email.com
INFO  - Proceso de recordatorios completado. 3 recordatorios enviados
ERROR - Error al enviar recordatorio para reserva ID: 123
```

### Email Completada ⭐
```
INFO  - Email de reserva completada enviado a: cliente@email.com
ERROR - Error enviando email de reserva completada: [mensaje de error]
```

### Email Cancelada ⭐
```
INFO  - Email de reserva cancelada enviado a: cliente@email.com
ERROR - Error enviando email de reserva cancelada: [mensaje de error]
```

### Email Reprogramada ⭐
```
INFO  - Email de reserva reprogramada enviado a: cliente@email.com
ERROR - Error enviando email de reserva reprogramada: [mensaje de error]
```

### Cancelación Automática ⭐
```
INFO  - Encontradas 2 reservas expiradas para cancelación automática
INFO  - Reserva ID 456 cancelada automáticamente. Estado anterior: CONFIRMED, Fecha: 2025-11-14T10:00
INFO  - Proceso de cancelación automática completado. 2 reservas canceladas
ERROR - Error enviando email de cancelación automática para reserva ID: 456
```

## 🛡️ Sistema Anti-Duplicados

### Problema Identificado
Sin protección, el sistema podría enviar múltiples recordatorios para la misma reserva porque:
- La tarea programada ejecuta cada minuto
- Una reserva permanece en la ventana de 14-16 minutos durante ~2 minutos
- Esto podría resultar en 2+ emails al mismo cliente

### Solución Implementada

#### 1. Campo en Base de Datos
**Entidad `Reserva`:**
```java
@Column(name = "recordatorio_enviado", nullable = false, columnDefinition = "boolean default false")
@Builder.Default
private Boolean recordatorioEnviado = false;
```

**Migración SQL:**
- Archivo: `init/migration-add-recordatorio-enviado.sql`
- Agrega columna `recordatorio_enviado` (default: `false`)
- Crea índice optimizado para consultas del scheduler

#### 2. Filtro en Búsqueda
```java
List<Reserva> reservasProximas = reservaRepository.findAll().stream()
    .filter(reserva -> !reserva.getDeleted())
    .filter(reserva -> !reserva.getRecordatorioEnviado()) // ⭐ FILTRO CLAVE
    .filter(reserva -> /* estado PENDING o CONFIRMED */)
    .filter(reserva -> /* ventana 14-16 min */)
    .toList();
```

#### 3. Marca Después de Enviar
```java
// Enviar email
emailService.sendReservaRecordatorioEmail(...);

// ⭐ Marcar como enviado
reserva.setRecordatorioEnviado(true);
reservaRepository.save(reserva);
```

#### 4. Reinicio del Flag

El flag se reinicia a `false` en los siguientes casos:

**a) Reprogramar Reserva:**
```java
// Cliente, Owner o Employee reprograma
reserva.setReservationDate(nuevaFecha);
reserva.setRecordatorioEnviado(false); // ⭐ Nueva fecha = nuevo recordatorio
```
**Razón:** Una reserva reprogramada tiene nueva fecha/hora, necesita nuevo recordatorio.

**b) Completar Reserva:**
```java
// Owner o Employee completa
reserva.setStatus("COMPLETED");
reserva.setRecordatorioEnviado(false); // ⭐ Ya no aplicable
```
**Razón:** Reserva finalizada, recordatorio ya no relevante.

**c) Cancelar Reserva:**
```java
// Cliente, Owner, Employee o Sistema cancela
reserva.setStatus("CANCELLED");
reserva.setRecordatorioEnviado(false); // ⭐ Ya no aplicable
```
**Razón:** Reserva cancelada, no necesita recordatorio.

### Flujo Completo con Anti-Duplicados

```
1. Reserva creada (recordatorioEnviado = false)
   ↓
2. Tarea programada busca reservas 14-16 min antes
   ↓
3. Filtra solo reservas con recordatorioEnviado = false
   ↓
4. Envía email recordatorio
   ↓
5. Marca recordatorioEnviado = true
   ↓
6. Siguiente ejecución del cron
   ↓
7. Ya no encuentra la reserva (filtrada por flag = true)
   ↓
8. ✅ No se envía duplicado
```

### Casos Especiales

#### Caso 1: Error al Enviar Email
```java
try {
    emailService.sendReservaRecordatorioEmail(...);
    reserva.setRecordatorioEnviado(true); // Solo si éxito
    reservaRepository.save(reserva);
} catch (Exception e) {
    log.error("Error al enviar recordatorio...", e);
    // NO se marca como enviado
    // Se reintentará en próxima ejecución
}
```
**Comportamiento:** Si falla el envío, no se marca y se reintenta automáticamente.

#### Caso 2: Reserva Reprogramada
```
Reserva original: 10:00 (recordatorioEnviado = true, ya envió a las 9:45)
Usuario reprograma para: 14:00
Sistema: recordatorioEnviado = false (reinicia flag)
Nuevo recordatorio: 13:45 ✅
```

#### Caso 3: Cancelación Después de Recordatorio
```
Reserva: 10:00
Recordatorio enviado: 9:45 (recordatorioEnviado = true)
Usuario cancela: 9:50
Sistema: recordatorioEnviado = false (limpieza)
Resultado: No se envían más recordatorios ✅
```

### Ventajas del Sistema

1. ✅ **Garantiza unicidad** - Cada reserva recibe exactamente 1 recordatorio
2. ✅ **Tolerante a errores** - Reintenta si falla el envío
3. ✅ **Flexible** - Permite reprogramar con nuevo recordatorio
4. ✅ **Limpio** - Reinicia flag en estados finales
5. ✅ **Performante** - Índice optimizado en BD
6. ✅ **Observable** - Logging detallado

### Índice de Base de Datos

```sql
CREATE INDEX IF NOT EXISTS idx_reservas_recordatorio_fecha 
ON reservas(recordatorio_enviado, reservation_date, status, deleted)
WHERE deleted = false AND recordatorio_enviado = false;
```

**Beneficios:**
- Acelera consulta del scheduler
- Solo indexa reservas elegibles
- Reduce carga en BD

## 🛡️ Manejo de Errores

### Principios de Resiliencia

1. **No bloquear flujo principal:**
   - Si falla el envío de email, la reserva se crea igual
   - Try-catch envuelve todo el proceso de envío

2. **Logging exhaustivo:**
   - Errores se registran con stack trace
   - Éxitos se registran con detalles

3. **Envío asíncrono:**
   - No afecta tiempo de respuesta al usuario
   - Usa thread pool de Spring

4. **Tolerancia a fallos:**
   - Si un recordatorio falla, continúa con los siguientes
   - No interrumpe la tarea programada

## 🔧 Configuración Requerida

### application.properties
```properties
# Configuración de email (ya existente)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# URL base de la aplicación
app.base-url=http://localhost:8080
app.frontend-url=http://localhost:3000
```

## 🧪 Testing

### Probar Email de Confirmación
```bash
# 1. Crear una nueva reserva
POST /api/reservas/client
{
  "serviceId": 1,
  "employeeId": "uuid-empleado",
  "reservationDate": "2025-11-15T10:00:00"
}

# 2. Verificar en logs
INFO  - Email de confirmación de reserva enviado a: ...

# 3. Revisar inbox del cliente
```

### Probar Email Recordatorio
```bash
# 1. Crear reserva para dentro de 15 minutos
POST /api/reservas/client
{
  "reservationDate": "2025-11-14T17:16:00"  # Hora actual + 15 min
}

# 2. Esperar hasta que el cron ejecute (máx 1 minuto)

# 3. Verificar en logs
INFO  - Encontradas 1 reservas próximas (15 min) para enviar recordatorios
INFO  - Recordatorio enviado para reserva ID: ...

# 4. Revisar inbox del cliente
```

## 📐 Ventana de Envío de Recordatorios

### ¿Por qué 14-16 minutos y no exactamente 15?

**Razón:** Garantizar que cada reserva reciba **exactamente 1** recordatorio

**Ejemplo de problema sin ventana:**
```
Reserva: 10:15:00
Cron ejecuta: 10:00:30 → Busca reservas a las 10:15:30 → ❌ No encuentra
Cron ejecuta: 10:01:30 → Busca reservas a las 10:16:30 → ❌ No encuentra
```

**Solución con ventana (14-16 min):**
```
Reserva: 10:15:00
Cron ejecuta: 10:00:30 → Busca entre 10:14:30 y 10:16:30 → ✅ Encuentra 10:15:00
```

## 💡 Mensajes Importantes en los Emails

### En Email de Confirmación
> ⚠️ **IMPORTANTE:** Por favor, **llega al menos 15 minutos antes** de tu cita programada para asegurar que podamos atenderte en el horario establecido.

> Recibirás un recordatorio 15 minutos antes de tu cita.

### En Email Recordatorio
> Tu cita es a las **HH:MM**
> **¡EN 15 MINUTOS! 🏃‍♂️**

> ¡No olvides que debes estar en las instalaciones con anticipación!

## 🚀 Mejoras Futuras

Posibles mejoras para implementar:

1. **Email de cancelación** - Notificar cuando se cancela una reserva
2. **Email de reprogramación** - Notificar cambios de fecha/hora
3. **Email de completación** - Agradecer al cliente y pedir reseña
4. **Múltiples recordatorios** - 24h antes, 1h antes, 15 min antes
5. **Personalización** - Permitir a empresas personalizar templates
6. **SMS** - Alternativa/complemento a emails
7. **WhatsApp** - Integración con WhatsApp Business API
8. **Calendario** - Adjuntar archivo .ics para agregar al calendario

## 📚 Referencias

- [Spring Mail](https://docs.spring.io/spring-framework/reference/integration/email.html)
- [Spring @Async](https://spring.io/guides/gs/async-method/)
- [HTML Email Best Practices](https://www.campaignmonitor.com/dev-resources/guides/coding-html-emails/)
- [Cron Expressions](https://crontab.guru/)

## ✅ Resumen de Cambios

### Archivos Modificados
1. ✅ `EmailService.java` - Agregados 2 métodos
2. ✅ `EmailServiceImpl.java` - Implementados 2 métodos + 2 templates
3. ✅ `ReservaClientService.java` - Integrado envío de email confirmación
4. ✅ `ReservaScheduledService.java` - Agregada tarea de recordatorios

### Funcionalidades Agregadas
1. ✅ Email de confirmación al crear reserva
2. ✅ Recordatorio de 15 min antes (llegar con anticipación)
3. ✅ Tarea programada cada minuto para recordatorios
4. ✅ Templates HTML profesionales y responsive
5. ✅ Manejo de errores sin bloquear flujo
6. ✅ Logging completo para auditoría

### Compilación
✅ **BUILD SUCCESS** - Sin errores de compilación
