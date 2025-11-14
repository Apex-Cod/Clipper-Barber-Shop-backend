# 📋 Código Listo para Copiar - WebSocket en Expo

## 📦 Instalación

```bash
npm install @stomp/stompjs sockjs-client
```

---

## 📄 Archivo 1: `services/WebSocketService.ts`

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

  connect(
    url: string,
    onConnect?: () => void,
    onError?: (error: any) => void
  ): void {
    if (this.client && this.client.connected) {
      console.log('Ya existe una conexión activa');
      return;
    }

    this.client = new Client({
      webSocketFactory: () => new SockJS(url),
      debug: (str) => console.log('STOMP:', str),
      reconnectDelay: this.reconnectDelay,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        console.log('✅ Conectado');
        this.reconnectAttempts = 0;
        onConnect?.();
      },

      onStompError: (frame) => {
        console.error('❌ Error STOMP:', frame);
        onError?.(frame);
      },

      onWebSocketError: (event) => {
        console.error('❌ Error WebSocket:', event);
        onError?.(event);
      },

      onDisconnect: () => {
        console.log('⚠️ Desconectado');
        this.handleReconnect();
      },
    });

    this.client.activate();
  }

  private handleReconnect(): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`🔄 Reconectando (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
      setTimeout(() => this.client?.activate(), this.reconnectDelay * this.reconnectAttempts);
    } else {
      console.error('❌ Máximo de intentos alcanzado');
    }
  }

  subscribeToReservas(callback: (notification: NotificationMessage) => void): void {
    this.subscribe('/topic/reservas', 'reservas', callback);
  }

  subscribeToEmpresa(empresaId: number, callback: (notification: NotificationMessage) => void): void {
    this.subscribe(`/topic/empresa/${empresaId}`, `empresa-${empresaId}`, callback);
  }

  subscribeToUser(userId: string, callback: (notification: NotificationMessage) => void): void {
    this.subscribe(`/user/queue/notifications`, `user-${userId}`, callback);
  }

  private subscribe(
    destination: string,
    key: string,
    callback: (notification: NotificationMessage) => void
  ): void {
    if (!this.client || !this.client.connected) {
      console.error('❌ No conectado');
      return;
    }

    if (this.subscriptions.has(key)) {
      this.subscriptions.get(key)?.unsubscribe();
    }

    const subscription = this.client.subscribe(destination, (message) => {
      try {
        const notification: NotificationMessage = JSON.parse(message.body);
        console.log('📩 Notificación:', notification);
        callback(notification);
      } catch (error) {
        console.error('Error al parsear:', error);
      }
    });

    this.subscriptions.set(key, subscription);
    console.log(`✅ Suscrito a: ${destination}`);
  }

  unsubscribe(key: string): void {
    this.subscriptions.get(key)?.unsubscribe();
    this.subscriptions.delete(key);
  }

  disconnect(): void {
    this.subscriptions.forEach((sub) => sub.unsubscribe());
    this.subscriptions.clear();
    this.client?.deactivate();
    this.client = null;
    console.log('✅ Desconectado');
  }

  isConnected(): boolean {
    return this.client?.connected ?? false;
  }
}

export default new WebSocketService();
```

---

## 📄 Archivo 2: `hooks/useWebSocket.ts`

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

  const handleNotification = useCallback(
    (notification: NotificationMessage) => {
      setNotifications((prev) => [notification, ...prev]);

      Alert.alert(notification.title, notification.message, [
        { text: 'OK', onPress: () => console.log('OK') },
      ]);

      onNotification?.(notification);
    },
    [onNotification]
  );

  useEffect(() => {
    if (autoConnect) {
      WebSocketService.connect(
        url,
        () => {
          setConnected(true);
          WebSocketService.subscribeToReservas(handleNotification);
          
          if (empresaId) {
            WebSocketService.subscribeToEmpresa(empresaId, handleNotification);
          }
          
          if (userId) {
            WebSocketService.subscribeToUser(userId, handleNotification);
          }
        },
        (error) => {
          console.error('Error:', error);
          setConnected(false);
        }
      );
    }

    return () => {
      WebSocketService.disconnect();
      setConnected(false);
    };
  }, [url, userId, empresaId, autoConnect, handleNotification]);

  const clearNotifications = useCallback(() => {
    setNotifications([]);
  }, []);

  return { connected, notifications, clearNotifications };
};
```

---

## 📄 Archivo 3: Componente de Ejemplo `ReservasScreen.tsx`

```typescript
import React from 'react';
import { View, Text, FlatList, StyleSheet, TouchableOpacity } from 'react-native';
import { useWebSocket } from '../hooks/useWebSocket';

export const ReservasScreen = () => {
  // ⚠️ CAMBIAR ESTOS VALORES POR LOS REALES
  const userId = 'user-uuid-123';
  const empresaId = 1;
  const serverIP = '192.168.1.100'; // 👈 TU IP LOCAL

  const { connected, notifications, clearNotifications } = useWebSocket({
    url: `http://${serverIP}:8080/ws`,
    userId,
    empresaId,
    autoConnect: true,
    onNotification: (notif) => {
      console.log('Nueva notificación:', notif);
      // Aquí puedes agregar más lógica:
      // - Reproducir sonido
      // - Vibrar
      // - Actualizar UI
      // - etc.
    },
  });

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Mis Reservas</Text>
        <View style={[styles.badge, { backgroundColor: connected ? '#4CAF50' : '#F44336' }]}>
          <Text style={styles.badgeText}>{connected ? '🟢 En línea' : '🔴 Fuera de línea'}</Text>
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.subtitle}>Notificaciones ({notifications.length})</Text>
        {notifications.length > 0 && (
          <TouchableOpacity onPress={clearNotifications} style={styles.clearButton}>
            <Text style={styles.clearButtonText}>Limpiar</Text>
          </TouchableOpacity>
        )}
      </View>

      <FlatList
        data={notifications}
        keyExtractor={(item, index) => `${item.timestamp}-${index}`}
        renderItem={({ item }) => (
          <View style={styles.card}>
            <View style={styles.cardHeader}>
              <Text style={styles.cardTitle}>{item.title}</Text>
              <Text style={[styles.priority, getPriorityStyle(item.priority)]}>
                {item.priority}
              </Text>
            </View>
            
            <Text style={styles.cardMessage}>{item.message}</Text>
            
            <Text style={styles.cardTime}>
              {new Date(item.timestamp).toLocaleString('es-ES')}
            </Text>

            {item.data && (
              <View style={styles.details}>
                <DetailRow label="Servicio" value={item.data.serviceName} />
                <DetailRow label="Cliente" value={item.data.clientName} />
                <DetailRow label="Empleado" value={item.data.employeeName} />
                <DetailRow 
                  label="Fecha" 
                  value={new Date(item.data.reservationDate).toLocaleString('es-ES')} 
                />
                <DetailRow label="Precio" value={`$${item.data.finalPrice}`} />
                <DetailRow label="Estado" value={item.data.status} />
              </View>
            )}
          </View>
        )}
        ListEmptyComponent={
          <View style={styles.empty}>
            <Text style={styles.emptyText}>📭</Text>
            <Text style={styles.emptySubtext}>No hay notificaciones</Text>
          </View>
        }
      />
    </View>
  );
};

const DetailRow = ({ label, value }: { label: string; value: string }) => (
  <View style={styles.detailRow}>
    <Text style={styles.detailLabel}>{label}:</Text>
    <Text style={styles.detailValue}>{value}</Text>
  </View>
);

const getPriorityStyle = (priority: string) => {
  switch (priority) {
    case 'HIGH':
    case 'URGENT':
      return { color: '#F44336' };
    case 'MEDIUM':
      return { color: '#FF9800' };
    case 'LOW':
      return { color: '#4CAF50' };
    default:
      return { color: '#999' };
  }
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
    padding: 16,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#333',
  },
  badge: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 20,
  },
  badgeText: {
    color: 'white',
    fontSize: 12,
    fontWeight: 'bold',
  },
  section: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  subtitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#333',
  },
  clearButton: {
    backgroundColor: '#FF5722',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 8,
  },
  clearButtonText: {
    color: 'white',
    fontSize: 12,
    fontWeight: 'bold',
  },
  card: {
    backgroundColor: 'white',
    padding: 16,
    borderRadius: 12,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    flex: 1,
  },
  priority: {
    fontSize: 12,
    fontWeight: 'bold',
  },
  cardMessage: {
    fontSize: 14,
    color: '#666',
    marginBottom: 8,
  },
  cardTime: {
    fontSize: 12,
    color: '#999',
    marginBottom: 12,
  },
  details: {
    borderTopWidth: 1,
    borderTopColor: '#eee',
    paddingTop: 12,
  },
  detailRow: {
    flexDirection: 'row',
    marginBottom: 6,
  },
  detailLabel: {
    fontSize: 13,
    fontWeight: 'bold',
    color: '#333',
    width: 80,
  },
  detailValue: {
    fontSize: 13,
    color: '#666',
    flex: 1,
  },
  empty: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 64,
  },
  emptyText: {
    fontSize: 64,
    marginBottom: 8,
  },
  emptySubtext: {
    fontSize: 16,
    color: '#999',
  },
});
```

---

## 🧪 Probar Rápidamente

### 1. Desde tu navegador (mientras la app está abierta):

```
http://localhost:8080/api/test/websocket/simulate-reserva?empresaId=1&clientId=user-uuid-123&ownerId=owner-456
```

### 2. Desde terminal:

```bash
curl "http://localhost:8080/api/test/websocket/simulate-reserva?empresaId=1&clientId=user-uuid-123&ownerId=owner-456"
```

---

## 📝 Checklist de Implementación

- [ ] Instalar dependencias: `npm install @stomp/stompjs sockjs-client`
- [ ] Crear `services/WebSocketService.ts`
- [ ] Crear `hooks/useWebSocket.ts`
- [ ] Crear componente de ejemplo
- [ ] Obtener IP local de tu computadora
- [ ] Actualizar `serverIP` en el componente
- [ ] Actualizar `userId` y `empresaId` con valores reales
- [ ] Iniciar backend: `mvn spring-boot:run`
- [ ] Iniciar Expo: `npm start`
- [ ] Probar conexión (verificar badge verde)
- [ ] Probar notificación con endpoint de test
- [ ] Crear reserva real y verificar notificación

---

## 🔍 Obtener IP Local

### Windows (PowerShell):
```powershell
ipconfig | findstr "IPv4"
```

### Mac/Linux:
```bash
ifconfig | grep "inet " | grep -v 127.0.0.1
```

### Ejemplo de resultado:
```
192.168.1.100  👈 Usa esta IP
```

---

## ✅ ¡Todo listo para copiar y usar!

Solo necesitas:
1. Copiar los 3 archivos de código
2. Cambiar tu IP local
3. Iniciar el backend
4. ¡Probar!

🎉 **¡Disfruta de las notificaciones en tiempo real!**
