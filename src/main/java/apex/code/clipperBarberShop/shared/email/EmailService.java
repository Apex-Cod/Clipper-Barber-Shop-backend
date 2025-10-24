package apex.code.clipperBarberShop.shared.email;

/**
 * Servicio para envío de correos electrónicos
 */
public interface EmailService {
    
    /**
     * Envía un email de verificación al usuario
     * @param toEmail Email del destinatario
     * @param userName Nombre del usuario
     * @param verificationToken Token de verificación
     */
    void sendVerificationEmail(String toEmail, String userName, String verificationToken);
    
    /**
     * Envía un email de bienvenida después de verificar la cuenta
     * @param toEmail Email del destinatario
     * @param userName Nombre del usuario
     */
    void sendWelcomeEmail(String toEmail, String userName);
    
    /**
     * Envía un email con contraseña temporal para recuperación de cuenta
     * @param toEmail Email del destinatario
     * @param userName Nombre del usuario
     * @param temporaryPassword Contraseña temporal generada
     */
    void sendTemporaryPasswordEmail(String toEmail, String userName, String temporaryPassword);
}
