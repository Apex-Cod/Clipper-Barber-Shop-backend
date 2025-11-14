package apex.code.clipperBarberShop.resenia.domain.exception;

public class ReseniaNotFoundException extends RuntimeException {
    public ReseniaNotFoundException(Long id) {
        super("Reseña no encontrada con ID: " + id);
    }
}
