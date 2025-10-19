package apex.code.clipperBarberShop.user.domain.port.in;

import apex.code.clipperBarberShop.user.application.dto.*;

import java.util.List;

/**
 * Puerto de entrada para casos de uso de gestión de usuarios
 */
public interface UserManagementUseCase {
    
    /**
     * Obtiene un usuario por su ID
     */
    UsuarioResponse obtenerUsuario(String userId);
    
    /**
     * Lista todos los usuarios activos de una empresa
     */
    List<UsuarioResponse> listarUsuariosPorEmpresa(Long empresaId);
    
    /**
     * Lista todos los usuarios activos del sistema (solo para OWNER)
     */
    List<UsuarioResponse> listarTodosLosUsuarios();
    
    /**
     * Lista usuarios eliminados (papelera)
     */
    List<UsuarioResponse> listarUsuariosEliminados(Long empresaId);
    
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
     */
    UsuarioResponse restaurarUsuario(String userId);
}
