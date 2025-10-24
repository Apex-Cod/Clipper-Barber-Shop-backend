package apex.code.clipperBarberShop.empresa.domain.port.in;

import apex.code.clipperBarberShop.empresa.application.dto.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Puerto de entrada para casos de uso de configuración de empresa
 */
public interface EmpresaConfigUseCase {
    
    /**
     * Obtiene la configuración completa de una empresa
     */
    EmpresaConfigResponse obtenerConfiguracion(Long empresaId);
    
    /**
     * Actualiza la información básica de la empresa
     */
    EmpresaConfigResponse actualizarInformacionBasica(Long empresaId, ActualizarInformacionBasicaRequest request);
    
    /**
     * Actualiza los horarios de atención de la empresa
     */
    EmpresaConfigResponse actualizarHorarios(Long empresaId, ActualizarHorariosRequest request);
    
    /**
     * Actualiza la ubicación de la empresa
     */
    EmpresaConfigResponse actualizarUbicacion(Long empresaId, ActualizarUbicacionRequest request);
    
    /**
     * Sube el logo de la empresa a Supabase y actualiza la URL
     */
    EmpresaConfigResponse subirLogo(Long empresaId, MultipartFile archivo);
    
    /**
     * Sube el banner de la empresa a Supabase y actualiza la URL
     */
    EmpresaConfigResponse subirBanner(Long empresaId, MultipartFile archivo);
    
    /**
     * Elimina el logo de la empresa
     */
    void eliminarLogo(Long empresaId);
    
    /**
     * Elimina el banner de la empresa
     */
    void eliminarBanner(Long empresaId);
}
