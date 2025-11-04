package apex.code.clipperBarberShop.Entities.enums;

/**
 * Enum para definir el estado de una reserva
 */
public enum ReservaStatus {
    PENDING("Pendiente"),
    CONFIRMED("Confirmada"),
    COMPLETED("Completada"),
    CANCELLED("Cancelada"),
    RESCHEDULED("Reprogramada");

    private final String descripcion;

    ReservaStatus(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
