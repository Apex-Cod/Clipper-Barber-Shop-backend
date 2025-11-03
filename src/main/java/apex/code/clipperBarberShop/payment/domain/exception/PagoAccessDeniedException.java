package apex.code.clipperBarberShop.payment.domain.exception;

/**
 * Excepción lanzada cuando se intenta acceder a un pago sin autorización
 */
public class PagoAccessDeniedException extends RuntimeException {
    public PagoAccessDeniedException(String message) {
        super(message);
    }
}
