package apex.code.clipperBarberShop.shared.websocket.service;

import apex.code.clipperBarberShop.shared.websocket.dto.NotificationMessage;
import apex.code.clipperBarberShop.shared.websocket.dto.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Servicio para enviar notificaciones en tiempo real a través de WebSocket
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envía una notificación a todos los suscriptores
     */
    public void sendGlobalNotification(NotificationMessage notification) {
        try {
            log.info("Enviando notificación global: {}", notification.getType());
            messagingTemplate.convertAndSend("/topic/reservas", notification);
        } catch (Exception e) {
            log.error("Error al enviar notificación global", e);
        }
    }

    /**
     * Envía una notificación a un usuario específico
     */
    public void sendNotificationToUser(String userId, NotificationMessage notification) {
        try {
            log.info("Enviando notificación al usuario {}: {}", userId, notification.getType());
            notification.setUserId(userId);
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/notifications",
                    notification
            );
        } catch (Exception e) {
            log.error("Error al enviar notificación al usuario {}", userId, e);
        }
    }

    /**
     * Envía una notificación a todos los usuarios de una empresa
     */
    public void sendNotificationToEmpresa(Long empresaId, NotificationMessage notification) {
        try {
            log.info("Enviando notificación a empresa {}: {}", empresaId, notification.getType());
            notification.setEmpresaId(empresaId);
            messagingTemplate.convertAndSend(
                    "/topic/empresa/" + empresaId,
                    notification
            );
        } catch (Exception e) {
            log.error("Error al enviar notificación a empresa {}", empresaId, e);
        }
    }

    /**
     * Convierte de forma segura un Object a Long
     * Maneja casos donde el valor puede ser Integer, Long u otros tipos numéricos
     */
    private Long safeLongConversion(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("No se pudo convertir el valor {} a Long", value, e);
            return null;
        }
    }

    /**
     * Envía una notificación de nueva reserva creada
     * @param empresaId ID de la empresa
     * @param clientId ID del cliente que creó la reserva
     * @param employeeId ID del empleado asignado a la reserva
     * @param reservaData Datos de la reserva
     */
    public void sendReservaCreada(Long empresaId, String clientId, String employeeId, Map<String, Object> reservaData) {
        Long reservaId = safeLongConversion(reservaData.get("id"));
        
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationType.RESERVA_CREADA)
                .title("Nueva Reserva")
                .message("Se ha creado una nueva reserva")
                .resourceType("reserva")
                .resourceId(reservaId)
                .empresaId(empresaId)
                .priority("HIGH")
                .requiresAction(true)
                .data(reservaData)
                .actionUrl("/reservas/" + reservaId)
                .build();

        // Notificar al cliente que creó la reserva
        sendNotificationToUser(clientId, notification);

        // Notificar al empleado asignado
        NotificationMessage employeeNotification = NotificationMessage.builder()
                .type(NotificationType.RESERVA_CREADA)
                .title("Nueva Reserva Asignada")
                .message("Se te ha asignado una nueva reserva")
                .resourceType("reserva")
                .resourceId(reservaId)
                .empresaId(empresaId)
                .priority("HIGH")
                .requiresAction(true)
                .data(reservaData)
                .actionUrl("/reservas/" + reservaId)
                .build();
        sendNotificationToUser(employeeId, employeeNotification);

        // Notificar a toda la empresa
        sendNotificationToEmpresa(empresaId, notification);
    }

    /**
     * Envía una notificación de reserva confirmada
     */
    public void sendReservaConfirmada(Long empresaId, String clientId, Map<String, Object> reservaData) {
        Long reservaId = safeLongConversion(reservaData.get("id"));
        
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationType.RESERVA_CONFIRMADA)
                .title("Reserva Confirmada")
                .message("Tu reserva ha sido confirmada")
                .resourceType("reserva")
                .resourceId(reservaId)
                .empresaId(empresaId)
                .priority("HIGH")
                .data(reservaData)
                .actionUrl("/reservas/" + reservaId)
                .build();

        sendNotificationToUser(clientId, notification);
        sendNotificationToEmpresa(empresaId, notification);
    }

    /**
     * Envía una notificación de reserva cancelada
     * @param empresaId ID de la empresa
     * @param clientId ID del cliente
     * @param employeeId ID del empleado asignado
     * @param reservaData Datos de la reserva
     */
    public void sendReservaCancelada(Long empresaId, String clientId, String employeeId, Map<String, Object> reservaData) {
        Long reservaId = safeLongConversion(reservaData.get("id"));
        
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationType.RESERVA_CANCELADA)
                .title("Reserva Cancelada")
                .message("Una reserva ha sido cancelada")
                .resourceType("reserva")
                .resourceId(reservaId)
                .empresaId(empresaId)
                .priority("MEDIUM")
                .data(reservaData)
                .actionUrl("/reservas/" + reservaId)
                .build();

        sendNotificationToUser(clientId, notification);
        if (employeeId != null) {
            sendNotificationToUser(employeeId, notification);
        }
        sendNotificationToEmpresa(empresaId, notification);
    }

    /**
     * Envía una notificación de reserva reprogramada
     * @param empresaId ID de la empresa
     * @param clientId ID del cliente
     * @param employeeId ID del empleado asignado
     * @param reservaData Datos de la reserva
     */
    public void sendReservaReprogramada(Long empresaId, String clientId, String employeeId, Map<String, Object> reservaData) {
        Long reservaId = safeLongConversion(reservaData.get("id"));
        
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationType.RESERVA_REPROGRAMADA)
                .title("Reserva Reprogramada")
                .message("Una reserva ha sido reprogramada")
                .resourceType("reserva")
                .resourceId(reservaId)
                .empresaId(empresaId)
                .priority("HIGH")
                .requiresAction(true)
                .data(reservaData)
                .actionUrl("/reservas/" + reservaId)
                .build();

        sendNotificationToUser(clientId, notification);
        if (employeeId != null) {
            sendNotificationToUser(employeeId, notification);
        }
        sendNotificationToEmpresa(empresaId, notification);
    }

    /**
     * Envía una notificación de reserva completada
     */
    public void sendReservaCompletada(Long empresaId, String clientId, Map<String, Object> reservaData) {
        Long reservaId = safeLongConversion(reservaData.get("id"));
        
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationType.RESERVA_COMPLETADA)
                .title("Reserva Completada")
                .message("Tu reserva ha sido completada")
                .resourceType("reserva")
                .resourceId(reservaId)
                .empresaId(empresaId)
                .priority("MEDIUM")
                .data(reservaData)
                .actionUrl("/reservas/" + reservaId)
                .build();

        sendNotificationToUser(clientId, notification);
        sendNotificationToEmpresa(empresaId, notification);
    }

    /**
     * Envía una notificación de reserva actualizada
     * @param empresaId ID de la empresa
     * @param clientId ID del cliente
     * @param employeeId ID del empleado asignado
     * @param reservaData Datos de la reserva
     */
    public void sendReservaActualizada(Long empresaId, String clientId, String employeeId, Map<String, Object> reservaData) {
        Long reservaId = safeLongConversion(reservaData.get("id"));
        
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationType.RESERVA_ACTUALIZADA)
                .title("Reserva Actualizada")
                .message("Una reserva ha sido actualizada")
                .resourceType("reserva")
                .resourceId(reservaId)
                .empresaId(empresaId)
                .priority("MEDIUM")
                .data(reservaData)
                .actionUrl("/reservas/" + reservaId)
                .build();

        sendNotificationToUser(clientId, notification);
        if (employeeId != null) {
            sendNotificationToUser(employeeId, notification);
        }
        sendNotificationToEmpresa(empresaId, notification);
    }
}
