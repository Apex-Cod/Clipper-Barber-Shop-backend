package apex.code.clipperBarberShop.resenia.adapters.in.web;

import apex.code.clipperBarberShop.resenia.application.dto.EstadisticasEmpresaResponse;
import apex.code.clipperBarberShop.resenia.application.dto.ReseniaResponse;
import apex.code.clipperBarberShop.resenia.application.service.ReseniaPublicService;
import apex.code.clipperBarberShop.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST PÚBLICO para consultar reseñas (sin autenticación)
 */
@RestController
@RequestMapping("/api/public/empresas")
@RequiredArgsConstructor
public class ReseniaPublicController {
    
    private final ReseniaPublicService reseniaPublicService;
    
    /**
     * Lista todas las reseñas activas de una empresa
     */
    @GetMapping("/{empresaId}/resenias")
    public ResponseEntity<ApiResponse<List<ReseniaResponse>>> listarReseniasPorEmpresa(
            @PathVariable Long empresaId) {
        List<ReseniaResponse> resenias = reseniaPublicService.listarReseniasPorEmpresa(empresaId);
        return ResponseEntity.ok(ApiResponse.success("Reseñas obtenidas", resenias));
    }
    
    /**
     * Lista reseñas paginadas de una empresa
     */
    @GetMapping("/{empresaId}/resenias/paginadas")
    public ResponseEntity<ApiResponse<Page<ReseniaResponse>>> listarReseniasPaginadas(
            @PathVariable Long empresaId,
            Pageable pageable) {
        Page<ReseniaResponse> resenias = reseniaPublicService.listarReseniasPaginadas(empresaId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Reseñas obtenidas", resenias));
    }
    
    /**
     * Obtiene estadísticas de reseñas de una empresa
     */
    @GetMapping("/{empresaId}/resenias/estadisticas")
    public ResponseEntity<ApiResponse<EstadisticasEmpresaResponse>> obtenerEstadisticas(
            @PathVariable Long empresaId) {
        EstadisticasEmpresaResponse estadisticas = reseniaPublicService.obtenerEstadisticasEmpresa(empresaId);
        return ResponseEntity.ok(ApiResponse.success("Estadísticas obtenidas", estadisticas));
    }
    
    /**
     * Lista reseñas de un empleado específico
     */
    @GetMapping("/empleados/{empleadoId}/resenias")
    public ResponseEntity<ApiResponse<List<ReseniaResponse>>> listarReseniasPorEmpleado(
            @PathVariable String empleadoId) {
        List<ReseniaResponse> resenias = reseniaPublicService.listarReseniasPorEmpleado(empleadoId);
        return ResponseEntity.ok(ApiResponse.success("Reseñas obtenidas", resenias));
    }
}
