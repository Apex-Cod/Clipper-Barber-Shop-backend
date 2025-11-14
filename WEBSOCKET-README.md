# 🔔 WebSocket - Índice de Documentación

## 📚 Guías Disponibles

### 🚀 Para Empezar Rápido
- **[WEBSOCKET-QUICKSTART.md](WEBSOCKET-QUICKSTART.md)** - Inicio rápido en 5 pasos

### 💻 Código Listo para Usar
- **[WEBSOCKET-CODIGO-EXPO.md](WEBSOCKET-CODIGO-EXPO.md)** - Código completo para copiar y pegar en Expo

### 📖 Documentación Completa
- **[WEBSOCKET-IMPLEMENTATION.md](WEBSOCKET-IMPLEMENTATION.md)** - Guía detallada de implementación

### 📊 Resumen Técnico
- **[WEBSOCKET-RESUMEN.md](WEBSOCKET-RESUMEN.md)** - Resumen de cambios y arquitectura

---

## 🎯 ¿Qué Guía Debo Usar?

### Si nunca has usado WebSocket:
➡️ Empieza con **WEBSOCKET-QUICKSTART.md**

### Si quieres código listo para usar:
➡️ Ve directo a **WEBSOCKET-CODIGO-EXPO.md**

### Si quieres entender todo en detalle:
➡️ Lee **WEBSOCKET-IMPLEMENTATION.md**

### Si quieres un resumen de los cambios:
➡️ Consulta **WEBSOCKET-RESUMEN.md**

---

## ⚡ Quick Start Ultra Rápido

```bash
# 1. Backend - Ya está listo, solo inicia
mvn spring-boot:run

# 2. Frontend - Instala dependencias
npm install @stomp/stompjs sockjs-client

# 3. Copia el código de WEBSOCKET-CODIGO-EXPO.md

# 4. Cambia la IP en tu código
url: 'http://TU_IP_LOCAL:8080/ws'

# 5. Prueba
curl "http://localhost:8080/api/test/websocket/simulate-reserva?empresaId=1&clientId=user-123"
```

---

## 🎬 Demo Rápida

### Desde el navegador:
1. Abre: `http://localhost:8080/api/test/websocket/health`
2. Deberías ver el status del servicio

### Simular notificación:
```
http://localhost:8080/api/test/websocket/simulate-reserva?empresaId=1&clientId=user-123&ownerId=owner-456
```

---

## 📡 Endpoints Disponibles

### WebSocket
- `ws://localhost:8080/ws` - Conexión principal

### REST API de Pruebas
- `GET /api/test/websocket/health` - Health check
- `GET /api/test/websocket/send-global` - Notificación global
- `GET /api/test/websocket/send-user?userId=X` - Notificación a usuario
- `GET /api/test/websocket/send-empresa?empresaId=X` - Notificación a empresa
- `GET /api/test/websocket/simulate-reserva` - Simular reserva

---

## 🔔 Tipos de Notificaciones

| Tipo | Cuándo se envía |
|------|-----------------|
| `RESERVA_CREADA` | Cliente crea una reserva |
| `RESERVA_CONFIRMADA` | Owner confirma la reserva |
| `RESERVA_CANCELADA` | Se cancela una reserva |
| `RESERVA_REPROGRAMADA` | Se cambia fecha/hora |
| `RESERVA_COMPLETADA` | Se marca como completada |
| `RESERVA_ACTUALIZADA` | Se actualiza información |

---

## 🎯 Canales de Suscripción

| Canal | Descripción | Quién lo recibe |
|-------|-------------|-----------------|
| `/topic/reservas` | Global | Todos los conectados |
| `/topic/empresa/{id}` | Por empresa | Usuarios de esa empresa |
| `/user/queue/notifications` | Personal | Solo ese usuario |

---

## 🛠️ Tecnologías

### Backend
- Spring Boot 3.5.6
- Spring WebSocket
- STOMP Protocol
- SockJS fallback

### Frontend
- React Native (Expo)
- @stomp/stompjs
- sockjs-client

---

## 📱 Estructura del Proyecto Frontend

```
src/
├── services/
│   └── WebSocketService.ts       # Servicio de WebSocket
├── hooks/
│   └── useWebSocket.ts            # Hook de React
└── screens/
    └── ReservasScreen.tsx         # Componente de ejemplo
```

---

## ✅ Checklist de Implementación

### Backend (✅ Completado)
- [x] Dependencias agregadas
- [x] WebSocket configurado
- [x] DTOs creados
- [x] Servicio de notificaciones
- [x] Integrado con reservas
- [x] Endpoints de prueba
- [x] Seguridad configurada

### Frontend (⏳ Pendiente)
- [ ] Instalar dependencias
- [ ] Crear WebSocketService
- [ ] Crear hook useWebSocket
- [ ] Crear componente UI
- [ ] Probar conexión
- [ ] Probar notificaciones

---

## 🐛 Solución de Problemas

### Backend no inicia
```bash
mvn clean install
mvn spring-boot:run
```

### No puedo conectar desde Expo
1. Verifica tu IP local (no uses `localhost`)
2. Asegúrate que el backend está corriendo
3. Verifica que estás en la misma red WiFi

### No recibo notificaciones
1. Verifica que `userId` y `empresaId` son correctos
2. Mira los logs del backend
3. Verifica el badge de conexión (debe estar verde)

---

## 📞 Soporte

Si tienes problemas:
1. Revisa la sección de Troubleshooting en `WEBSOCKET-IMPLEMENTATION.md`
2. Verifica los logs del backend
3. Verifica la consola de Expo
4. Prueba los endpoints REST de prueba

---

## 🎉 ¡Listo para Empezar!

Sigue cualquiera de las guías según tu nivel de experiencia y necesidades.

**Recomendación:** Empieza con `WEBSOCKET-QUICKSTART.md` para una experiencia guiada paso a paso.

---

## 📝 Notas Adicionales

- El sistema envía notificaciones automáticamente cuando se crea/actualiza/cancela una reserva
- Las notificaciones se envían a múltiples destinatarios (cliente, empleado, empresa)
- El sistema incluye reconexión automática
- Compatible con Expo Go (sin módulos nativos)
- Funciona en desarrollo y producción

---

**Última actualización:** 3 de Noviembre, 2025
**Versión Backend:** Spring Boot 3.5.6
**Compatible con:** React Native / Expo Go
