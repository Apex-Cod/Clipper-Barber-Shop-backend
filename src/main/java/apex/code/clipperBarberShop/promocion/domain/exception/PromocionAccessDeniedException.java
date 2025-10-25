package apex.code.clipperBarberShop.promocion.domain.exception;

/**
 * Excepción cuando se intenta acceder a una promoción de otra empresa
 */
public class PromocionAccessDeniedException extends RuntimeException {
    public PromocionAccessDeniedException(String mensaje) {
        super(mensaje);
    }
    
    public PromocionAccessDeniedException() {
        super("No tienes permisos para acceder a esta promoción");
    }
}
