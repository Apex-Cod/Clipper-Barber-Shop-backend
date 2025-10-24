package apex.code.clipperBarberShop.empresa.adapters.in.web;

import apex.code.clipperBarberShop.empresa.application.dto.*;
import apex.code.clipperBarberShop.empresa.domain.port.in.EmpresaConfigUseCase;
import apex.code.clipperBarberShop.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

/**
 * Controlador REST para configuración de empresa
 * Solo accesible por OWNER de la empresa
 */
@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaConfigController {
    
    private final EmpresaConfigUseCase empresaConfigUseCase;
    
    /**
     * Obtener configuración de la empresa del OWNER autenticado
     */
    @GetMapping("/mi-empresa/configuracion")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<EmpresaConfigResponse>> obtenerConfiguracion(Principal principal) {
        EmpresaConfigResponse config = empresaConfigUseCase.obtenerConfiguracion(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Configuración obtenida", config));
    }
    
    /**
     * Actualizar información básica de la empresa del OWNER autenticado
     */
    @PutMapping("/mi-empresa/informacion-basica")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<EmpresaConfigResponse>> actualizarInformacionBasica(
            @Valid @RequestBody ActualizarInformacionBasicaRequest request,
            Principal principal) {
        EmpresaConfigResponse config = empresaConfigUseCase.actualizarInformacionBasica(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Información actualizada", config));
    }
    
    /**
     * Actualizar horarios de atención de la empresa del OWNER autenticado
     */
    @PutMapping("/mi-empresa/horarios")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<EmpresaConfigResponse>> actualizarHorarios(
            @Valid @RequestBody ActualizarHorariosRequest request,
            Principal principal) {
        EmpresaConfigResponse config = empresaConfigUseCase.actualizarHorarios(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Horarios actualizados", config));
    }
    
    /**
     * Actualizar ubicación de la empresa del OWNER autenticado
     */
    @PutMapping("/mi-empresa/ubicacion")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<EmpresaConfigResponse>> actualizarUbicacion(
            @Valid @RequestBody ActualizarUbicacionRequest request,
            Principal principal) {
        EmpresaConfigResponse config = empresaConfigUseCase.actualizarUbicacion(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Ubicación actualizada", config));
    }
    
    /**
     * Subir logo de la empresa del OWNER autenticado
     */
    @PostMapping(value = "/mi-empresa/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<EmpresaConfigResponse>> subirLogo(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        EmpresaConfigResponse config = empresaConfigUseCase.subirLogo(principal.getName(), file);
        return ResponseEntity.ok(ApiResponse.success("Logo subido exitosamente", config));
    }
    
    /**
     * Subir banner de la empresa del OWNER autenticado
     */
    @PostMapping(value = "/mi-empresa/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<EmpresaConfigResponse>> subirBanner(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        EmpresaConfigResponse config = empresaConfigUseCase.subirBanner(principal.getName(), file);
        return ResponseEntity.ok(ApiResponse.success("Banner subido exitosamente", config));
    }
    
    /**
     * Eliminar logo de la empresa del OWNER autenticado
     */
    @DeleteMapping("/mi-empresa/logo")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Object>> eliminarLogo(Principal principal) {
        empresaConfigUseCase.eliminarLogo(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Logo eliminado", null));
    }
    
    /**
     * Eliminar banner de la empresa del OWNER autenticado
     */
    @DeleteMapping("/mi-empresa/banner")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Object>> eliminarBanner(Principal principal) {
        empresaConfigUseCase.eliminarBanner(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Banner eliminado", null));
    }
}
