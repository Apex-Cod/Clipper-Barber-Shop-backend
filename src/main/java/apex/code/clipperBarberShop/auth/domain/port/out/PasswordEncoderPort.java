package apex.code.clipperBarberShop.auth.domain.port.out;

/**
 * Puerto de salida para operaciones de codificación de contraseñas
 */
public interface PasswordEncoderPort {
    /**
     * Verifica si una contraseña en texto plano coincide con una contraseña codificada
     * @param rawPassword Contraseña en texto plano
     * @param encodedPassword Contraseña codificada
     * @return true si coinciden, false en caso contrario
     */
    boolean matches(String rawPassword, String encodedPassword);
}