package apex.code.clipperBarberShop.servicio.adapters.in.web;

import apex.code.clipperBarberShop.register.application.dto.EmpresaResponse;
import apex.code.clipperBarberShop.register.application.service.EmpresaClientService;
import apex.code.clipperBarberShop.servicio.application.dto.ServicioResponse;
import apex.code.clipperBarberShop.servicio.application.service.ServicioClientService;
import apex.code.clipperBarberShop.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para consulta de servicios y empresas por parte de CLIENTES
 * Solo tiene endpoints de lectura
 */
@RestController
@RequestMapping("/api/client/servicios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ServicioClientController {
    
    private final ServicioClientService servicioClientService;
    private final EmpresaClientService empresaClientService;
    
    // ==================== ENDPOINTS DE EMPRESAS ====================
    
    /**
     * Lista todas las empresas activas
     */
    @GetMapping("/empresas")
    public ResponseEntity<ApiResponse<List<EmpresaResponse>>> listarEmpresas() {
        List<EmpresaResponse> empresas = empresaClientService.listarEmpresas();
        return ResponseEntity.ok(ApiResponse.success("Empresas obtenidas", empresas));
    }
    
    /**
     * Lista todas las empresas activas con paginación
     */
    @GetMapping("/empresas/paginadas")
    public ResponseEntity<ApiResponse<Page<EmpresaResponse>>> listarEmpresasPaginadas(Pageable pageable) {
        Page<EmpresaResponse> empresas = empresaClientService.listarEmpresasPaginadas(pageable);
        return ResponseEntity.ok(ApiResponse.success("Empresas obtenidas", empresas));
    }
    
    /**
     * Obtiene una empresa por su ID
     */
    @GetMapping("/empresas/{id}")
    public ResponseEntity<ApiResponse<EmpresaResponse>> obtenerEmpresa(@PathVariable Long id) {
        EmpresaResponse empresa = empresaClientService.obtenerEmpresaPorId(id);
        return ResponseEntity.ok(ApiResponse.success("Empresa obtenida", empresa));
    }
    
    // ==================== ENDPOINTS DE SERVICIOS ====================
    
    /**
     * Obtiene un servicio por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioResponse>> obtenerServicio(@PathVariable Long id) {
        ServicioResponse servicio = servicioClientService.obtenerServicio(id);
        return ResponseEntity.ok(ApiResponse.success("Servicio obtenido", servicio));
    }
    
    /**
     * Lista todos los servicios activos de una empresa
     */
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<ApiResponse<List<ServicioResponse>>> listarServiciosPorEmpresa(
            @PathVariable Long empresaId) {
        List<ServicioResponse> servicios = servicioClientService.listarServiciosPorEmpresa(empresaId);
        return ResponseEntity.ok(ApiResponse.success("Servicios obtenidos", servicios));
    }
    
    /**
     * Lista servicios paginados de una empresa
     */
    @GetMapping("/empresa/{empresaId}/paginados")
    public ResponseEntity<ApiResponse<Page<ServicioResponse>>> listarServiciosPaginados(
            @PathVariable Long empresaId,
            Pageable pageable) {
        Page<ServicioResponse> servicios = servicioClientService.listarServiciosPaginados(empresaId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Servicios obtenidos", servicios));
    }
    
    /**
     * Lista servicios de una empresa filtrados por categoría
     */
    @GetMapping("/empresa/{empresaId}/categoria/{categoria}")
    public ResponseEntity<ApiResponse<List<ServicioResponse>>> listarServiciosPorCategoria(
            @PathVariable Long empresaId,
            @PathVariable String categoria) {
        List<ServicioResponse> servicios = servicioClientService.listarServiciosPorCategoria(empresaId, categoria);
        return ResponseEntity.ok(ApiResponse.success("Servicios obtenidos", servicios));
    }
}
