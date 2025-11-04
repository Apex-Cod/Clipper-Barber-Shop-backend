package apex.code.clipperBarberShop.servicio.domain.exception;

public class ServicioNotFoundException extends RuntimeException {
    public ServicioNotFoundException(String message) {
        super(message);
    }
    
    public ServicioNotFoundException(Long servicioId) {
        super("Servicio no encontrado con ID: " + servicioId);
    }
}
