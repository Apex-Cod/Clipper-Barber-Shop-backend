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

/**
 * Controlador REST para configuración de empresa
 */
@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaConfigController {
    
    private final EmpresaConfigUseCase empresaConfigUseCase;
    
    /**
     * Obtener configuración de la empresa
     */
    @GetMapping("/{empresaId}/configuracion")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EmpresaConfigResponse>> obtenerConfiguracion(
            @PathVariable Long empresaId) {
        EmpresaConfigResponse config = empresaConfigUseCase.obtenerConfiguracion(empresaId);
        return ResponseEntity.ok(ApiResponse.success("Configuración obtenida", config));
    }
    
    /**
     * Actualizar información básica de la empresa
     */
    @PutMapping("/{empresaId}/informacion-basica")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EmpresaConfigResponse>> actualizarInformacionBasica(
            @PathVariable Long empresaId,
            @Valid @RequestBody ActualizarInformacionBasicaRequest request) {
        EmpresaConfigResponse config = empresaConfigUseCase.actualizarInformacionBasica(empresaId, request);
        return ResponseEntity.ok(ApiResponse.success("Información actualizada", config));
    }
    
    /**
     * Actualizar horarios de atención
     */
    @PutMapping("/{empresaId}/horarios")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EmpresaConfigResponse>> actualizarHorarios(
            @PathVariable Long empresaId,
            @Valid @RequestBody ActualizarHorariosRequest request) {
        EmpresaConfigResponse config = empresaConfigUseCase.actualizarHorarios(empresaId, request);
        return ResponseEntity.ok(ApiResponse.success("Horarios actualizados", config));
    }
    
    /**
     * Actualizar ubicación de la empresa
     */
    @PutMapping("/{empresaId}/ubicacion")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EmpresaConfigResponse>> actualizarUbicacion(
            @PathVariable Long empresaId,
            @Valid @RequestBody ActualizarUbicacionRequest request) {
        EmpresaConfigResponse config = empresaConfigUseCase.actualizarUbicacion(empresaId, request);
        return ResponseEntity.ok(ApiResponse.success("Ubicación actualizada", config));
    }
    
    /**
     * Subir logo de la empresa
     */
    @PostMapping(value = "/{empresaId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EmpresaConfigResponse>> subirLogo(
            @PathVariable Long empresaId,
            @RequestParam("file") MultipartFile file) {
        EmpresaConfigResponse config = empresaConfigUseCase.subirLogo(empresaId, file);
        return ResponseEntity.ok(ApiResponse.success("Logo subido exitosamente", config));
    }
    
    /**
     * Subir banner de la empresa
     */
    @PostMapping(value = "/{empresaId}/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EmpresaConfigResponse>> subirBanner(
            @PathVariable Long empresaId,
            @RequestParam("file") MultipartFile file) {
        EmpresaConfigResponse config = empresaConfigUseCase.subirBanner(empresaId, file);
        return ResponseEntity.ok(ApiResponse.success("Banner subido exitosamente", config));
    }
    
    /**
     * Eliminar logo de la empresa
     */
    @DeleteMapping("/{empresaId}/logo")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Object>> eliminarLogo(@PathVariable Long empresaId) {
        empresaConfigUseCase.eliminarLogo(empresaId);
        return ResponseEntity.ok(ApiResponse.success("Logo eliminado", null));
    }
    
    /**
     * Eliminar banner de la empresa
     */
    @DeleteMapping("/{empresaId}/banner")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Object>> eliminarBanner(@PathVariable Long empresaId) {
        empresaConfigUseCase.eliminarBanner(empresaId);
        return ResponseEntity.ok(ApiResponse.success("Banner eliminado", null));
    }
}
