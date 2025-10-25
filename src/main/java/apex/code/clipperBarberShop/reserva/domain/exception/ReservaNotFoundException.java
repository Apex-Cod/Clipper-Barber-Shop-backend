package apex.code.clipperBarberShop.reserva.domain.exception;

public class ReservaNotFoundException extends RuntimeException {
    public ReservaNotFoundException(String message) {
        super(message);
    }
    
    public ReservaNotFoundException(Long reservaId) {
        super("Reserva no encontrada con ID: " + reservaId);
    }
}
