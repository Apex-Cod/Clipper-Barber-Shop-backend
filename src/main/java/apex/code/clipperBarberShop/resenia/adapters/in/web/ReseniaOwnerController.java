package apex.code.clipperBarberShop.resenia.adapters.in.web;

import apex.code.clipperBarberShop.resenia.application.dto.EstadisticasEmpleadoResponse;
import apex.code.clipperBarberShop.resenia.application.dto.EstadisticasEmpresaResponse;
import apex.code.clipperBarberShop.resenia.application.dto.ReseniaResponse;
import apex.code.clipperBarberShop.resenia.application.service.ReseniaOwnerService;
import apex.code.clipperBarberShop.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Controlador REST para gestión de reseñas por parte del OWNER
 */
@RestController
@RequestMapping("/api/owner/resenias")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class ReseniaOwnerController {
    
    private final ReseniaOwnerService reseniaOwnerService;
    
    /**
     * Obtiene los detalles de una reseña
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReseniaResponse>> obtenerResenia(
            @PathVariable Long id,
            Principal principal) {
        ReseniaResponse resenia = reseniaOwnerService.obtenerResenia(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reseña obtenida", resenia));
    }
    
    /**
     * Lista todas las reseñas de la empresa
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReseniaResponse>>> listarResenias(Principal principal) {
        List<ReseniaResponse> resenias = reseniaOwnerService.listarReseniasPorEmpresa(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reseñas obtenidas", resenias));
    }
    
    /**
     * Lista reseñas paginadas
     */
    @GetMapping("/paginadas")
    public ResponseEntity<ApiResponse<Page<ReseniaResponse>>> listarReseniasPaginadas(
            Pageable pageable,
            Principal principal) {
        Page<ReseniaResponse> resenias = reseniaOwnerService.listarReseniasPaginadas(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Reseñas obtenidas", resenias));
    }
    
    /**
     * Lista reseñas de un empleado específico
     */
    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<ApiResponse<List<ReseniaResponse>>> listarReseniasPorEmpleado(
            @PathVariable String empleadoId,
            Principal principal) {
        List<ReseniaResponse> resenias = reseniaOwnerService.listarReseniasPorEmpleado(empleadoId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reseñas obtenidas", resenias));
    }
    
    /**
     * Obtiene estadísticas de la empresa
     */
    @GetMapping("/estadisticas/empresa")
    public ResponseEntity<ApiResponse<EstadisticasEmpresaResponse>> obtenerEstadisticasEmpresa(Principal principal) {
        EstadisticasEmpresaResponse estadisticas = reseniaOwnerService.obtenerEstadisticasEmpresa(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Estadísticas obtenidas", estadisticas));
    }
    
    /**
     * Obtiene estadísticas de un empleado
     */
    @GetMapping("/estadisticas/empleado/{empleadoId}")
    public ResponseEntity<ApiResponse<EstadisticasEmpleadoResponse>> obtenerEstadisticasEmpleado(
            @PathVariable String empleadoId,
            Principal principal) {
        EstadisticasEmpleadoResponse estadisticas = reseniaOwnerService.obtenerEstadisticasEmpleado(empleadoId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Estadísticas obtenidas", estadisticas));
    }
    
    /**
     * Elimina una reseña (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarResenia(
            @PathVariable Long id,
            Principal principal) {
        reseniaOwnerService.eliminarResenia(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reseña eliminada exitosamente", null));
    }
}
