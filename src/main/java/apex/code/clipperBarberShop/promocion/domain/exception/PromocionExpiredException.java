package apex.code.clipperBarberShop.promocion.domain.exception;

/**
 * Excepción cuando una promoción ha expirado
 */
public class PromocionExpiredException extends RuntimeException {
    public PromocionExpiredException(String mensaje) {
        super(mensaje);
    }
    
    public PromocionExpiredException(Long id) {
        super("La promoción con ID " + id + " ha expirado o alcanzó su límite de usos");
    }
}
