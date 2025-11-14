package apex.code.clipperBarberShop.reserva.adapters.in.web;

import apex.code.clipperBarberShop.reserva.application.dto.*;
import apex.code.clipperBarberShop.reserva.application.service.ReservaClientService;
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
 * Controlador REST para gestión de reservas por parte de los CLIENTES
 * Solo puede gestionar sus propias reservas (CRUD completo)
 */
@RestController
@RequestMapping("/api/client/reservas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ReservaClientController {

    private final ReservaClientService reservaClientService;

    /**
     * Crea una nueva reserva
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReservaResponse>> crearReserva(
            @Valid @RequestBody CrearReservaRequest request,
            Principal principal) {
        ReservaResponse reserva = reservaClientService.crearReserva(request, principal.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reserva creada exitosamente", reserva));
    }

    /**
     * Actualiza una reserva propia
     * Solo si está en estado PENDING
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservaResponse>> actualizarReserva(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarReservaRequest request,
            Principal principal) {
        ReservaResponse reserva = reservaClientService.actualizarReserva(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva actualizada exitosamente", reserva));
    }

    /**
     * Cancela una reserva propia
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<Void>> cancelarReserva(
            @PathVariable Long id,
            @RequestBody(required = false) CancelarReservaRequest request,
            Principal principal) {
        if (request == null) {
            request = new CancelarReservaRequest();
        }
        reservaClientService.cancelarReserva(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva cancelada exitosamente", null));
    }

    /**
     * Reprograma una reserva propia
     * Solo si está en estado PENDING
     */
    @PatchMapping("/{id}/reprogramar")
    public ResponseEntity<ApiResponse<ReservaResponse>> reprogramarReserva(
            @PathVariable Long id,
            @Valid @RequestBody ReprogramarReservaRequest request,
            Principal principal) {
        ReservaResponse reserva = reservaClientService.reprogramarReserva(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva reprogramada exitosamente", reserva));
    }

    /**
     * Obtiene los detalles de una reserva propia
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservaDetalleResponse>> obtenerReserva(
            @PathVariable Long id,
            Principal principal) {
        ReservaDetalleResponse reserva = reservaClientService.obtenerReserva(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva obtenida", reserva));
    }

    /**
     * Lista todas las reservas propias
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservaResponse>>> listarMisReservas(Principal principal) {
        List<ReservaResponse> reservas = reservaClientService.listarMisReservas(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas", reservas));
    }

    /**
     * Lista reservas propias paginadas
     */
    @GetMapping("/paginadas")
    public ResponseEntity<ApiResponse<Page<ReservaResponse>>> listarMisReservasPaginadas(
            Pageable pageable,
            Principal principal) {
        Page<ReservaResponse> reservas = reservaClientService.listarMisReservasPaginadas(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas", reservas));
    }

    /**
     * Lista reservas propias por estado
     */
    @GetMapping("/estado/{status}")
    public ResponseEntity<ApiResponse<List<ReservaResponse>>> listarMisReservasPorEstado(
            @PathVariable String status,
            Principal principal) {
        List<ReservaResponse> reservas = reservaClientService.listarMisReservasPorEstado(status, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas", reservas));
    }

    /**
     * Elimina una reserva propia (soft delete)
     * Solo si está cancelada o completada
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarReserva(
            @PathVariable Long id,
            Principal principal) {
        reservaClientService.eliminarReserva(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva eliminada exitosamente", null));
    }

    /**
     * Obtiene los horarios ocupados para una fecha y empresa específica
     * Útil para mostrar disponibilidad en el frontend
     */
    @GetMapping("/occupied-slots")
    public ResponseEntity<ApiResponse<List<String>>> obtenerHorariosOcupados(
            @RequestParam String empresaId,
            @RequestParam String fecha, // Formato: YYYY-MM-DD
            @RequestParam(required = false) String empleadoId) {
        List<String> horariosOcupados = reservaClientService.obtenerHorariosOcupados(empresaId, fecha, empleadoId);
        return ResponseEntity.ok(ApiResponse.success("Horarios ocupados obtenidos", horariosOcupados));
    }
}
