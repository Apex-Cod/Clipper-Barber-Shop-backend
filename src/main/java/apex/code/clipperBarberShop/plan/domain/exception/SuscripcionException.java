package apex.code.clipperBarberShop.plan.domain.exception;

/**
 * Excepción lanzada cuando ocurre un error relacionado con suscripciones
 */
public class SuscripcionException extends RuntimeException {
    
    public SuscripcionException(String mensaje) {
        super(mensaje);
    }
    
    public SuscripcionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
