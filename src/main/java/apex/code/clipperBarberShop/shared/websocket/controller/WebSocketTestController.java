package apex.code.clipperBarberShop.shared.websocket.controller;

import apex.code.clipperBarberShop.shared.websocket.dto.NotificationMessage;
import apex.code.clipperBarberShop.shared.websocket.dto.NotificationType;
import apex.code.clipperBarberShop.shared.websocket.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de prueba para WebSocket
 * Útil para probar las notificaciones manualmente
 */
@RestController
@RequestMapping("/api/test/websocket")
@RequiredArgsConstructor
public class WebSocketTestController {

    private final WebSocketNotificationService notificationService;

    /**
     * Envía una notificación de prueba global
     * GET http://localhost:8080/api/test/websocket/send-global
     */
    @GetMapping("/send-global")
    public ResponseEntity<String> sendGlobalNotification() {
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationType.SISTEMA_INFO)
                .title("Prueba de Notificación Global")
                .message("Esta es una notificación de prueba enviada a todos")
                .priority("MEDIUM")
                .build();

        notificationService.sendGlobalNotification(notification);
        return ResponseEntity.ok("Notificación global enviada");
    }

    /**
     * Envía una notificación a un usuario específico
     * GET http://localhost:8080/api/test/websocket/send-user?userId=user-123
     */
    @GetMapping("/send-user")
    public ResponseEntity<String> sendUserNotification(@RequestParam String userId) {
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationType.SISTEMA_INFO)
                .title("Notificación Personal")
                .message("Esta es una notificación solo para ti, " + userId)
                .userId(userId)
                .priority("HIGH")
                .build();

        notificationService.sendNotificationToUser(userId, notification);
        return ResponseEntity.ok("Notificación enviada al usuario: " + userId);
    }

    /**
     * Envía una notificación a una empresa específica
     * GET http://localhost:8080/api/test/websocket/send-empresa?empresaId=1
     */
    @GetMapping("/send-empresa")
    public ResponseEntity<String> sendEmpresaNotification(@RequestParam Long empresaId) {
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationType.SISTEMA_INFO)
                .title("Notificación de Empresa")
                .message("Notificación para la empresa ID: " + empresaId)
                .empresaId(empresaId)
                .priority("MEDIUM")
                .build();

        notificationService.sendNotificationToEmpresa(empresaId, notification);
        return ResponseEntity.ok("Notificación enviada a la empresa: " + empresaId);
    }

    /**
     * Simula la creación de una reserva
     * GET http://localhost:8080/api/test/websocket/simulate-reserva
     */
    @GetMapping("/simulate-reserva")
    public ResponseEntity<String> simulateReserva(
            @RequestParam(defaultValue = "1") Long empresaId,
            @RequestParam(defaultValue = "client-123") String clientId,
            @RequestParam(defaultValue = "owner-456") String ownerId) {

        Map<String, Object> reservaData = new HashMap<>();
        reservaData.put("id", 999L);
        reservaData.put("serviceName", "Corte de Cabello + Barba");
        reservaData.put("clientName", "Juan Pérez");
        reservaData.put("employeeId", "emp-789");
        reservaData.put("employeeName", "Carlos Barbero");
        reservaData.put("reservationDate", "2025-11-05T10:00:00");
        reservaData.put("finalPrice", 35.00);
        reservaData.put("status", "PENDING");

        notificationService.sendReservaCreada(empresaId, clientId, ownerId, reservaData);
        
        return ResponseEntity.ok("Simulación de reserva enviada - empresaId: " + empresaId 
                + ", clientId: " + clientId + ", ownerId: " + ownerId);
    }

    /**
     * Endpoint de health check para WebSocket
     * GET http://localhost:8080/api/test/websocket/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "WebSocket Notification Service");
        health.put("endpoints", Map.of(
                "ws", "/ws",
                "global", "/topic/reservas",
                "empresa", "/topic/empresa/{empresaId}",
                "user", "/user/queue/notifications"
        ));
        return ResponseEntity.ok(health);
    }
}
