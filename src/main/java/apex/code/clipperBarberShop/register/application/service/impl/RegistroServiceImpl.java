package apex.code.clipperBarberShop.register.application.service.impl;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.register.application.dto.ClienteRequest;
import apex.code.clipperBarberShop.register.application.dto.EmpleadoRequest;
import apex.code.clipperBarberShop.register.application.dto.RegistroRequest;
import apex.code.clipperBarberShop.register.application.service.RegistroService;
import apex.code.clipperBarberShop.register.domain.port.out.EmpresaRepositoryPort;
import apex.code.clipperBarberShop.register.domain.port.out.UsuarioRepositoryPort;
import apex.code.clipperBarberShop.servicio.application.service.ServicioDefaultService;
import apex.code.clipperBarberShop.shared.email.EmailService;
import apex.code.clipperBarberShop.shared.util.VerificationCodeGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistroServiceImpl implements RegistroService {

    private final EmpresaRepositoryPort empresaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ServicioDefaultService servicioDefaultService;
    
    @Value("${app.verification.use-code:true}")
    private boolean useVerificationCode;
    
    @Value("${app.verification.expiry-minutes:15}")
    private int expiryMinutes;

    @Override
    @Transactional
    public void registrarEmpresaConAdmin(RegistroRequest request) {
        // Sanitizar datos: eliminar espacios en blanco al inicio y final
        String empresaNombre = request.getEmpresaNombre().trim();
        String empresaEmail = request.getEmpresaEmail().trim().toLowerCase();
        String adminName = request.getAdminName().trim();
        String adminLastName = request.getAdminLastName().trim();
        String adminEmail = request.getAdminEmail().trim().toLowerCase();
        
        // Verificar que el email no esté ya registrado
        if (usuarioRepository.findByEmail(adminEmail).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        
        Empresa empresa = Empresa.builder()
                .nombre(empresaNombre)
                .email(empresaEmail)
                .publicoObjetivo(request.getEmpresaPublicoObjetivo())
                .build();

        empresa = empresaRepository.save(empresa);
        
        // Crear servicios por defecto según el público objetivo
        try {
            servicioDefaultService.crearServiciosPorDefecto(empresa);
            log.info("Servicios por defecto creados para empresa: {}", empresa.getNombre());
        } catch (Exception e) {
            log.error("Error al crear servicios por defecto para empresa: {}", empresa.getNombre(), e);
            // No lanzamos excepción para no bloquear el registro
        }

        // Generar token de verificación (código de 6 dígitos o UUID según configuración)
        String verificationToken = useVerificationCode ? 
            VerificationCodeGenerator.generateCode() : 
            UUID.randomUUID().toString();
        
        Usuario admin = Usuario.builder()
                .id(UUID.randomUUID().toString())
                .empresa(empresa)
                .name(adminName)
                .lastName(adminLastName)
                .email(adminEmail)
                .password(passwordEncoder.encode(request.getAdminPassword()))
                .role("OWNER")
                .emailVerified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(java.time.LocalDateTime.now().plusMinutes(expiryMinutes))
                .verificationAttempts(0)
                .activo(false) // Inactivo hasta verificar email
                .build();

        usuarioRepository.save(admin);
        
        // Enviar email de verificación
        try {
            emailService.sendVerificationEmail(adminEmail, adminName, verificationToken);
            log.info("Email de verificación enviado a: {}", adminEmail);
        } catch (Exception e) {
            log.error("Error al enviar email de verificación", e);
            // No lanzamos excepción para no bloquear el registro
        }
    }

    @Override
    @Transactional
    public void registrarEmpleado(EmpleadoRequest request) {
        // Sanitizar datos
        String name = request.getName().trim();
        String lastName = request.getLastName().trim();
        String email = request.getEmail().trim().toLowerCase();
        
        // Verificar que el email no esté ya registrado
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        
        Long empresaId = null;
        try{
            empresaId = Long.parseLong(request.getEmpresaId().trim());
        }catch(Exception e){
            throw new IllegalArgumentException("empresaId inválido");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        // Generar token de verificación (código de 6 dígitos o UUID según configuración)
        String verificationToken = useVerificationCode ? 
            VerificationCodeGenerator.generateCode() : 
            UUID.randomUUID().toString();
        
        Usuario empleado = Usuario.builder()
                .id(UUID.randomUUID().toString())
                .empresa(empresa)
                .name(name)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role("EMPLOYEE")
                .emailVerified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(java.time.LocalDateTime.now().plusMinutes(expiryMinutes))
                .verificationAttempts(0)
                .activo(false) // Inactivo hasta verificar email
                .build();

        usuarioRepository.save(empleado);
        
        // Enviar email de verificación
        try {
            emailService.sendVerificationEmail(email, name, verificationToken);
            log.info("Email de verificación enviado a: {}", email);
        } catch (Exception e) {
            log.error("Error al enviar email de verificación", e);
        }
    }

    @Override
    @Transactional
    public void registrarCliente(ClienteRequest request) {
        // Sanitizar datos
        String name = request.getName().trim();
        String lastName = request.getLastName().trim();
        String email = request.getEmail().trim().toLowerCase();
        
        // Verificar que el email no esté ya registrado
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        
        // Generar token de verificación (código de 6 dígitos o UUID según configuración)
        String verificationToken = useVerificationCode ? 
            VerificationCodeGenerator.generateCode() : 
            UUID.randomUUID().toString();
        
        Usuario cliente = Usuario.builder()
                .id(UUID.randomUUID().toString())
                .empresa(null)
                .name(name)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role("CLIENT")
                .emailVerified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(java.time.LocalDateTime.now().plusMinutes(expiryMinutes))
                .verificationAttempts(0)
                .activo(false) // Inactivo hasta verificar email
                .build();

        usuarioRepository.save(cliente);
        
        // Enviar email de verificación
        try {
            emailService.sendVerificationEmail(email, name, verificationToken);
            log.info("Email de verificación enviado a: {}", email);
        } catch (Exception e) {
            log.error("Error al enviar email de verificación", e);
        }
    }

    @Override
    @Transactional
    public void registrarEmpleadoByAdmin(EmpleadoRequest request, String adminUserId) {
        // Sanitizar datos
        String name = request.getName().trim();
        String lastName = request.getLastName().trim();
        String email = request.getEmail().trim().toLowerCase();
        
        // Verificar que el email no esté ya registrado
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        
        // find admin user
        Usuario admin = usuarioRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin no encontrado"));

        if(!"OWNER".equals(admin.getRole())){
            throw new IllegalArgumentException("El usuario no tiene permisos para crear empleados");
        }

        Empresa empresa = admin.getEmpresa();
        if(empresa == null) throw new IllegalArgumentException("Admin no asociado a ninguna empresa");

        // Generar token de verificación (código de 6 dígitos o UUID según configuración)
        String verificationToken = useVerificationCode ? 
            VerificationCodeGenerator.generateCode() : 
            UUID.randomUUID().toString();
        
        Usuario empleado = Usuario.builder()
                .id(UUID.randomUUID().toString())
                .empresa(empresa)
                .name(name)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role("EMPLOYEE")
                .emailVerified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(java.time.LocalDateTime.now().plusMinutes(expiryMinutes))
                .verificationAttempts(0)
                .activo(false) // Inactivo hasta verificar email
                .build();

        usuarioRepository.save(empleado);
        
        // Enviar email de verificación
        try {
            emailService.sendVerificationEmail(email, name, verificationToken);
            log.info("Email de verificación enviado a: {}", email);
        } catch (Exception e) {
            log.error("Error al enviar email de verificación", e);
        }
    }
}
