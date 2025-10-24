package apex.code.clipperBarberShop.auth.domain;

/**
 * Puerto de entrada para el caso de uso de recuperación de contraseña
 */
public interface ForgotPasswordUseCase {
    
    /**
     * Genera una contraseña temporal y la envía por email al usuario
     * 
     * @param email El email del usuario que olvidó su contraseña
     * @return Mensaje de confirmación
     * @throws RuntimeException Si el usuario no existe o no está verificado
     */
    String resetPassword(String email);
}
