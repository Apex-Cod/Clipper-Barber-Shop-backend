package apex.code.clipperBarberShop.shared.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de email
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;
    
    @Value("${app.verification.use-code:true}")
    private boolean useVerificationCode;
    
    @Override
    @Async
    public void sendVerificationEmail(String toEmail, String userName, String verificationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verifica tu cuenta - Clipper Barber Shop");
            
            String htmlContent;
            if (useVerificationCode) {
                // App móvil: mostrar código de 6 dígitos
                htmlContent = buildVerificationCodeEmailTemplate(userName, verificationToken);
            } else {
                // Web: mostrar enlace clickeable
                String verificationLink = baseUrl + "/api/registro/verify?token=" + verificationToken;
                htmlContent = buildVerificationLinkEmailTemplate(userName, verificationLink);
            }
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email de verificación enviado a: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Error al enviar email de verificación a: {}", toEmail, e);
            throw new RuntimeException("Error al enviar email de verificación", e);
        }
    }
    
    @Override
    @Async
    public void sendWelcomeEmail(String toEmail, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("¡Bienvenido a Clipper Barber Shop!");
            
            String htmlContent = buildWelcomeEmailTemplate(userName);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email de bienvenida enviado a: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Error al enviar email de bienvenida a: {}", toEmail, e);
            // No lanzamos excepción aquí porque no es crítico
        }
    }
    
    @Override
    @Async
    public void sendTemporaryPasswordEmail(String toEmail, String userName, String temporaryPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Recuperación de Contraseña - Clipper Barber Shop");
            
            String htmlContent = buildTemporaryPasswordEmailTemplate(userName, temporaryPassword);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email de recuperación de contraseña enviado a: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Error al enviar email de recuperación a: {}", toEmail, e);
            throw new RuntimeException("Error al enviar email de recuperación", e);
        }
    }
    
    /**
     * Template para apps móviles - muestra código de 6 dígitos
     */
    private String buildVerificationCodeEmailTemplate(String userName, String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f4f4f4; padding: 30px; }
                    .code {
                        font-size: 32px;
                        font-weight: bold;
                        letter-spacing: 8px;
                        color: #2c3e50;
                        background-color: #ecf0f1;
                        padding: 20px;
                        text-align: center;
                        border-radius: 8px;
                        margin: 30px 0;
                    }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✂️ Clipper Barber Shop</h1>
                    </div>
                    <div class="content">
                        <h2>¡Hola %s!</h2>
                        <p>Gracias por registrarte en Clipper Barber Shop.</p>
                        <p>Para completar tu registro y activar tu cuenta, ingresa el siguiente código en la aplicación:</p>
                        <div class="code">%s</div>
                        <p><strong>Este código expirará en 15 minutos.</strong></p>
                        <p>Si no te registraste en nuestra plataforma, puedes ignorar este correo.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Clipper Barber Shop. Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, code);
    }
    
    /**
     * Template para web - muestra enlace clickeable
     */
    private String buildVerificationLinkEmailTemplate(String userName, String verificationLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f4f4f4; padding: 30px; }
                    .button { 
                        display: inline-block; 
                        padding: 12px 30px; 
                        background-color: #3498db; 
                        color: white; 
                        text-decoration: none; 
                        border-radius: 5px;
                        font-weight: bold;
                    }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✂️ Clipper Barber Shop</h1>
                    </div>
                    <div class="content">
                        <h2>¡Hola %s!</h2>
                        <p>Gracias por registrarte en Clipper Barber Shop.</p>
                        <p>Para completar tu registro y activar tu cuenta, por favor verifica tu correo electrónico haciendo clic en el siguiente botón:</p>
                        <p style="text-align: center; margin: 30px 0;">
                            <a href="%s" class="button">Verificar Email</a>
                        </p>
                        <p>Si el botón no funciona, copia y pega este enlace en tu navegador:</p>
                        <p style="word-break: break-all; color: #3498db;">%s</p>
                        <p><strong>Este enlace expirará en 15 minutos.</strong></p>
                        <p>Si no te registraste en nuestra plataforma, puedes ignorar este correo.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Clipper Barber Shop. Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, verificationLink, verificationLink);
    }
    
    private String buildWelcomeEmailTemplate(String userName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #27ae60; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f4f4f4; padding: 30px; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✂️ ¡Bienvenido!</h1>
                    </div>
                    <div class="content">
                        <h2>¡Hola %s!</h2>
                        <p>Tu email ha sido verificado exitosamente. ¡Bienvenido a Clipper Barber Shop! 🎉</p>
                        <p>Ya puedes iniciar sesión y disfrutar de todos nuestros servicios.</p>
                        <ul>
                            <li>Reserva citas en línea</li>
                            <li>Gestiona tu perfil</li>
                            <li>Accede a promociones exclusivas</li>
                        </ul>
                        <p>Si tienes alguna pregunta, no dudes en contactarnos.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Clipper Barber Shop. Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName);
    }
    
    private String buildTemporaryPasswordEmailTemplate(String userName, String temporaryPassword) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #e74c3c; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f4f4f4; padding: 30px; }
                    .password-box { 
                        background-color: white; 
                        border: 2px solid #e74c3c; 
                        padding: 20px; 
                        text-align: center; 
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .password { 
                        font-size: 32px; 
                        font-weight: bold; 
                        color: #e74c3c; 
                        letter-spacing: 3px;
                        font-family: 'Courier New', monospace;
                    }
                    .warning { 
                        background-color: #fff3cd; 
                        border-left: 4px solid #ffc107; 
                        padding: 15px; 
                        margin: 20px 0;
                    }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔒 Recuperación de Contraseña</h1>
                    </div>
                    <div class="content">
                        <h2>¡Hola %s!</h2>
                        <p>Has solicitado recuperar tu contraseña. Hemos generado una contraseña temporal para ti:</p>
                        
                        <div class="password-box">
                            <p style="margin: 0; font-size: 14px; color: #666;">Tu contraseña temporal es:</p>
                            <p class="password">%s</p>
                        </div>
                        
                        <div class="warning">
                            <p style="margin: 0;"><strong>⚠️ Importante:</strong></p>
                            <ul style="margin: 10px 0 0 0;">
                                <li>Utiliza esta contraseña para iniciar sesión</li>
                                <li>Por seguridad, te recomendamos cambiarla inmediatamente</li>
                                <li>Esta contraseña es temporal y única</li>
                            </ul>
                        </div>
                        
                        <p>Si no solicitaste esta recuperación de contraseña, por favor contacta con nosotros inmediatamente.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Clipper Barber Shop. Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, temporaryPassword);
    }
}
