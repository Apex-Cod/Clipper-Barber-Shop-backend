package apex.code.clipperBarberShop.shared.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket para notificaciones en tiempo real
 * 
 * Endpoints disponibles:
 * - /ws: Endpoint principal de WebSocket
 * 
 * Destinos de suscripción:
 * - /topic/reservas: Notificaciones públicas de reservas para todos
 * - /queue/user/{userId}: Notificaciones privadas para un usuario específico
 * - /topic/empresa/{empresaId}: Notificaciones para una empresa específica
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita un simple message broker para enviar mensajes a los clientes
        // con destinos que empiecen con "/topic" (público) o "/queue" (privado)
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefijo para mensajes que el cliente envía al servidor
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefijo para mensajes dirigidos a usuarios específicos
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registra el endpoint "/ws" para que los clientes se conecten
        // con fallback a SockJS para navegadores que no soportan WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // ⚠️ En producción, especificar orígenes permitidos
                .withSockJS(); // Soporte para SockJS como fallback
        
        // También registro sin SockJS para clientes nativos (como React Native/Expo)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}
