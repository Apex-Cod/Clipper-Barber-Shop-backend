package apex.code.clipperBarberShop.shared.websocket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para mensajes de notificación enviados por WebSocket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    
    /**
     * Tipo de notificación
     */
    private NotificationType type;
    
    /**
     * Título de la notificación
     */
    private String title;
    
    /**
     * Mensaje descriptivo
     */
    private String message;
    
    /**
     * Timestamp de cuando se creó la notificación
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * ID del recurso relacionado (ej: ID de la reserva)
     */
    private Long resourceId;
    
    /**
     * Tipo de recurso (ej: "reserva", "servicio", etc.)
     */
    private String resourceType;
    
    /**
     * Datos adicionales específicos de la notificación
     */
    private Map<String, Object> data;
    
    /**
     * Nivel de prioridad (LOW, MEDIUM, HIGH, URGENT)
     */
    @Builder.Default
    private String priority = "MEDIUM";
    
    /**
     * ID de la empresa relacionada (para filtrar notificaciones)
     */
    private Long empresaId;
    
    /**
     * ID del usuario destinatario (si es una notificación privada)
     */
    private String userId;
    
    /**
     * Indica si requiere acción del usuario
     */
    @Builder.Default
    private Boolean requiresAction = false;
    
    /**
     * URL o ruta para navegar cuando se hace clic en la notificación
     */
    private String actionUrl;
}
