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
    public UsuarioResponse obtenerUsuario(String userId) {
        Usuario usuario = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        return mapToResponse(usuario);
    }
    
    @Override
    public List<UsuarioResponse> listarUsuariosPorEmpresa(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
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
    public List<UsuarioResponse> listarUsuariosEliminados(Long empresaId) {
        if (empresaId != null) {
            Empresa empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
            
            return userRepository.findByEmpresaAndDeletedTrue(empresa)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
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
        
        // Soft delete
        usuario.softDelete(deletedBy);
        usuario.setActivo(false); // También marcamos como inactivo
        userRepository.save(usuario);
    }
    
    @Override
    @Transactional
    public UsuarioResponse restaurarUsuario(String userId) {
        Usuario usuario = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (!usuario.getDeleted()) {
            throw new IllegalArgumentException("El usuario no está eliminado");
        }
        
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
