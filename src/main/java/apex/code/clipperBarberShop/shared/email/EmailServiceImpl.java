package apex.code.clipperBarberShop.shared.email;

import apex.code.clipperBarberShop.Entities.Reserva;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

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
    
    @Override
    @Async
    public void sendReservaConfirmacionEmail(Reserva reserva, String clientName, String clientEmail,
                                             String employeeName, String serviceName,
                                             String empresaName, String empresaDireccion) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(clientEmail);
            helper.setSubject("✅ Confirmación de Reserva - " + empresaName);
            
            String htmlContent = buildReservaConfirmacionTemplate(reserva, clientName, employeeName, 
                                                                  serviceName, empresaName, empresaDireccion);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email de confirmación de reserva enviado a: {}", clientEmail);
            
        } catch (MessagingException e) {
            log.error("Error al enviar email de confirmación de reserva a: {}", clientEmail, e);
            // No lanzamos excepción para no bloquear el flujo de creación de reserva
        }
    }
    
    @Override
    @Async
    public void sendReservaRecordatorioEmail(Reserva reserva, String clientName, String clientEmail,
                                             String employeeName, String serviceName,
                                             String empresaName, String empresaDireccion) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(clientEmail);
            helper.setSubject("⏰ Recordatorio: Tu cita es en 15 minutos - " + empresaName);
            
            String htmlContent = buildReservaRecordatorioTemplate(reserva, clientName, employeeName,
                                                                  serviceName, empresaName, empresaDireccion);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Email recordatorio de reserva enviado a: {}", clientEmail);
            
        } catch (MessagingException e) {
            log.error("Error al enviar email recordatorio de reserva a: {}", clientEmail, e);
        }
    }
    
    private String buildReservaConfirmacionTemplate(Reserva reserva, String clientName, String employeeName,
                                                    String serviceName, String empresaName, String empresaDireccion) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        String fecha = reserva.getReservationDate().format(dateFormatter);
        String hora = reserva.getReservationDate().format(timeFormatter);
        String diaSemana = reserva.getReservationDate().getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        
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
                    .reserva-info { 
                        background-color: white; 
                        border-left: 4px solid #3498db; 
                        padding: 20px; 
                        margin: 20px 0;
                    }
                    .info-row { 
                        display: flex; 
                        margin: 10px 0; 
                        border-bottom: 1px solid #ecf0f1;
                        padding-bottom: 8px;
                    }
                    .info-label { 
                        font-weight: bold; 
                        min-width: 130px; 
                        color: #2c3e50;
                    }
                    .info-value { color: #555; }
                    .alert-box { 
                        background-color: #fff3cd; 
                        border-left: 4px solid #ffc107; 
                        padding: 15px; 
                        margin: 20px 0;
                    }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                    .button { 
                        display: inline-block; 
                        padding: 12px 30px; 
                        background-color: #27ae60; 
                        color: white; 
                        text-decoration: none; 
                        border-radius: 5px;
                        font-weight: bold;
                        margin: 10px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✂️ %s</h1>
                        <p style="margin: 0; font-size: 18px;">Confirmación de Reserva</p>
                    </div>
                    <div class="content">
                        <h2>¡Hola %s! 👋</h2>
                        <p>Tu reserva ha sido <strong>confirmada exitosamente</strong>. A continuación encontrarás los detalles:</p>
                        
                        <div class="reserva-info">
                            <h3 style="margin-top: 0; color: #2c3e50;">📋 Detalles de la Reserva</h3>
                            <div class="info-row">
                                <span class="info-label">📅 Fecha:</span>
                                <span class="info-value">%s, %s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">🕐 Hora:</span>
                                <span class="info-value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">✂️ Servicio:</span>
                                <span class="info-value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">⏱️ Duración:</span>
                                <span class="info-value">%d minutos</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">👨‍💼 Profesional:</span>
                                <span class="info-value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">💰 Precio:</span>
                                <span class="info-value">$%.2f</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">📍 Dirección:</span>
                                <span class="info-value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">🆔 Reserva #:</span>
                                <span class="info-value">%d</span>
                            </div>
                        </div>
                        
                        <div class="alert-box">
                            <p style="margin: 0; font-size: 16px;"><strong>⚠️ IMPORTANTE:</strong></p>
                            <p style="margin: 10px 0 0 0;">Por favor, <strong>llega al menos 15 minutos antes</strong> de tu cita programada para asegurar que podamos atenderte en el horario establecido.</p>
                        </div>
                        
                        <p>Recibirás un recordatorio 15 minutos antes de tu cita.</p>
                        <p>Si necesitas cancelar o reprogramar tu cita, por favor contáctanos con anticipación.</p>
                        
                        <p><strong>¡Te esperamos!</strong></p>
                    </div>
                    <div class="footer">
                        <p>© 2025 %s. Todos los derechos reservados.</p>
                        <p>Este es un correo automático, por favor no responder.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(empresaName, clientName, diaSemana, fecha, hora, serviceName,
                         reserva.getDuracionMinutos(), employeeName, reserva.getFinalPrice(),
                         empresaDireccion, reserva.getId(), empresaName);
    }
    
    private String buildReservaRecordatorioTemplate(Reserva reserva, String clientName, String employeeName,
                                                    String serviceName, String empresaName, String empresaDireccion) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String hora = reserva.getReservationDate().format(timeFormatter);
        
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
                    .alert-box { 
                        background-color: #ffebee; 
                        border: 3px solid #e74c3c; 
                        padding: 20px; 
                        margin: 20px 0;
                        text-align: center;
                        border-radius: 8px;
                    }
                    .time { 
                        font-size: 48px; 
                        font-weight: bold; 
                        color: #e74c3c; 
                        margin: 10px 0;
                    }
                    .info-box { 
                        background-color: white; 
                        border-left: 4px solid #e74c3c; 
                        padding: 20px; 
                        margin: 20px 0;
                    }
                    .info-row { margin: 8px 0; }
                    .info-label { font-weight: bold; color: #2c3e50; }
                    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⏰ ¡RECORDATORIO!</h1>
                        <p style="margin: 0; font-size: 18px;">Tu cita es en 15 minutos</p>
                    </div>
                    <div class="content">
                        <h2>¡Hola %s!</h2>
                        
                        <div class="alert-box">
                            <p style="margin: 0; font-size: 20px;">Tu cita es a las</p>
                            <div class="time">%s</div>
                            <p style="margin: 0; font-size: 18px; font-weight: bold;">¡EN 15 MINUTOS! 🏃‍♂️</p>
                        </div>
                        
                        <div class="info-box">
                            <h3 style="margin-top: 0; color: #e74c3c;">📋 Detalles de tu Cita</h3>
                            <div class="info-row">
                                <span class="info-label">✂️ Servicio:</span> %s
                            </div>
                            <div class="info-row">
                                <span class="info-label">👨‍💼 Profesional:</span> %s
                            </div>
                            <div class="info-row">
                                <span class="info-label">📍 Dirección:</span> %s
                            </div>
                            <div class="info-row">
                                <span class="info-label">🆔 Reserva #:</span> %d
                            </div>
                        </div>
                        
                        <p style="text-align: center; font-size: 18px; color: #e74c3c; font-weight: bold;">
                            ¡No olvides que debes estar en las instalaciones con anticipación!
                        </p>
                        
                        <p>Nos vemos pronto. 😊</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 %s. Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(clientName, hora, serviceName, employeeName, empresaDireccion, 
                         reserva.getId(), empresaName);
    }
    
    @Override
    @Async
    public void sendReservaCompletadaEmail(Reserva reserva, String clientName, String clientEmail,
                                           String employeeName, String serviceName, String empresaName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(clientEmail);
            helper.setSubject("✅ Reserva Completada - " + empresaName);
            helper.setText(buildReservaCompletadaEmailTemplate(reserva, clientName, employeeName,
                    serviceName, empresaName), true);

            mailSender.send(message);
            log.info("Email de reserva completada enviado a: {}", clientEmail);

        } catch (MessagingException e) {
            log.error("Error al enviar email de reserva completada a {}: {}", clientEmail, e.getMessage());
        }
    }
    
    @Override
    @Async
    public void sendReservaCanceladaEmail(Reserva reserva, String clientName, String clientEmail,
                                          String serviceName, String empresaName, String motivo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(clientEmail);
            helper.setSubject("❌ Reserva Cancelada - " + empresaName);
            helper.setText(buildReservaCanceladaEmailTemplate(reserva, clientName, serviceName,
                    empresaName, motivo), true);

            mailSender.send(message);
            log.info("Email de reserva cancelada enviado a: {}", clientEmail);

        } catch (MessagingException e) {
            log.error("Error al enviar email de reserva cancelada a {}: {}", clientEmail, e.getMessage());
        }
    }
    
    @Override
    @Async
    public void sendReservaReprogramadaEmail(Reserva reserva, String clientName, String clientEmail,
                                             String employeeName, String serviceName, 
                                             String empresaName, String fechaAnterior) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(clientEmail);
            helper.setSubject("🔄 Reserva Reprogramada - " + empresaName);
            helper.setText(buildReservaReprogramadaEmailTemplate(reserva, clientName, employeeName,
                    serviceName, empresaName, fechaAnterior), true);

            mailSender.send(message);
            log.info("Email de reserva reprogramada enviado a: {}", clientEmail);

        } catch (MessagingException e) {
            log.error("Error al enviar email de reserva reprogramada a {}: {}", clientEmail, e.getMessage());
        }
    }
    
    private String buildReservaCompletadaEmailTemplate(Reserva reserva, String clientName,
                                                       String employeeName, String serviceName,
                                                       String empresaName) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String fecha = reserva.getReservationDate().format(dateFormatter);
        String horaInicio = reserva.getReservationDate().format(timeFormatter);
        String horaFin = reserva.getReservationDate().plusMinutes(reserva.getDuracionMinutos()).format(timeFormatter);
        String hora = horaInicio + " - " + horaFin + " (aprox.)";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #4CAF50 0%%, #45a049 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .info-box { background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #4CAF50; border-radius: 5px; }
                    .info-row { margin: 10px 0; }
                    .info-label { font-weight: bold; color: #555; }
                    .footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }
                    .success-icon { font-size: 48px; margin-bottom: 10px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="success-icon">✅</div>
                        <h1 style="margin: 0;">Reserva Completada</h1>
                        <p style="margin: 10px 0 0 0; font-size: 16px;">Tu cita ha sido completada exitosamente</p>
                    </div>
                    <div class="content">
                        <p>Hola <strong>%s</strong>,</p>
                        <p>Tu reserva en <strong>%s</strong> ha sido completada exitosamente. Esperamos que hayas disfrutado de nuestro servicio.</p>
                        <div class="info-box">
                            <h3 style="margin-top: 0; color: #4CAF50;">Detalles de la Reserva</h3>
                            <div class="info-row"><span class="info-label">Servicio:</span> %s</div>
                            <div class="info-row"><span class="info-label">Empleado:</span> %s</div>
                            <div class="info-row"><span class="info-label">Fecha:</span> %s</div>
                            <div class="info-row"><span class="info-label">Hora:</span> %s</div>
                        </div>
                        <p>¡Gracias por confiar en nosotros! Esperamos verte pronto de nuevo.</p>
                        <p>Si tienes algún comentario sobre tu experiencia, nos encantaría escucharlo.</p>
                    </div>
                    <div class="footer">
                        <p>Este es un correo automático, por favor no respondas a este mensaje.</p>
                        <p>© 2025 %s. Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(clientName, empresaName, serviceName, employeeName, fecha, hora, empresaName);
    }
    
    private String buildReservaCanceladaEmailTemplate(Reserva reserva, String clientName,
                                                      String serviceName, String empresaName,
                                                      String motivo) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String fecha = reserva.getReservationDate().format(dateFormatter);
        String horaInicio = reserva.getReservationDate().format(timeFormatter);
        String horaFin = reserva.getReservationDate().plusMinutes(reserva.getDuracionMinutos()).format(timeFormatter);
        String hora = horaInicio + " - " + horaFin + " (aprox.)";

        String motivoSection = "";
        if (motivo != null && !motivo.isEmpty()) {
            motivoSection = """
                <div class="info-box" style="border-left-color: #f44336;">
                    <h4 style="margin-top: 0; color: #f44336;">Motivo de la Cancelación</h4>
                    <p>%s</p>
                </div>
                """.formatted(motivo);
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #f44336 0%%, #d32f2f 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .info-box { background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #f44336; border-radius: 5px; }
                    .info-row { margin: 10px 0; }
                    .info-label { font-weight: bold; color: #555; }
                    .footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }
                    .cancel-icon { font-size: 48px; margin-bottom: 10px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="cancel-icon">❌</div>
                        <h1 style="margin: 0;">Reserva Cancelada</h1>
                        <p style="margin: 10px 0 0 0; font-size: 16px;">Tu cita ha sido cancelada</p>
                    </div>
                    <div class="content">
                        <p>Hola <strong>%s</strong>,</p>
                        <p>Tu reserva en <strong>%s</strong> ha sido cancelada.</p>
                        <div class="info-box">
                            <h3 style="margin-top: 0; color: #f44336;">Detalles de la Reserva Cancelada</h3>
                            <div class="info-row"><span class="info-label">Servicio:</span> %s</div>
                            <div class="info-row"><span class="info-label">Fecha:</span> %s</div>
                            <div class="info-row"><span class="info-label">Hora:</span> %s</div>
                        </div>
                        %s
                        <p>Si deseas agendar una nueva cita, estaremos encantados de atenderte. Puedes contactarnos o realizar una nueva reserva cuando lo desees.</p>
                    </div>
                    <div class="footer">
                        <p>Este es un correo automático, por favor no respondas a este mensaje.</p>
                        <p>© 2025 %s. Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(clientName, empresaName, serviceName, fecha, hora, motivoSection, empresaName);
    }
    
    private String buildReservaReprogramadaEmailTemplate(Reserva reserva, String clientName,
                                                         String employeeName, String serviceName,
                                                         String empresaName, String fechaAnterior) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String nuevaFecha = reserva.getReservationDate().format(dateFormatter);
        String horaInicio = reserva.getReservationDate().format(timeFormatter);
        String horaFin = reserva.getReservationDate().plusMinutes(reserva.getDuracionMinutos()).format(timeFormatter);
        String nuevaHora = horaInicio + " - " + horaFin + " (aprox.)";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #FF9800 0%%, #F57C00 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .info-box { background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #FF9800; border-radius: 5px; }
                    .info-row { margin: 10px 0; }
                    .info-label { font-weight: bold; color: #555; }
                    .old-value { color: #999; text-decoration: line-through; }
                    .new-value { color: #FF9800; font-weight: bold; }
                    .footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }
                    .reschedule-icon { font-size: 48px; margin-bottom: 10px; }
                    .reminder-box { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="reschedule-icon">🔄</div>
                        <h1 style="margin: 0;">Reserva Reprogramada</h1>
                        <p style="margin: 10px 0 0 0; font-size: 16px;">Tu cita ha sido cambiada de fecha</p>
                    </div>
                    <div class="content">
                        <p>Hola <strong>%s</strong>,</p>
                        <p>Tu reserva en <strong>%s</strong> ha sido reprogramada exitosamente.</p>
                        <div class="info-box">
                            <h3 style="margin-top: 0; color: #FF9800;">Fecha Anterior</h3>
                            <div class="info-row"><span class="info-label">Fecha anterior:</span> <span class="old-value">%s</span></div>
                        </div>
                        <div class="info-box">
                            <h3 style="margin-top: 0; color: #FF9800;">Nueva Fecha de la Reserva</h3>
                            <div class="info-row"><span class="info-label">Servicio:</span> %s</div>
                            <div class="info-row"><span class="info-label">Empleado:</span> %s</div>
                            <div class="info-row"><span class="info-label">Nueva fecha:</span> <span class="new-value">%s</span></div>
                            <div class="info-row"><span class="info-label">Nueva hora:</span> <span class="new-value">%s</span></div>
                        </div>
                        <div class="reminder-box">
                            <strong>⏰ Recordatorio:</strong> Por favor, llega 15 minutos antes de tu cita.
                        </div>
                        <p>Te esperamos en la nueva fecha. Si tienes alguna pregunta, no dudes en contactarnos.</p>
                    </div>
                    <div class="footer">
                        <p>Este es un correo automático, por favor no respondas a este mensaje.</p>
                        <p>© 2025 %s. Todos los derechos reservados.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(clientName, empresaName, fechaAnterior, serviceName, employeeName, 
                         nuevaFecha, nuevaHora, empresaName);
    }
}
