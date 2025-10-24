package apex.code.clipperBarberShop.user.adapters.in.web;

import apex.code.clipperBarberShop.shared.ApiResponse;
import apex.code.clipperBarberShop.user.application.dto.*;
import apex.code.clipperBarberShop.user.domain.port.in.UserManagementUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Controlador REST para gestión de usuarios
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserManagementController {
    
    private final UserManagementUseCase userManagementUseCase;
    
    /**
     * Obtener información de un usuario específico
     * ADMIN: Puede ver cualquier usuario
     * OWNER: Solo puede ver usuarios de su empresa
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerUsuario(
            @PathVariable String id,
            Principal principal) {
        UsuarioResponse usuario = userManagementUseCase.obtenerUsuario(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Usuario obtenido", usuario));
    }
    
    /**
     * Obtener el perfil del usuario autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerPerfil(Principal principal) {
        UsuarioResponse usuario = userManagementUseCase.obtenerUsuario(principal.getName(), principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Perfil obtenido", usuario));
    }
    
    /**
     * Listar todos los usuarios de una empresa
     * ADMIN: Puede listar usuarios de cualquier empresa
     * OWNER: Solo puede listar usuarios de su propia empresa
     */
    @GetMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listarUsuariosPorEmpresa(
            @PathVariable Long empresaId,
            Principal principal) {
        List<UsuarioResponse> usuarios = userManagementUseCase.listarUsuariosPorEmpresa(empresaId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Usuarios obtenidos", usuarios));
    }
    
    /**
     * Listar todos los usuarios del sistema (solo para ADMIN)
     * ADMIN: Acceso total a todos los usuarios
     * OWNER: No tiene acceso a este endpoint
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listarTodosLosUsuarios() {
        List<UsuarioResponse> usuarios = userManagementUseCase.listarTodosLosUsuarios();
        return ResponseEntity.ok(ApiResponse.success("Usuarios obtenidos", usuarios));
    }
    
    /**
     * Listar usuarios eliminados (papelera)
     * ADMIN: Puede ver eliminados de todas las empresas (si no se especifica empresaId)
     * OWNER: Solo puede ver eliminados de su empresa
     */
    @GetMapping("/deleted")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listarUsuariosEliminados(
            @RequestParam(required = false) Long empresaId,
            Principal principal) {
        List<UsuarioResponse> usuarios = userManagementUseCase.listarUsuariosEliminados(empresaId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Usuarios eliminados obtenidos", usuarios));
    }
    
    /**
     * Actualizar información de un usuario
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarUsuario(
            @PathVariable String id,
            @Valid @RequestBody ActualizarUsuarioRequest request,
            Principal principal) {
        UsuarioResponse usuario = userManagementUseCase.actualizarUsuario(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Usuario actualizado", usuario));
    }
    
    /**
     * Actualizar el perfil del usuario autenticado
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarPerfil(
            @Valid @RequestBody ActualizarUsuarioRequest request,
            Principal principal) {
        UsuarioResponse usuario = userManagementUseCase.actualizarUsuario(
                principal.getName(), request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Perfil actualizado", usuario));
    }
    
    /**
     * Cambiar contraseña
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Object>> cambiarPassword(
            @PathVariable String id,
            @Valid @RequestBody CambiarPasswordRequest request,
            Principal principal) {
        
        // Solo puede cambiar su propia contraseña o OWNER puede cambiar cualquiera
        if (!id.equals(principal.getName())) {
            throw new IllegalArgumentException("No tiene permisos para cambiar esta contraseña");
        }
        
        userManagementUseCase.cambiarPassword(id, request);
        return ResponseEntity.ok(ApiResponse.success("Contraseña actualizada", null));
    }
    
    /**
     * Cambiar contraseña del usuario autenticado
     */
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Object>> cambiarMiPassword(
            @Valid @RequestBody CambiarPasswordRequest request,
            Principal principal) {
        userManagementUseCase.cambiarPassword(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Contraseña actualizada", null));
    }
    
    /**
     * Cambiar estado activo/inactivo de un usuario
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cambiarEstado(
            @PathVariable String id,
            @Valid @RequestBody CambiarEstadoRequest request,
            Principal principal) {
        UsuarioResponse usuario = userManagementUseCase.cambiarEstado(id, request, principal.getName());
        String mensaje = request.getActivo() ? "Usuario activado" : "Usuario desactivado";
        return ResponseEntity.ok(ApiResponse.success(mensaje, usuario));
    }
    
    /**
     * Activar un usuario
     */
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> activarUsuario(
            @PathVariable String id,
            Principal principal) {
        CambiarEstadoRequest request = CambiarEstadoRequest.builder()
                .activo(true)
                .build();
        UsuarioResponse usuario = userManagementUseCase.cambiarEstado(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Usuario activado", usuario));
    }
    
    /**
     * Desactivar un usuario
     */
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> desactivarUsuario(
            @PathVariable String id,
            Principal principal) {
        CambiarEstadoRequest request = CambiarEstadoRequest.builder()
                .activo(false)
                .build();
        UsuarioResponse usuario = userManagementUseCase.cambiarEstado(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Usuario desactivado", usuario));
    }
    
    /**
     * Eliminar un usuario (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Object>> eliminarUsuario(
            @PathVariable String id,
            Principal principal) {
        userManagementUseCase.eliminarUsuario(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Usuario eliminado", null));
    }
    
    /**
     * Restaurar un usuario eliminado
     * ADMIN: Puede restaurar cualquier usuario
     * OWNER: Solo puede restaurar usuarios de su empresa
     */
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> restaurarUsuario(
            @PathVariable String id,
            Principal principal) {
        UsuarioResponse usuario = userManagementUseCase.restaurarUsuario(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Usuario restaurado", usuario));
    }
}
