package apex.code.clipperBarberShop.resenia.domain.exception;

public class ReseniaAlreadyExistsException extends RuntimeException {
    public ReseniaAlreadyExistsException(Long reservaId) {
        super("Ya existe una reseña para la reserva con ID: " + reservaId);
    }
}
