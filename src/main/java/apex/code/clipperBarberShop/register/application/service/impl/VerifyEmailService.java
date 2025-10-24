package apex.code.clipperBarberShop.register.application.service.impl;

import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.register.domain.port.in.VerifyEmailUseCase;
import apex.code.clipperBarberShop.register.domain.port.out.EmailVerificationRepositoryPort;
import apex.code.clipperBarberShop.register.domain.port.out.UsuarioRepositoryPort;
import apex.code.clipperBarberShop.shared.email.EmailService;
import apex.code.clipperBarberShop.shared.util.VerificationCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio de verificación de email
 * Soporta dos métodos:
 * 1. Token UUID (para web) - enlace clickeable
 * 2. Código de 6 dígitos (para móvil) - ingreso manual
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VerifyEmailService implements VerifyEmailUseCase {
    
    private final EmailVerificationRepositoryPort verificationRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final EmailService emailService;
    
    @Value("${app.verification.use-code:true}")
    private boolean useVerificationCode; // true = código 6 dígitos (móvil), false = token UUID (web)
    
    @Value("${app.verification.max-attempts:3}")
    private int maxVerificationAttempts;
    
    @Value("${app.verification.expiry-minutes:15}")
    private int expiryMinutes;
    
    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Código de verificación inválido");
        }
        
        String normalizedToken = token.trim();
        
        Usuario usuario = verificationRepository.findByVerificationToken(normalizedToken)
                .orElseThrow(() -> new IllegalArgumentException("Código de verificación inválido o expirado"));
        
        // Verificar que el usuario no esté eliminado
        if (usuario.getDeleted()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        
        // Verificar si ya está verificado
        if (usuario.getEmailVerified()) {
            log.info("El usuario {} ya tiene el email verificado", usuario.getEmail());
            return true;
        }
        
        // Verificar si el token expiró
        if (usuario.getVerificationTokenExpiry() != null && 
            usuario.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El código de verificación ha expirado. Por favor, solicita uno nuevo.");
        }
        
        // Verificar número de intentos (solo para códigos de 6 dígitos)
        if (useVerificationCode && usuario.getVerificationAttempts() != null && 
            usuario.getVerificationAttempts() >= maxVerificationAttempts) {
            throw new IllegalArgumentException("Has excedido el número máximo de intentos. Por favor, solicita un nuevo código.");
        }
        
        // Marcar como verificado
        usuario.setEmailVerified(true);
        usuario.setVerificationToken(null);
        usuario.setVerificationTokenExpiry(null);
        usuario.setVerificationAttempts(0);
        usuario.setActivo(true); // Activar el usuario
        
        verificationRepository.save(usuario);
        
        log.info("Email verificado exitosamente para el usuario: {}", usuario.getEmail());
        
        // Enviar email de bienvenida
        try {
            emailService.sendWelcomeEmail(usuario.getEmail(), usuario.getName());
        } catch (Exception e) {
            log.error("Error al enviar email de bienvenida", e);
            // No lanzamos excepción porque la verificación ya fue exitosa
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean verifyEmailWithCode(String email, String code) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email es requerido");
        }
        
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Código de verificación es requerido");
        }
        
        String normalizedEmail = email.trim().toLowerCase();
        String normalizedCode = code.trim();
        
        // Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar que el usuario no esté eliminado
        if (usuario.getDeleted()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        
        // Verificar si ya está verificado
        if (usuario.getEmailVerified()) {
            log.info("El usuario {} ya tiene el email verificado", usuario.getEmail());
            return true;
        }
        
        // Verificar si el token expiró
        if (usuario.getVerificationTokenExpiry() != null && 
            usuario.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El código de verificación ha expirado. Por favor, solicita uno nuevo.");
        }
        
        // Verificar número de intentos
        Integer attempts = usuario.getVerificationAttempts() != null ? usuario.getVerificationAttempts() : 0;
        if (attempts >= maxVerificationAttempts) {
            throw new IllegalArgumentException("Has excedido el número máximo de intentos. Por favor, solicita un nuevo código.");
        }
        
        // Verificar que el código coincida
        if (!normalizedCode.equals(usuario.getVerificationToken())) {
            // Incrementar intentos fallidos
            usuario.setVerificationAttempts(attempts + 1);
            usuarioRepository.save(usuario);
            
            int remainingAttempts = maxVerificationAttempts - (attempts + 1);
            log.warn("Código incorrecto para {}: intento {} de {}", 
                email, attempts + 1, maxVerificationAttempts);
            
            if (remainingAttempts > 0) {
                throw new IllegalArgumentException(
                    "Código incorrecto. Te quedan " + remainingAttempts + " intentos.");
            } else {
                throw new IllegalArgumentException(
                    "Has excedido el número máximo de intentos. Por favor, solicita un nuevo código.");
            }
        }
        
        // Código correcto - marcar como verificado
        usuario.setEmailVerified(true);
        usuario.setVerificationToken(null);
        usuario.setVerificationTokenExpiry(null);
        usuario.setVerificationAttempts(0);
        usuario.setActivo(true);
        
        verificationRepository.save(usuario);
        
        log.info("Email verificado exitosamente para el usuario: {}", usuario.getEmail());
        
        // Enviar email de bienvenida
        try {
            emailService.sendWelcomeEmail(usuario.getEmail(), usuario.getName());
        } catch (Exception e) {
            log.error("Error al enviar email de bienvenida", e);
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email inválido");
        }
        
        String normalizedEmail = email.trim().toLowerCase();
        
        Usuario usuario = usuarioRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar que el usuario no esté eliminado
        if (usuario.getDeleted()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        
        // Verificar si ya está verificado
        if (usuario.getEmailVerified()) {
            throw new IllegalArgumentException("El email ya está verificado");
        }
        
        // Generar nuevo token (código o UUID según configuración)
        String newToken = useVerificationCode ? 
            VerificationCodeGenerator.generateCode() : 
            UUID.randomUUID().toString();
            
        usuario.setVerificationToken(newToken);
        usuario.setVerificationTokenExpiry(LocalDateTime.now().plusMinutes(expiryMinutes));
        usuario.setVerificationAttempts(0); // Resetear intentos
        
        usuarioRepository.save(usuario);
        
        // Enviar email
        try {
            emailService.sendVerificationEmail(usuario.getEmail(), usuario.getName(), newToken);
            log.info("Email de verificación reenviado a: {}", usuario.getEmail());
        } catch (Exception e) {
            log.error("Error al reenviar email de verificación", e);
            throw new RuntimeException("Error al enviar el email de verificación. Por favor, intenta más tarde.");
        }
    }
    
    /**
     * Registra un intento fallido de verificación
     * Solo aplica para códigos de 6 dígitos (móvil)
     */
    @Transactional
    public void registerFailedAttempt(String email) {
        if (!useVerificationCode) {
            return; // Solo para códigos, no para tokens UUID
        }
        
        String normalizedEmail = email.trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmail(normalizedEmail).orElse(null);
        
        if (usuario != null && !usuario.getEmailVerified()) {
            int attempts = usuario.getVerificationAttempts() != null ? usuario.getVerificationAttempts() : 0;
            usuario.setVerificationAttempts(attempts + 1);
            usuarioRepository.save(usuario);
            
            log.warn("Intento fallido de verificación para {}: {} de {}", 
                email, attempts + 1, maxVerificationAttempts);
        }
    }
}
