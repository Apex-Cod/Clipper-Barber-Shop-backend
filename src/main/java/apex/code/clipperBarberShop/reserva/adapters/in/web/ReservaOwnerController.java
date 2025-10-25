package apex.code.clipperBarberShop.reserva.adapters.in.web;

import apex.code.clipperBarberShop.reserva.application.dto.*;
import apex.code.clipperBarberShop.reserva.application.service.ReservaOwnerService;
import apex.code.clipperBarberShop.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para gestión de reservas por parte del OWNER
 * Puede gestionar todas las reservas de su empresa
 */
@RestController
@RequestMapping("/api/owner/reservas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class ReservaOwnerController {
    
    private final ReservaOwnerService reservaOwnerService;
    
    /**
     * Crea una nueva reserva para un cliente
     */
    @PostMapping("/cliente/{clientId}")
    public ResponseEntity<ApiResponse<ReservaResponse>> crearReserva(
            @PathVariable String clientId,
            @Valid @RequestBody CrearReservaRequest request,
            Principal principal) {
        ReservaResponse reserva = reservaOwnerService.crearReserva(request, clientId, principal.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reserva creada exitosamente", reserva));
    }
    
    /**
     * Actualiza una reserva existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservaResponse>> actualizarReserva(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarReservaRequest request,
            Principal principal) {
        ReservaResponse reserva = reservaOwnerService.actualizarReserva(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva actualizada exitosamente", reserva));
    }
    
    /**
     * Confirma una reserva
     */
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<ApiResponse<ReservaResponse>> confirmarReserva(
            @PathVariable Long id,
            Principal principal) {
        ReservaResponse reserva = reservaOwnerService.confirmarReserva(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva confirmada exitosamente", reserva));
    }
    
    /**
     * Marca una reserva como completada
     */
    @PatchMapping("/{id}/completar")
    public ResponseEntity<ApiResponse<ReservaResponse>> completarReserva(
            @PathVariable Long id,
            Principal principal) {
        ReservaResponse reserva = reservaOwnerService.completarReserva(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva completada exitosamente", reserva));
    }
    
    /**
     * Cancela una reserva
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<Void>> cancelarReserva(
            @PathVariable Long id,
            @RequestBody(required = false) CancelarReservaRequest request,
            Principal principal) {
        if (request == null) {
            request = new CancelarReservaRequest();
        }
        reservaOwnerService.cancelarReserva(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva cancelada exitosamente", null));
    }
    
    /**
     * Reprograma una reserva
     */
    @PatchMapping("/{id}/reprogramar")
    public ResponseEntity<ApiResponse<ReservaResponse>> reprogramarReserva(
            @PathVariable Long id,
            @Valid @RequestBody ReprogramarReservaRequest request,
            Principal principal) {
        ReservaResponse reserva = reservaOwnerService.reprogramarReserva(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva reprogramada exitosamente", reserva));
    }
    
    /**
     * Obtiene los detalles de una reserva
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservaDetalleResponse>> obtenerReserva(
            @PathVariable Long id,
            Principal principal) {
        ReservaDetalleResponse reserva = reservaOwnerService.obtenerReserva(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva obtenida", reserva));
    }
    
    /**
     * Lista todas las reservas de la empresa
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservaResponse>>> listarReservas(Principal principal) {
        List<ReservaResponse> reservas = reservaOwnerService.listarReservasPorEmpresa(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas", reservas));
    }
    
    /**
     * Lista reservas paginadas
     */
    @GetMapping("/paginadas")
    public ResponseEntity<ApiResponse<Page<ReservaResponse>>> listarReservasPaginadas(
            Pageable pageable,
            Principal principal) {
        Page<ReservaResponse> reservas = reservaOwnerService.listarReservasPaginadas(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas", reservas));
    }
    
    /**
     * Lista reservas por estado
     */
    @GetMapping("/estado/{status}")
    public ResponseEntity<ApiResponse<List<ReservaResponse>>> listarReservasPorEstado(
            @PathVariable String status,
            Principal principal) {
        List<ReservaResponse> reservas = reservaOwnerService.listarReservasPorEstado(status, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas", reservas));
    }
    
    /**
     * Lista reservas de un empleado específico
     */
    @GetMapping("/empleado/{employeeId}")
    public ResponseEntity<ApiResponse<List<ReservaResponse>>> listarReservasPorEmpleado(
            @PathVariable String employeeId,
            Principal principal) {
        List<ReservaResponse> reservas = reservaOwnerService.listarReservasPorEmpleado(employeeId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas", reservas));
    }
    
    /**
     * Lista reservas en un rango de fechas
     */
    @GetMapping("/rango")
    public ResponseEntity<ApiResponse<List<ReservaResponse>>> listarReservasPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            Principal principal) {
        List<ReservaResponse> reservas = reservaOwnerService.listarReservasPorRangoFechas(inicio, fin, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas", reservas));
    }
    
    /**
     * Elimina una reserva (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarReserva(
            @PathVariable Long id,
            Principal principal) {
        reservaOwnerService.eliminarReserva(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva eliminada exitosamente", null));
    }
}
