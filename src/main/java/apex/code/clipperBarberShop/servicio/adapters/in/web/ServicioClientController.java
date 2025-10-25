package apex.code.clipperBarberShop.servicio.adapters.in.web;

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
 * Controlador REST para consulta de servicios por parte de CLIENTES
 * Solo tiene endpoints de lectura
 */
@RestController
@RequestMapping("/api/client/servicios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ServicioClientController {
    
    private final ServicioClientService servicioClientService;
    
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
