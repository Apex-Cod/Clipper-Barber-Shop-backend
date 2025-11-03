package apex.code.clipperBarberShop.resenia.domain.exception;

public class ReservaNotCompletedException extends RuntimeException {
    public ReservaNotCompletedException(Long reservaId) {
        super("La reserva con ID " + reservaId + " debe estar completada para crear una reseña");
    }
}
