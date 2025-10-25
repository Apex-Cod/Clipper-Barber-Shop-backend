package apex.code.clipperBarberShop.promocion.adapters.in.web;

import apex.code.clipperBarberShop.promocion.application.dto.ActualizarPromocionRequest;
import apex.code.clipperBarberShop.promocion.application.dto.PromocionRequest;
import apex.code.clipperBarberShop.promocion.application.dto.PromocionResponse;
import apex.code.clipperBarberShop.promocion.application.service.PromocionOwnerService;
import apex.code.clipperBarberShop.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para promociones (solo OWNER)
 */
@RestController
@RequestMapping("/api/owner/promociones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class PromocionOwnerController {
    
    private final PromocionOwnerService promocionService;
    
    /**
     * Crea una nueva promoción
     * POST /api/owner/promociones
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PromocionResponse>> crearPromocion(
            @Valid @RequestBody PromocionRequest request,
            Authentication authentication) {
        
        String userId = authentication.getName();
        PromocionResponse response = promocionService.crearPromocion(userId, request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Promoción creada exitosamente",
                        response
                ));
    }
    
    /**
     * Actualiza una promoción existente
     * PUT /api/owner/promociones/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PromocionResponse>> actualizarPromocion(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPromocionRequest request,
            Authentication authentication) {
        
        String userId = authentication.getName();
        PromocionResponse response = promocionService.actualizarPromocion(userId, id, request);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Promoción actualizada exitosamente",
                        response
                ));
    }
    
    /**
     * Obtiene una promoción por ID
     * GET /api/owner/promociones/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromocionResponse>> obtenerPromocion(
            @PathVariable Long id,
            Authentication authentication) {
        
        String userId = authentication.getName();
        PromocionResponse response = promocionService.obtenerPromocion(userId, id);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Promoción obtenida exitosamente",
                        response
                ));
    }
    
    /**
     * Lista todas las promociones de la empresa
     * GET /api/owner/promociones
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PromocionResponse>>> listarPromociones(
            Authentication authentication) {
        
        String userId = authentication.getName();
        List<PromocionResponse> response = promocionService.listarPromociones(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Promociones obtenidas exitosamente",
                        response
                ));
    }
    
    /**
     * Lista promociones con paginación
     * GET /api/owner/promociones/paginadas?page=0&size=10&sort=nombre
     */
    @GetMapping("/paginadas")
    public ResponseEntity<ApiResponse<Page<PromocionResponse>>> listarPromocionesPaginadas(
            @PageableDefault(size = 10) Pageable pageable,
            Authentication authentication) {
        
        String userId = authentication.getName();
        Page<PromocionResponse> response = promocionService.listarPromocionesPaginadas(userId, pageable);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Promociones paginadas obtenidas exitosamente",
                        response
                ));
    }
    
    /**
     * Lista solo promociones activas
     * GET /api/owner/promociones/activas
     */
    @GetMapping("/activas")
    public ResponseEntity<ApiResponse<List<PromocionResponse>>> listarPromocionesActivas(
            Authentication authentication) {
        
        String userId = authentication.getName();
        List<PromocionResponse> response = promocionService.listarPromocionesActivas(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Promociones activas obtenidas exitosamente",
                        response
                ));
    }
    
    /**
     * Activa una promoción
     * PATCH /api/owner/promociones/{id}/activar
     */
    @PatchMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<PromocionResponse>> activarPromocion(
            @PathVariable Long id,
            Authentication authentication) {
        
        String userId = authentication.getName();
        PromocionResponse response = promocionService.cambiarEstadoPromocion(userId, id, true);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Promoción activada exitosamente",
                        response
                ));
    }
    
    /**
     * Desactiva una promoción
     * PATCH /api/owner/promociones/{id}/desactivar
     */
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<PromocionResponse>> desactivarPromocion(
            @PathVariable Long id,
            Authentication authentication) {
        
        String userId = authentication.getName();
        PromocionResponse response = promocionService.cambiarEstadoPromocion(userId, id, false);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Promoción desactivada exitosamente",
                        response
                ));
    }
    
    /**
     * Elimina una promoción (soft delete)
     * DELETE /api/owner/promociones/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarPromocion(
            @PathVariable Long id,
            Authentication authentication) {
        
        String userId = authentication.getName();
        promocionService.eliminarPromocion(userId, id);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Promoción eliminada exitosamente",
                        null
                ));
    }
}
