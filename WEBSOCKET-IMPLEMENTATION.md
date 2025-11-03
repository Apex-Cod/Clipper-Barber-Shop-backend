# 🔔 Implementación de WebSockets con STOMP en React Native (Expo Go)

## 📋 Tabla de Contenidos
- [Descripción General](#descripción-general)
- [Backend: Spring Boot WebSocket](#backend-spring-boot-websocket)
- [Frontend: React Native + Expo](#frontend-react-native--expo)
- [Ejemplos de Uso](#ejemplos-de-uso)
- [Pruebas](#pruebas)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Descripción General

Este sistema implementa notificaciones en tiempo real usando WebSocket con el protocolo STOMP para comunicación bidireccional entre el backend Spring Boot y aplicaciones móviles React Native (Expo Go).

### Características
- ✅ Notificaciones en tiempo real cuando se crea/actualiza/cancela una reserva
- ✅ Soporte para notificaciones globales, por empresa y por usuario
- ✅ Compatible con Expo Go (sin módulos nativos)
- ✅ Reconexión automática
- ✅ Manejo de errores robusto

---

## 🖥️ Backend: Spring Boot WebSocket

### 1. Dependencias Agregadas

En el `pom.xml` ya se agregó:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### 2. Configuración WebSocket

Archivo: `WebSocketConfig.java`

**Endpoints disponibles:**
- `ws://localhost:8080/ws` - Endpoint principal de WebSocket

**Destinos de suscripción:**
- `/topic/reservas` - Notificaciones globales de reservas
- `/topic/empresa/{empresaId}` - Notificaciones para una empresa específica
- `/queue/user/{userId}` - Notificaciones privadas para un usuario
- `/user/queue/notifications` - Notificaciones privadas (automático)

### 3. Estructura de Mensajes

#### NotificationMessage DTO

```json
{
  "type": "RESERVA_CREADA",
  "title": "Nueva Reserva",
  "message": "Se ha creado una nueva reserva",
  "timestamp": "2025-11-03T14:30:00",
  "resourceId": 123,
  "resourceType": "reserva",
  "priority": "HIGH",
  "empresaId": 1,
  "userId": "user-uuid",
  "requiresAction": true,
  "actionUrl": "/reservas/123",
  "data": {
    "id": 123,
    "serviceName": "Corte de Cabello",
    "clientName": "Juan Pérez",
    "employeeId": "emp-uuid",
    "employeeName": "Carlos Barbero",
    "reservationDate": "2025-11-05T10:00:00",
    "finalPrice": 25.00,
    "status": "PENDING"
  }
}
```

#### Tipos de Notificaciones

```typescript
enum NotificationType {
  RESERVA_CREADA = "Nueva reserva creada",
  RESERVA_CONFIRMADA = "Reserva confirmada",
  RESERVA_CANCELADA = "Reserva cancelada",
  RESERVA_REPROGRAMADA = "Reserva reprogramada",
  RESERVA_COMPLETADA = "Reserva completada",
  RESERVA_ACTUALIZADA = "Reserva actualizada"
}
```

### 4. Seguridad

El endpoint `/ws/**` está permitido sin autenticación en `SecurityConfig.java`, pero puedes agregar autenticación JWT en los mensajes STOMP si lo necesitas.

---

## 📱 Frontend: React Native + Expo

### 1. Instalación de Dependencias

```bash
# En tu proyecto de Expo
npm install @stomp/stompjs sockjs-client
npm install --save-dev @types/sockjs-client  # Si usas TypeScript
```

### 2. Servicio WebSocket

Crea un archivo `services/WebSocketService.ts`:

```typescript
import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface NotificationMessage {
  type: string;
  title: string;
  message: string;
  timestamp: string;
  resourceId?: number;
  resourceType?: string;
  priority: string;
  empresaId?: number;
  userId?: string;
  requiresAction?: boolean;
  actionUrl?: string;
  data?: Record<string, any>;
}

class WebSocketService {
  private client: Client | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;

  /**
   * Conecta al servidor WebSocket
   */
  connect(
    url: string,
    onConnect?: () => void,
    onError?: (error: any) => void
  ): void {
    if (this.client && this.client.connected) {
      console.log('Ya existe una conexión activa');
      return;
    }

    // Crear cliente STOMP
    this.client = new Client({
      webSocketFactory: () => new SockJS(url),
      
      debug: (str) => {
        console.log('STOMP Debug:', str);
      },
      
      reconnectDelay: this.reconnectDelay,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        console.log('✅ Conectado al servidor WebSocket');
        this.reconnectAttempts = 0;
        onConnect?.();
      },

      onStompError: (frame) => {
        console.error('❌ Error STOMP:', frame.headers['message']);
        console.error('Detalles:', frame.body);
        onError?.(frame);
      },

      onWebSocketError: (event) => {
        console.error('❌ Error WebSocket:', event);
        onError?.(event);
      },

      onDisconnect: () => {
        console.log('⚠️ Desconectado del servidor WebSocket');
        this.handleReconnect();
      },
    });

    // Activar cliente
    this.client.activate();
  }

  /**
   * Maneja la reconexión automática
   */
  private handleReconnect(): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(
        `🔄 Intentando reconectar (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`
      );
      
      setTimeout(() => {
        this.client?.activate();
      }, this.reconnectDelay * this.reconnectAttempts);
    } else {
      console.error('❌ Máximo de intentos de reconexión alcanzado');
    }
  }

  /**
   * Suscribirse a notificaciones globales de reservas
   */
  subscribeToReservas(
    callback: (notification: NotificationMessage) => void
  ): void {
    this.subscribe('/topic/reservas', 'reservas', callback);
  }

  /**
   * Suscribirse a notificaciones de una empresa específica
   */
  subscribeToEmpresa(
    empresaId: number,
    callback: (notification: NotificationMessage) => void
  ): void {
    this.subscribe(
      `/topic/empresa/${empresaId}`,
      `empresa-${empresaId}`,
      callback
    );
  }

  /**
   * Suscribirse a notificaciones privadas de usuario
   */
  subscribeToUser(
    userId: string,
    callback: (notification: NotificationMessage) => void
  ): void {
    this.subscribe(
      `/user/queue/notifications`,
      `user-${userId}`,
      callback
    );
  }

  /**
   * Método genérico de suscripción
   */
  private subscribe(
    destination: string,
    key: string,
    callback: (notification: NotificationMessage) => void
  ): void {
    if (!this.client || !this.client.connected) {
      console.error('❌ No hay conexión activa. Conecta primero.');
      return;
    }

    // Cancelar suscripción previa si existe
    if (this.subscriptions.has(key)) {
      this.subscriptions.get(key)?.unsubscribe();
    }

    // Nueva suscripción
    const subscription = this.client.subscribe(destination, (message) => {
      try {
        const notification: NotificationMessage = JSON.parse(message.body);
        console.log('📩 Notificación recibida:', notification);
        callback(notification);
      } catch (error) {
        console.error('Error al parsear notificación:', error);
      }
    });

    this.subscriptions.set(key, subscription);
    console.log(`✅ Suscrito a: ${destination}`);
  }

  /**
   * Cancelar una suscripción específica
   */
  unsubscribe(key: string): void {
    const subscription = this.subscriptions.get(key);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(key);
      console.log(`✅ Desuscrito de: ${key}`);
    }
  }

  /**
   * Desconectar del servidor
   */
  disconnect(): void {
    // Cancelar todas las suscripciones
    this.subscriptions.forEach((sub) => sub.unsubscribe());
    this.subscriptions.clear();

    // Desactivar cliente
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }

    console.log('✅ Desconectado y limpiado');
  }

  /**
   * Verifica si está conectado
   */
  isConnected(): boolean {
    return this.client?.connected ?? false;
  }
}

// Exportar instancia singleton
export default new WebSocketService();
```

### 3. Hook de React para Notificaciones

Crea un archivo `hooks/useWebSocket.ts`:

```typescript
import { useEffect, useState, useCallback } from 'react';
import { Alert } from 'react-native';
import WebSocketService, { NotificationMessage } from '../services/WebSocketService';

interface UseWebSocketOptions {
  url: string;
  userId?: string;
  empresaId?: number;
  autoConnect?: boolean;
  onNotification?: (notification: NotificationMessage) => void;
}

export const useWebSocket = ({
  url,
  userId,
  empresaId,
  autoConnect = true,
  onNotification,
}: UseWebSocketOptions) => {
  const [connected, setConnected] = useState(false);
  const [notifications, setNotifications] = useState<NotificationMessage[]>([]);

  // Manejador de notificaciones
  const handleNotification = useCallback(
    (notification: NotificationMessage) => {
      // Agregar a la lista de notificaciones
      setNotifications((prev) => [notification, ...prev]);

      // Mostrar alerta
      Alert.alert(
        notification.title,
        notification.message,
        [
          {
            text: 'OK',
            onPress: () => console.log('Notificación vista'),
          },
        ]
      );

      // Callback personalizado
      onNotification?.(notification);
    },
    [onNotification]
  );

  // Conectar al WebSocket
  useEffect(() => {
    if (autoConnect) {
      WebSocketService.connect(
        url,
        () => {
          setConnected(true);

          // Suscribirse a notificaciones globales
          WebSocketService.subscribeToReservas(handleNotification);

          // Suscribirse a notificaciones de empresa si se proporciona
          if (empresaId) {
            WebSocketService.subscribeToEmpresa(empresaId, handleNotification);
          }

          // Suscribirse a notificaciones de usuario si se proporciona
          if (userId) {
            WebSocketService.subscribeToUser(userId, handleNotification);
          }
        },
        (error) => {
          console.error('Error de conexión:', error);
          setConnected(false);
        }
      );
    }

    // Cleanup
    return () => {
      WebSocketService.disconnect();
      setConnected(false);
    };
  }, [url, userId, empresaId, autoConnect, handleNotification]);

  // Limpiar notificaciones
  const clearNotifications = useCallback(() => {
    setNotifications([]);
  }, []);

  return {
    connected,
    notifications,
    clearNotifications,
  };
};
```

### 4. Componente de Ejemplo

```typescript
import React from 'react';
import { View, Text, FlatList, StyleSheet } from 'react-native';
import { useWebSocket } from '../hooks/useWebSocket';

export const ReservasScreen = () => {
  // Obtén estos datos del contexto de autenticación
  const userId = 'user-uuid-123'; // ID del usuario autenticado
  const empresaId = 1; // ID de la empresa (si es owner/empleado)
  
  const { connected, notifications, clearNotifications } = useWebSocket({
    url: 'http://192.168.1.100:8080/ws', // ⚠️ Cambiar por tu IP local
    userId,
    empresaId,
    autoConnect: true,
    onNotification: (notification) => {
      console.log('Nueva notificación:', notification);
      
      // Aquí puedes agregar lógica adicional:
      // - Actualizar lista de reservas
      // - Reproducir sonido
      // - Mostrar badge
      // - Enviar notificación push local
    },
  });

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Mis Reservas</Text>
        <View
          style={[
            styles.statusIndicator,
            { backgroundColor: connected ? '#4CAF50' : '#F44336' },
          ]}
        >
          <Text style={styles.statusText}>
            {connected ? '🟢 Conectado' : '🔴 Desconectado'}
          </Text>
        </View>
      </View>

      <Text style={styles.subtitle}>
        Notificaciones recientes ({notifications.length})
      </Text>

      <FlatList
        data={notifications}
        keyExtractor={(item, index) => `${item.timestamp}-${index}`}
        renderItem={({ item }) => (
          <View style={styles.notificationCard}>
            <Text style={styles.notificationTitle}>{item.title}</Text>
            <Text style={styles.notificationMessage}>{item.message}</Text>
            <Text style={styles.notificationTime}>
              {new Date(item.timestamp).toLocaleString()}
            </Text>
            
            {item.data && (
              <View style={styles.dataContainer}>
                <Text style={styles.dataLabel}>Servicio:</Text>
                <Text style={styles.dataValue}>{item.data.serviceName}</Text>
                
                <Text style={styles.dataLabel}>Fecha:</Text>
                <Text style={styles.dataValue}>
                  {new Date(item.data.reservationDate).toLocaleString()}
                </Text>
                
                <Text style={styles.dataLabel}>Precio:</Text>
                <Text style={styles.dataValue}>${item.data.finalPrice}</Text>
              </View>
            )}
          </View>
        )}
        ListEmptyComponent={
          <Text style={styles.emptyText}>No hay notificaciones</Text>
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
    backgroundColor: '#f5f5f5',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
  },
  statusIndicator: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 20,
  },
  statusText: {
    color: 'white',
    fontSize: 12,
    fontWeight: 'bold',
  },
  subtitle: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 12,
  },
  notificationCard: {
    backgroundColor: 'white',
    padding: 16,
    borderRadius: 8,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  notificationTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  notificationMessage: {
    fontSize: 14,
    color: '#666',
    marginBottom: 8,
  },
  notificationTime: {
    fontSize: 12,
    color: '#999',
  },
  dataContainer: {
    marginTop: 12,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#eee',
  },
  dataLabel: {
    fontSize: 12,
    fontWeight: 'bold',
    color: '#333',
    marginTop: 4,
  },
  dataValue: {
    fontSize: 14,
    color: '#666',
    marginBottom: 4,
  },
  emptyText: {
    textAlign: 'center',
    color: '#999',
    marginTop: 32,
  },
});
```

---

## 🧪 Pruebas

### 1. Compilar el Backend

```bash
# En el directorio del proyecto backend
mvn clean install
mvn spring-boot:run
```

### 2. Probar Conexión desde Postman/Browser

Puedes usar una extensión de navegador para probar WebSocket:
- **URL:** `ws://localhost:8080/ws`
- **Suscribirse a:** `/topic/reservas`

### 3. Crear una Reserva

```bash
curl -X POST http://localhost:8080/api/client/reservas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <tu-jwt-token>" \
  -d '{
    "serviceId": 1,
    "employeeId": "emp-uuid",
    "reservationDate": "2025-11-05T10:00:00",
    "promocionId": null
  }'
```

Deberías ver una notificación en:
- El cliente WebSocket (Postman/Browser)
- Tu app de Expo (si está conectada)

### 4. Verificar Logs

En el backend deberías ver:
```
Enviando notificación global: RESERVA_CREADA
Enviando notificación al usuario user-uuid: RESERVA_CREADA
Enviando notificación a empresa 1: RESERVA_CREADA
```

---

## 🐛 Troubleshooting

### Problema: "Cannot connect to WebSocket"

**Solución:**
1. Verifica que el backend esté corriendo en el puerto 8080
2. Usa la IP local correcta (no `localhost` en dispositivo físico)
3. Verifica que `/ws/**` esté permitido en SecurityConfig

```typescript
// ❌ No funciona en dispositivo físico
url: 'http://localhost:8080/ws'

// ✅ Usa tu IP local
url: 'http://192.168.1.100:8080/ws'
```

### Problema: "CORS error"

**Solución:** Actualiza `WebSocketConfig.java`:

```java
@Override
public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*") // O específica tus orígenes
            .withSockJS();
}
```

### Problema: "No recibo notificaciones"

**Solución:**
1. Verifica que el userId y empresaId sean correctos
2. Revisa los logs del backend para ver si se envían las notificaciones
3. Asegúrate de que las suscripciones estén activas

```typescript
// Verificar estado
console.log('Conectado:', WebSocketService.isConnected());
```

### Problema: "App se congela o crashea"

**Solución:**
1. Asegúrate de desconectar en el cleanup del useEffect
2. Maneja errores en los callbacks
3. Usa try-catch en el parsing de mensajes

---

## 📚 Recursos Adicionales

- [STOMP Protocol](https://stomp.github.io/)
- [Spring WebSocket Docs](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket)
- [SockJS Client](https://github.com/sockjs/sockjs-client)
- [@stomp/stompjs](https://github.com/stomp-js/stompjs)

---

## 🎉 ¡Listo!

Ahora tu aplicación puede:
✅ Recibir notificaciones en tiempo real
✅ Mostrar alertas cuando se cree/actualice una reserva
✅ Mantener a los usuarios informados sin necesidad de hacer refresh
✅ Funcionar en Expo Go sin módulos nativos

**Próximos pasos sugeridos:**
- Agregar notificaciones push nativas
- Implementar badge de notificaciones no leídas
- Agregar sonidos personalizados
- Implementar historial de notificaciones persistente
