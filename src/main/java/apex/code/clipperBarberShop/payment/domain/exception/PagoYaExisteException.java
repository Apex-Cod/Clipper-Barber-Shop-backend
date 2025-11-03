package apex.code.clipperBarberShop.payment.domain.exception;

/**
 * Excepción lanzada cuando ya existe un pago para una reserva
 */
public class PagoYaExisteException extends RuntimeException {
    public PagoYaExisteException(Long reservaId) {
        super("Ya existe un pago completado para la reserva ID: " + reservaId);
    }
}
