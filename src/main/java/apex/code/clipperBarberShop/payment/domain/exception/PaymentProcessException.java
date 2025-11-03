package apex.code.clipperBarberShop.payment.domain.exception;

/**
 * Excepción lanzada cuando falla un proceso de pago
 */
public class PaymentProcessException extends RuntimeException {
    public PaymentProcessException(String message) {
        super(message);
    }
    
    public PaymentProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
