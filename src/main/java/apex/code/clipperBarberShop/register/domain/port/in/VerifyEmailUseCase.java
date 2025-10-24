package apex.code.clipperBarberShop.register.domain.port.in;

/**
 * Caso de uso para verificación de email
 */
public interface VerifyEmailUseCase {
    
    /**
     * Verifica el email de un usuario mediante el token
     * @param token Token de verificación
     * @return true si la verificación fue exitosa
     * @throws IllegalArgumentException si el token es inválido o expiró
     */
    boolean verifyEmail(String token);
    
    /**
     * Reenvía el email de verificación
     * @param email Email del usuario
     * @throws IllegalArgumentException si el email no existe o ya está verificado
     */
    void resendVerificationEmail(String email);
}
