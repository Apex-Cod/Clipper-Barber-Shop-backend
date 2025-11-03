package apex.code.clipperBarberShop.shared.websocket.dto;

/**
 * Tipos de notificaciones que se pueden enviar por WebSocket
 */
public enum NotificationType {
    // Notificaciones de reservas
    RESERVA_CREADA("Nueva reserva creada"),
    RESERVA_CONFIRMADA("Reserva confirmada"),
    RESERVA_CANCELADA("Reserva cancelada"),
    RESERVA_REPROGRAMADA("Reserva reprogramada"),
    RESERVA_COMPLETADA("Reserva completada"),
    RESERVA_ACTUALIZADA("Reserva actualizada"),
    
    // Notificaciones de sistema
    SISTEMA_INFO("Información del sistema"),
    SISTEMA_WARNING("Advertencia del sistema"),
    SISTEMA_ERROR("Error del sistema");
    
    private final String descripcion;
    
    NotificationType(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}
