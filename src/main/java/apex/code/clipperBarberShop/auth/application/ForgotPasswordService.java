package apex.code.clipperBarberShop.auth.application;

import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.auth.domain.ForgotPasswordUseCase;
import apex.code.clipperBarberShop.register.adapters.out.persistence.SpringDataUsuarioRepository;
import apex.code.clipperBarberShop.shared.email.EmailService;
import apex.code.clipperBarberShop.shared.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para la recuperación de contraseñas olvidadas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordService implements ForgotPasswordUseCase {
    
    private final SpringDataUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @Override
    @Transactional
    public String resetPassword(String email) {
        // Normalizar el email a minúsculas (RFC 5321/5322)
        String normalizedEmail = email.trim().toLowerCase();
        
        log.info("Iniciando recuperación de contraseña para: {}", normalizedEmail);
        
        // Buscar el usuario
        Usuario usuario = usuarioRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Intento de recuperación para email no registrado: {}", normalizedEmail);
                    return new RuntimeException("No existe una cuenta con este email");
                });
        
        // Verificar que el usuario esté activo
        if (!usuario.getActivo()) {
            log.warn("Intento de recuperación para cuenta inactiva: {}", normalizedEmail);
            throw new RuntimeException("La cuenta está desactivada");
        }
        
        // Verificar que el email esté verificado
        if (!usuario.getEmailVerified()) {
            log.warn("Intento de recuperación para email no verificado: {}", normalizedEmail);
            throw new RuntimeException("Debes verificar tu email antes de recuperar la contraseña");
        }
        
        // Generar contraseña temporal
        String temporaryPassword = PasswordGenerator.generateTemporaryPassword();
        log.info("Contraseña temporal generada para: {}", normalizedEmail);
        
        // Hashear y guardar la nueva contraseña
        usuario.setPassword(passwordEncoder.encode(temporaryPassword));
        usuarioRepository.save(usuario);
        log.info("Contraseña temporal guardada en BD para: {}", normalizedEmail);
        
        // Enviar email con la contraseña temporal
        try {
            emailService.sendTemporaryPasswordEmail(
                usuario.getEmail(), 
                usuario.getName(), 
                temporaryPassword
            );
            log.info("Email de recuperación enviado a: {}", normalizedEmail);
        } catch (Exception e) {
            log.error("Error al enviar email de recuperación a: {}", normalizedEmail, e);
            // No revertimos el cambio de contraseña, el usuario podrá intentar nuevamente
            throw new RuntimeException("Error al enviar el email. Por favor, intenta nuevamente más tarde.");
        }
        
        return "Se ha enviado una contraseña temporal a tu correo electrónico";
    }
}
