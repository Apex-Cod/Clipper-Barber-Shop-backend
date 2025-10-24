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
     * Verifica el email de un usuario mediante código de 6 dígitos (para apps móviles)
     * @param email Email del usuario
     * @param code Código de verificación de 6 dígitos
     * @return true si la verificación fue exitosa
     * @throws IllegalArgumentException si el código es inválido, expiró o no corresponde al email
     */
    boolean verifyEmailWithCode(String email, String code);
    
    /**
     * Reenvía el email de verificación
     * @param email Email del usuario
     * @throws IllegalArgumentException si el email no existe o ya está verificado
     */
    void resendVerificationEmail(String email);
}
