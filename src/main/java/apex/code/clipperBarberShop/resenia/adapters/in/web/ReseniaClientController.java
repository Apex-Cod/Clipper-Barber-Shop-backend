package apex.code.clipperBarberShop.resenia.adapters.in.web;

import apex.code.clipperBarberShop.resenia.application.dto.ActualizarReseniaRequest;
import apex.code.clipperBarberShop.resenia.application.dto.CrearReseniaRequest;
import apex.code.clipperBarberShop.resenia.application.dto.ReseniaResponse;
import apex.code.clipperBarberShop.resenia.application.service.ReseniaClientService;
import apex.code.clipperBarberShop.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Controlador REST para gestión de reseñas por parte de los CLIENTES
 */
@RestController
@RequestMapping("/api/client/resenias")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ReseniaClientController {
    
    private final ReseniaClientService reseniaClientService;
    
    /**
     * Crea una nueva reseña para una reserva completada
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReseniaResponse>> crearResenia(
            @Valid @RequestBody CrearReseniaRequest request,
            Principal principal) {
        ReseniaResponse resenia = reseniaClientService.crearResenia(request, principal.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reseña creada exitosamente", resenia));
    }
    
    /**
     * Actualiza una reseña propia
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReseniaResponse>> actualizarResenia(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarReseniaRequest request,
            Principal principal) {
        ReseniaResponse resenia = reseniaClientService.actualizarResenia(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reseña actualizada exitosamente", resenia));
    }
    
    /**
     * Obtiene los detalles de una reseña propia
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReseniaResponse>> obtenerResenia(
            @PathVariable Long id,
            Principal principal) {
        ReseniaResponse resenia = reseniaClientService.obtenerResenia(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reseña obtenida", resenia));
    }
    
    /**
     * Lista todas las reseñas propias
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReseniaResponse>>> listarMisResenias(Principal principal) {
        List<ReseniaResponse> resenias = reseniaClientService.listarMisResenias(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reseñas obtenidas", resenias));
    }
    
    /**
     * Obtiene la reseña de una reserva específica
     */
    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<ApiResponse<ReseniaResponse>> obtenerReseniaPorReserva(
            @PathVariable Long reservaId,
            Principal principal) {
        ReseniaResponse resenia = reseniaClientService.obtenerReseniaPorReserva(reservaId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reseña obtenida", resenia));
    }
    
    /**
     * Elimina una reseña propia (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarResenia(
            @PathVariable Long id,
            Principal principal) {
        reseniaClientService.eliminarResenia(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reseña eliminada exitosamente", null));
    }
}
