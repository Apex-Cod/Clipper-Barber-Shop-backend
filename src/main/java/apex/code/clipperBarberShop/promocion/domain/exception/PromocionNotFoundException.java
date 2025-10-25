package apex.code.clipperBarberShop.promocion.domain.exception;

/**
 * Excepción cuando no se encuentra una promoción
 */
public class PromocionNotFoundException extends RuntimeException {
    public PromocionNotFoundException(String mensaje) {
        super(mensaje);
    }
    
    public PromocionNotFoundException(Long id) {
        super("Promoción no encontrada con ID: " + id);
    }
}
