package apex.code.clipperBarberShop.reserva.adapters.in.web;

import apex.code.clipperBarberShop.reserva.application.dto.*;
import apex.code.clipperBarberShop.reserva.application.service.ReservaEmployeeService;
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
 * Controlador REST para gestión de reservas por parte de los EMPLEADOS
 * Los empleados pueden ver y gestionar las reservas asignadas a ellos
 */
@RestController
@RequestMapping("/api/employee/reservas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class ReservaEmployeeController {

    private final ReservaEmployeeService reservaEmployeeService;

    /**
     * Lista todas las reservas asignadas al empleado autenticado
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservaResponse>>> listarMisReservas(Principal principal) {
        List<ReservaResponse> reservas = reservaEmployeeService.listarMisReservas(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas", reservas));
    }

    /**
     * Lista reservas del empleado paginadas
     */
    @GetMapping("/paginadas")
    public ResponseEntity<ApiResponse<Page<ReservaResponse>>> listarMisReservasPaginadas(
            Pageable pageable,
            Principal principal) {
        Page<ReservaResponse> reservas = reservaEmployeeService.listarMisReservasPaginadas(principal.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas", reservas));
    }

    /**
     * Lista reservas del empleado por estado
     */
    @GetMapping("/estado/{status}")
    public ResponseEntity<ApiResponse<List<ReservaResponse>>> listarMisReservasPorEstado(
            @PathVariable String status,
            Principal principal) {
        List<ReservaResponse> reservas = reservaEmployeeService.listarMisReservasPorEstado(status, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas", reservas));
    }

    /**
     * Lista reservas del empleado por fecha específica
     */
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<ApiResponse<List<ReservaResponse>>> listarReservasPorFecha(
            @PathVariable String fecha, // Formato: YYYY-MM-DD
            Principal principal) {
        List<ReservaResponse> reservas = reservaEmployeeService.listarReservasPorFecha(principal.getName(), fecha);
        return ResponseEntity.ok(ApiResponse.success("Reservas del día obtenidas", reservas));
    }

    /**
     * Obtiene los detalles de una reserva específica asignada al empleado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservaDetalleResponse>> obtenerReserva(
            @PathVariable Long id,
            Principal principal) {
        ReservaDetalleResponse reserva = reservaEmployeeService.obtenerReserva(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva obtenida", reserva));
    }

    /**
     * Confirma una reserva (cambia de PENDING a CONFIRMED)
     */
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<ApiResponse<ReservaResponse>> confirmarReserva(
            @PathVariable Long id,
            Principal principal) {
        ReservaResponse reserva = reservaEmployeeService.confirmarReserva(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva confirmada exitosamente", reserva));
    }

    /**
     * Completa una reserva (cambia de CONFIRMED a COMPLETED)
     */
    @PatchMapping("/{id}/completar")
    public ResponseEntity<ApiResponse<ReservaResponse>> completarReserva(
            @PathVariable Long id,
            Principal principal) {
        ReservaResponse reserva = reservaEmployeeService.completarReserva(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva completada exitosamente", reserva));
    }

    /**
     * Cancela una reserva desde el punto de vista del empleado
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<ReservaResponse>> cancelarReserva(
            @PathVariable Long id,
            @RequestBody(required = false) CancelarReservaRequest request,
            Principal principal) {
        if (request == null) {
            request = new CancelarReservaRequest();
        }
        ReservaResponse reserva = reservaEmployeeService.cancelarReserva(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Reserva cancelada exitosamente", reserva));
    }

    /**
     * Obtiene el horario de trabajo del empleado para una fecha específica
     * Solo muestra las reservas activas (PENDING y CONFIRMED)
     */
    @GetMapping("/mi-horario")
    public ResponseEntity<ApiResponse<List<String>>> obtenerMiHorario(
            @RequestParam String fecha, // Formato: YYYY-MM-DD
            Principal principal) {
        List<String> horarios = reservaEmployeeService.obtenerMiHorario(principal.getName(), fecha);
        return ResponseEntity.ok(ApiResponse.success("Horario obtenido", horarios));
    }

    /**
     * Endpoint de resumen para el dashboard del empleado
     * Retorna estadísticas rápidas de las reservas del día
     */
    @GetMapping("/dashboard/hoy")
    public ResponseEntity<ApiResponse<DashboardEmpleadoResponse>> dashboardHoy(Principal principal) {
        String fechaHoy = java.time.LocalDate.now().toString();
        
        List<ReservaResponse> reservasHoy = reservaEmployeeService.listarReservasPorFecha(principal.getName(), fechaHoy);
        
        long pendientes = reservasHoy.stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        long confirmadas = reservasHoy.stream().filter(r -> "CONFIRMED".equals(r.getStatus())).count();
        long completadas = reservasHoy.stream().filter(r -> "COMPLETED".equals(r.getStatus())).count();
        long canceladas = reservasHoy.stream().filter(r -> "CANCELLED".equals(r.getStatus())).count();
        
        DashboardEmpleadoResponse dashboard = DashboardEmpleadoResponse.builder()
                .fecha(fechaHoy)
                .totalReservas((int) reservasHoy.size())
                .reservasPendientes((int) pendientes)
                .reservasConfirmadas((int) confirmadas)
                .reservasCompletadas((int) completadas)
                .reservasCanceladas((int) canceladas)
                .proximasReservas(reservasHoy.stream()
                    .filter(r -> "PENDING".equals(r.getStatus()) || "CONFIRMED".equals(r.getStatus()))
                    .sorted((r1, r2) -> r1.getReservationDate().compareTo(r2.getReservationDate()))
                    .limit(5)
                    .collect(java.util.stream.Collectors.toList()))
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("Dashboard obtenido", dashboard));
    }
}
