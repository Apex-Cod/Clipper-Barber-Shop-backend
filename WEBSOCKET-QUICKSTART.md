# 🚀 Quick Start: WebSocket Notifications

## 📦 Paso 1: Instalar Dependencias en Expo

```bash
npm install @stomp/stompjs sockjs-client
```

## 🔌 Paso 2: Crear Servicio (copiar desde WEBSOCKET-IMPLEMENTATION.md)

Archivo: `services/WebSocketService.ts` - Ver documentación completa

## 🎣 Paso 3: Usar el Hook

```typescript
import { useWebSocket } from './hooks/useWebSocket';

const App = () => {
  const { connected, notifications } = useWebSocket({
    url: 'http://TU_IP_LOCAL:8080/ws', // ⚠️ Cambia esto
    userId: 'user-123',
    empresaId: 1,
    onNotification: (notif) => {
      console.log('Nueva notificación:', notif);
    }
  });

  return (
    <View>
      <Text>Estado: {connected ? '🟢' : '🔴'}</Text>
      <Text>Notificaciones: {notifications.length}</Text>
    </View>
  );
};
```

## 🧪 Paso 4: Probar

### Desde tu navegador:
```
http://localhost:8080/api/test/websocket/simulate-reserva?empresaId=1&clientId=user-123&ownerId=owner-456
```

### Desde tu terminal:
```bash
curl "http://localhost:8080/api/test/websocket/simulate-reserva?empresaId=1&clientId=user-123&ownerId=owner-456"
```

## 📱 Paso 5: Ver Resultado

Tu app de Expo debería mostrar:
- ✅ Alerta con título y mensaje
- 📋 Notificación en la lista
- 🔔 Datos de la reserva

## 🎯 Endpoints de Prueba

- **Global:** `GET /api/test/websocket/send-global`
- **Usuario:** `GET /api/test/websocket/send-user?userId=user-123`
- **Empresa:** `GET /api/test/websocket/send-empresa?empresaId=1`
- **Simular Reserva:** `GET /api/test/websocket/simulate-reserva`
- **Health:** `GET /api/test/websocket/health`

## 🔍 Obtener tu IP Local

### Windows:
```bash
ipconfig
# Busca "IPv4 Address" en tu adaptador de red activo
```

### Mac/Linux:
```bash
ifconfig | grep "inet "
# O
ip addr show
```

### Ejemplo:
Si tu IP es `192.168.1.100`, usa:
```typescript
url: 'http://192.168.1.100:8080/ws'
```

## ⚡ Compilar y Ejecutar Backend

```bash
mvn clean install
mvn spring-boot:run
```

## 🎉 ¡Listo!

Ahora cuando se cree una reserva real, verás notificaciones en tiempo real en tu app de Expo.

Ver documentación completa en: **WEBSOCKET-IMPLEMENTATION.md**
