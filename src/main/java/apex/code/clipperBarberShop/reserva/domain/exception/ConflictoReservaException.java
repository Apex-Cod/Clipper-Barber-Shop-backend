package apex.code.clipperBarberShop.reserva.domain.exception;

import java.time.LocalDateTime;

public class ConflictoReservaException extends RuntimeException {
    public ConflictoReservaException(String message) {
        super(message);
    }
    
    public ConflictoReservaException(LocalDateTime fecha, String employeeId) {
        super("Ya existe una reserva para el empleado " + employeeId + " en la fecha " + fecha);
    }
}
