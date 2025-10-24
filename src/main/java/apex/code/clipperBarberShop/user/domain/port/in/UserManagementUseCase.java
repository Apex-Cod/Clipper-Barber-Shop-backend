package apex.code.clipperBarberShop.user.domain.port.in;

import apex.code.clipperBarberShop.user.application.dto.*;

import java.util.List;

/**
 * Puerto de entrada para casos de uso de gestión de usuarios
 */
public interface UserManagementUseCase {
    
    /**
     * Obtiene un usuario por su ID
     * ADMIN: Puede ver cualquier usuario
     * OWNER: Solo puede ver usuarios de su empresa
     */
    UsuarioResponse obtenerUsuario(String userId, String requestedBy);
    
    /**
     * Lista todos los usuarios activos de una empresa
     * ADMIN: Puede listar usuarios de cualquier empresa
     * OWNER: Solo puede listar usuarios de su propia empresa
     */
    List<UsuarioResponse> listarUsuariosPorEmpresa(Long empresaId, String requestedBy);
    
    /**
     * Lista todos los usuarios activos del sistema (solo para ADMIN)
     */
    List<UsuarioResponse> listarTodosLosUsuarios();
    
    /**
     * Lista usuarios eliminados (papelera)
     * ADMIN: Puede ver eliminados de todas las empresas (si no se especifica empresaId)
     * OWNER: Solo puede ver eliminados de su empresa
     */
    List<UsuarioResponse> listarUsuariosEliminados(Long empresaId, String requestedBy);
    
    /**
     * Actualiza la información de un usuario
     */
    UsuarioResponse actualizarUsuario(String userId, ActualizarUsuarioRequest request, String updatedBy);
    
    /**
     * Cambia la contraseña de un usuario
     */
    void cambiarPassword(String userId, CambiarPasswordRequest request);
    
    /**
     * Cambia el estado activo/inactivo de un usuario
     */
    UsuarioResponse cambiarEstado(String userId, CambiarEstadoRequest request, String updatedBy);
    
    /**
     * Elimina un usuario (soft delete)
     */
    void eliminarUsuario(String userId, String deletedBy);
    
    /**
     * Restaura un usuario eliminado
     * ADMIN: Puede restaurar cualquier usuario
     * OWNER: Solo puede restaurar usuarios de su empresa
     */
    UsuarioResponse restaurarUsuario(String userId, String restoredBy);
}
