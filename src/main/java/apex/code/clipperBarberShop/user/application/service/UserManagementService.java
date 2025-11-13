package apex.code.clipperBarberShop.user.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.register.domain.port.out.EmpresaRepositoryPort;
import apex.code.clipperBarberShop.shared.util.StringUtils;
import apex.code.clipperBarberShop.user.application.dto.*;
import apex.code.clipperBarberShop.user.domain.port.in.UserManagementUseCase;
import apex.code.clipperBarberShop.user.domain.port.out.UserRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de gestión de usuarios
 */
@Service
@RequiredArgsConstructor
public class UserManagementService implements UserManagementUseCase {
    
    private final UserRepositoryPort userRepository;
    private final EmpresaRepositoryPort empresaRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UsuarioResponse obtenerUsuario(String userId, String requestedBy) {
        Usuario usuario = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Verificar permisos
        validarAccesoUsuario(requestedBy, usuario);
        
        return mapToResponse(usuario);
    }
    
    @Override
    public List<UsuarioResponse> listarUsuariosPorEmpresa(Long empresaId, String requestedBy) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
        // Verificar permisos: OWNER solo puede ver usuarios de su empresa
        validarAccesoEmpresa(requestedBy, empresaId);
        
        return userRepository.findByEmpresaAndDeletedFalse(empresa)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UsuarioResponse> listarTodosLosUsuarios() {
        return userRepository.findByDeletedFalse()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UsuarioResponse> listarUsuariosEliminados(Long empresaId, String requestedBy) {
        Usuario solicitante = userRepository.findById(requestedBy)
                .orElseThrow(() -> new IllegalArgumentException("Usuario solicitante no encontrado"));
        
        // Si empresaId es null y el usuario es OWNER, usar su empresa
        if (empresaId == null && "OWNER".equals(solicitante.getRole())) {
            if (solicitante.getEmpresa() == null) {
                throw new IllegalArgumentException("OWNER debe tener una empresa asociada");
            }
            empresaId = solicitante.getEmpresa().getId();
        }
        
        if (empresaId != null) {
            // Verificar permisos: OWNER solo puede ver su empresa
            validarAccesoEmpresa(requestedBy, empresaId);
            
            Empresa empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
            
            return userRepository.findByEmpresaAndDeletedTrue(empresa)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        
        // Solo ADMIN puede ver todos los eliminados sin filtro de empresa
        if (!"ADMIN".equals(solicitante.getRole())) {
            throw new IllegalArgumentException("No tiene permisos para ver todos los usuarios eliminados");
        }
        
        return userRepository.findByDeletedTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public UsuarioResponse actualizarUsuario(String userId, ActualizarUsuarioRequest request, String updatedBy) {
        Usuario usuario = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (usuario.getDeleted()) {
            throw new IllegalArgumentException("No se puede actualizar un usuario eliminado");
        }
        
        // Verificar permisos
        validarAccesoUsuario(updatedBy, usuario);
        
        // Sanitizar datos
        String name = StringUtils.sanitize(request.getName());
        String lastName = StringUtils.sanitize(request.getLastName());
        String email = StringUtils.sanitizeEmail(request.getEmail());
        
        // Verificar que el email no esté en uso por otro usuario
        if (!usuario.getEmail().equalsIgnoreCase(email)) {
            if (userRepository.existsByEmailAndIdNotAndDeletedFalse(email, userId)) {
                throw new IllegalArgumentException("El email ya está en uso por otro usuario");
            }
        }
        
        // Actualizar campos
        usuario.setName(name);
        usuario.setLastName(lastName);
        usuario.setEmail(email);
        
        usuario = userRepository.save(usuario);
        
        return mapToResponse(usuario);
    }
    
    @Override
    @Transactional
    public void cambiarPassword(String userId, CambiarPasswordRequest request) {
        Usuario usuario = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (usuario.getDeleted()) {
            throw new IllegalArgumentException("No se puede cambiar la contraseña de un usuario eliminado");
        }
        
        // Verificar contraseña actual
        if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        
        // Verificar que las contraseñas coincidan
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        
        // Verificar que la nueva contraseña sea diferente a la actual
        if (passwordEncoder.matches(request.getNewPassword(), usuario.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }
        
        // Actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(usuario);
    }
    
    @Override
    @Transactional
    public UsuarioResponse cambiarEstado(String userId, CambiarEstadoRequest request, String updatedBy) {
        Usuario usuario = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (usuario.getDeleted()) {
            throw new IllegalArgumentException("No se puede cambiar el estado de un usuario eliminado");
        }
        
        // Verificar permisos
        validarAccesoUsuario(updatedBy, usuario);
        
        usuario.setActivo(request.getActivo());
        usuario = userRepository.save(usuario);
        
        return mapToResponse(usuario);
    }
    
    @Override
    @Transactional
    public void eliminarUsuario(String userId, String deletedBy) {
        Usuario usuario = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (usuario.getDeleted()) {
            throw new IllegalArgumentException("El usuario ya está eliminado");
        }
        
        // Verificar permisos
        validarAccesoUsuario(deletedBy, usuario);
        
        // Soft delete
        usuario.softDelete(deletedBy);
        usuario.setActivo(false); // También marcamos como inactivo
        userRepository.save(usuario);
    }
    
    @Override
    @Transactional
    public UsuarioResponse restaurarUsuario(String userId, String restoredBy) {
        Usuario usuario = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (!usuario.getDeleted()) {
            throw new IllegalArgumentException("El usuario no está eliminado");
        }
        
        // Verificar permisos
        validarAccesoUsuario(restoredBy, usuario);
        
        // Verificar que el email no esté en uso (por si se reutilizó)
        if (userRepository.existsByEmailAndDeletedFalse(usuario.getEmail())) {
            throw new IllegalArgumentException(
                "No se puede restaurar: el email ya está en uso por otro usuario activo"
            );
        }
        
        // Restaurar
        usuario.restore();
        usuario.setActivo(true); // Volver a activar
        usuario = userRepository.save(usuario);
        
        return mapToResponse(usuario);
    }
    
    /**
     * Valida que el usuario solicitante tenga acceso al usuario objetivo
     * ADMIN: Acceso total
     * OWNER: Solo usuarios de su empresa
     */
    private void validarAccesoUsuario(String solicitanteId, Usuario usuarioObjetivo) {
        Usuario solicitante = userRepository.findById(solicitanteId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario solicitante no encontrado"));
        
        // Cualquier usuario puede acceder a su propio perfil
        if (solicitante.getId().equals(usuarioObjetivo.getId())) {
            return;
        }
        
        // ADMIN tiene acceso total
        if ("ADMIN".equals(solicitante.getRole())) {
            return;
        }
        
        // OWNER solo puede acceder a usuarios de su empresa
        if ("OWNER".equals(solicitante.getRole())) {
            // El OWNER debe tener una empresa
            if (solicitante.getEmpresa() == null) {
                throw new IllegalArgumentException("OWNER debe tener una empresa asociada");
            }
            
            // El usuario objetivo debe tener empresa (empleados)
            if (usuarioObjetivo.getEmpresa() == null) {
                throw new IllegalArgumentException("No tiene permisos para acceder a este usuario");
            }
            
            // Debe ser de la misma empresa
            if (!solicitante.getEmpresa().getId().equals(usuarioObjetivo.getEmpresa().getId())) {
                throw new IllegalArgumentException("No tiene permisos para acceder a este usuario");
            }
            return;
        }
        
        // Otros roles no tienen acceso a perfiles de otros usuarios
        throw new IllegalArgumentException("No tiene permisos para realizar esta acción");
    }
    
    /**
     * Valida que el usuario solicitante tenga acceso a una empresa
     * ADMIN: Acceso a todas las empresas
     * OWNER: Solo su propia empresa
     */
    private void validarAccesoEmpresa(String solicitanteId, Long empresaId) {
        Usuario solicitante = userRepository.findById(solicitanteId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario solicitante no encontrado"));
        
        // ADMIN tiene acceso total
        if ("ADMIN".equals(solicitante.getRole())) {
            return;
        }
        
        // OWNER solo puede acceder a su empresa
        if ("OWNER".equals(solicitante.getRole())) {
            if (solicitante.getEmpresa() == null) {
                throw new IllegalArgumentException("OWNER debe tener una empresa asociada");
            }
            
            if (!solicitante.getEmpresa().getId().equals(empresaId)) {
                throw new IllegalArgumentException("No tiene permisos para acceder a esta empresa");
            }
            return;
        }
        
        // Otros roles no tienen acceso
        throw new IllegalArgumentException("No tiene permisos para realizar esta acción");
    }
    
    /**
     * Mapea una entidad Usuario a UsuarioResponse
     */
    private UsuarioResponse mapToResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .empresaId(usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null)
                .empresaNombre(usuario.getEmpresa() != null ? usuario.getEmpresa().getNombre() : null)
                .name(usuario.getName())
                .lastName(usuario.getLastName())
                .email(usuario.getEmail())
                .role(usuario.getRole())
                .activo(usuario.getActivo())
                .registrationDate(usuario.getRegistrationDate())
                .deleted(usuario.getDeleted())
                .deletedAt(usuario.getDeletedAt())
                .deletedBy(usuario.getDeletedBy())
                .build();
    }
}
