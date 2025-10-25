package apex.code.clipperBarberShop.servicio.adapters.in.web;

import apex.code.clipperBarberShop.servicio.application.dto.ActualizarServicioRequest;
import apex.code.clipperBarberShop.servicio.application.dto.ServicioRequest;
import apex.code.clipperBarberShop.servicio.application.dto.ServicioResponse;
import apex.code.clipperBarberShop.servicio.application.service.ServicioOwnerService;
import apex.code.clipperBarberShop.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Controlador REST para gestión de servicios por parte del OWNER
 */
@RestController
@RequestMapping("/api/owner/servicios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class ServicioOwnerController {
    
    private final ServicioOwnerService servicioOwnerService;
    
    /**
     * Crea un nuevo servicio
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ServicioResponse>> crearServicio(
            @Valid @RequestBody ServicioRequest request,
            Principal principal) {
        ServicioResponse servicio = servicioOwnerService.crearServicio(request, principal.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Servicio creado exitosamente", servicio));
    }
    
    /**
     * Actualiza un servicio existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioResponse>> actualizarServicio(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarServicioRequest request,
            Principal principal) {
        ServicioResponse servicio = servicioOwnerService.actualizarServicio(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Servicio actualizado exitosamente", servicio));
    }
    
    /**
     * Obtiene un servicio por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioResponse>> obtenerServicio(
            @PathVariable Long id,
            Principal principal) {
        ServicioResponse servicio = servicioOwnerService.obtenerServicio(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Servicio obtenido", servicio));
    }
    
    /**
     * Lista todos los servicios de la empresa del owner
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicioResponse>>> listarServicios(Principal principal) {
        List<ServicioResponse> servicios = servicioOwnerService.listarServiciosPorEmpresa(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Servicios obtenidos", servicios));
    }
    
    /**
     * Lista servicios paginados
     */
    @GetMapping("/paginados")
    public ResponseEntity<ApiResponse<Page<ServicioResponse>>> listarServiciosPaginados(
            Pageable pageable,
            Principal principal) {
        Page<ServicioResponse> servicios = servicioOwnerService.listarServiciosPaginados(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Servicios obtenidos", servicios));
    }
    
    /**
     * Lista servicios por categoría
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<ApiResponse<List<ServicioResponse>>> listarServiciosPorCategoria(
            @PathVariable String categoria,
            Principal principal) {
        List<ServicioResponse> servicios = servicioOwnerService.listarServiciosPorCategoria(categoria, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Servicios obtenidos", servicios));
    }
    
    /**
     * Elimina un servicio (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarServicio(
            @PathVariable Long id,
            Principal principal) {
        servicioOwnerService.eliminarServicio(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Servicio eliminado exitosamente", null));
    }
    
    /**
     * Restaura un servicio eliminado
     */
    @PatchMapping("/{id}/restaurar")
    public ResponseEntity<ApiResponse<ServicioResponse>> restaurarServicio(
            @PathVariable Long id,
            Principal principal) {
        ServicioResponse servicio = servicioOwnerService.restaurarServicio(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Servicio restaurado exitosamente", servicio));
    }
}
