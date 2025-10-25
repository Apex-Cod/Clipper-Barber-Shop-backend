package apex.code.clipperBarberShop.empresa.domain.port.in;

import apex.code.clipperBarberShop.empresa.application.dto.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Puerto de entrada para casos de uso de configuración de empresa
 */
public interface EmpresaConfigUseCase {
    
    /**
     * Obtiene la configuración completa de la empresa del usuario autenticado (OWNER)
     * @param userId ID del usuario (OWNER) autenticado
     */
    EmpresaConfigResponse obtenerConfiguracion(String userId);
    
    /**
     * Actualiza la información básica de la empresa del usuario autenticado (OWNER)
     * @param userId ID del usuario (OWNER) autenticado
     */
    EmpresaConfigResponse actualizarInformacionBasica(String userId, ActualizarInformacionBasicaRequest request);
    
    /**
     * Actualiza los horarios de atención de la empresa del usuario autenticado (OWNER)
     * @param userId ID del usuario (OWNER) autenticado
     */
    EmpresaConfigResponse actualizarHorarios(String userId, ActualizarHorariosRequest request);
    
    /**
     * Actualiza la ubicación de la empresa del usuario autenticado (OWNER)
     * @param userId ID del usuario (OWNER) autenticado
     */
    EmpresaConfigResponse actualizarUbicacion(String userId, ActualizarUbicacionRequest request);
    
    /**
     * Sube el logo de la empresa del usuario autenticado (OWNER) a Supabase y actualiza la URL
     * @param userId ID del usuario (OWNER) autenticado
     */
    EmpresaConfigResponse subirLogo(String userId, MultipartFile archivo);
    
    /**
     * Sube el banner de la empresa del usuario autenticado (OWNER) a Supabase y actualiza la URL
     * @param userId ID del usuario (OWNER) autenticado
     */
    EmpresaConfigResponse subirBanner(String userId, MultipartFile archivo);
    
    /**
     * Elimina el logo de la empresa del usuario autenticado (OWNER)
     * @param userId ID del usuario (OWNER) autenticado
     */
    void eliminarLogo(String userId);
    
    /**
     * Elimina el banner de la empresa del usuario autenticado (OWNER)
     * @param userId ID del usuario (OWNER) autenticado
     */
    void eliminarBanner(String userId);
}
