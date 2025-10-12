package apex.code.clipperBarberShop.auth.domain.port.out;

/**
 * Puerto de salida para operaciones de tokens JWT
 */
public interface TokenProviderPort {
    /**
     * Genera un token JWT para un usuario
     * @param userId ID del usuario
     * @param email Email del usuario
     * @param role Rol del usuario
     * @return Token JWT generado
     */
    String generateToken(String userId, String email, String role);
}