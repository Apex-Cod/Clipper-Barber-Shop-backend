package apex.code.clipperBarberShop.payment.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra un pago
 */
public class PagoNotFoundException extends RuntimeException {
    public PagoNotFoundException(Long id) {
        super("Pago no encontrado con ID: " + id);
    }
    
    public PagoNotFoundException(String paypalPaymentId) {
        super("Pago no encontrado con PayPal Payment ID: " + paypalPaymentId);
    }
}
