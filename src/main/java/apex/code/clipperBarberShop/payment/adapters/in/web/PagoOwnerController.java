package apex.code.clipperBarberShop.payment.adapters.in.web;

import apex.code.clipperBarberShop.Entities.enums.EstadoPago;
import apex.code.clipperBarberShop.payment.application.dto.PagoResponse;
import apex.code.clipperBarberShop.payment.application.service.PagoOwnerService;
import apex.code.clipperBarberShop.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de pagos por parte del OWNER
 */
@RestController
@RequestMapping("/api/owner/pagos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class PagoOwnerController {
    
    private final PagoOwnerService pagoOwnerService;
    
    /**
     * Obtiene los detalles de un pago
     * GET /api/owner/pagos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PagoResponse>> obtenerPago(
            @PathVariable Long id,
            Principal principal) {
        PagoResponse pago = pagoOwnerService.obtenerPago(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Pago obtenido", pago));
    }
    
    /**
     * Lista todos los pagos de la empresa
     * GET /api/owner/pagos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listarPagos(Principal principal) {
        List<PagoResponse> pagos = pagoOwnerService.listarPagosPorEmpresa(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Pagos obtenidos", pagos));
    }
    
    /**
     * Lista pagos paginados
     * GET /api/owner/pagos/paginados
     */
    @GetMapping("/paginados")
    public ResponseEntity<ApiResponse<Page<PagoResponse>>> listarPagosPaginados(
            Pageable pageable,
            Principal principal) {
        Page<PagoResponse> pagos = pagoOwnerService.listarPagosPaginados(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Pagos obtenidos", pagos));
    }
    
    /**
     * Lista pagos por estado
     * GET /api/owner/pagos/estado/{estado}
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listarPagosPorEstado(
            @PathVariable EstadoPago estado,
            Principal principal) {
        List<PagoResponse> pagos = pagoOwnerService.listarPagosPorEstado(principal.getName(), estado);
        return ResponseEntity.ok(ApiResponse.success("Pagos obtenidos", pagos));
    }
    
    /**
     * Obtiene estadísticas de pagos de la empresa
     * GET /api/owner/pagos/estadisticas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> obtenerEstadisticas(Principal principal) {
        Map<String, Object> estadisticas = pagoOwnerService.obtenerEstadisticasPagos(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Estadísticas obtenidas", estadisticas));
    }
    
    /**
     * Elimina un pago (soft delete)
     * DELETE /api/owner/pagos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarPago(
            @PathVariable Long id,
            Principal principal) {
        pagoOwnerService.eliminarPago(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Pago eliminado exitosamente", null));
    }
}
