package apex.code.clipperBarberShop.reporte.adapters.in.web;

import apex.code.clipperBarberShop.reporte.application.dto.*;
import apex.code.clipperBarberShop.reporte.domain.port.in.ReporteUseCase;
import apex.code.clipperBarberShop.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

/**
 * Controlador REST para reportes
 * Solo disponible para OWNERS
 */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class ReporteController {
    
    private final ReporteUseCase reporteUseCase;
    
    /**
     * Obtener reporte de ingresos
     * 
     * GET /api/reportes/ingresos?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-01-31
     */
    @GetMapping("/ingresos")
    public ResponseEntity<ApiResponse<ReporteIngresosResponse>> obtenerReporteIngresos(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Principal principal
    ) {
        ReporteIngresosResponse reporte = reporteUseCase.obtenerReporteIngresos(
                empresaId, fechaInicio, fechaFin, principal.getName()
        );
        return ResponseEntity.ok(ApiResponse.success("Reporte de ingresos generado", reporte));
    }
    
    /**
     * Obtener reporte de servicios más solicitados
     * 
     * GET /api/reportes/servicios?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-01-31
     */
    @GetMapping("/servicios")
    public ResponseEntity<ApiResponse<ReporteServiciosResponse>> obtenerReporteServicios(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Principal principal
    ) {
        ReporteServiciosResponse reporte = reporteUseCase.obtenerReporteServicios(
                empresaId, fechaInicio, fechaFin, principal.getName()
        );
        return ResponseEntity.ok(ApiResponse.success("Reporte de servicios generado", reporte));
    }
    
    /**
     * Obtener reporte de rendimiento de empleados
     * 
     * GET /api/reportes/empleados?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-01-31
     */
    @GetMapping("/empleados")
    public ResponseEntity<ApiResponse<ReporteEmpleadosResponse>> obtenerReporteEmpleados(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Principal principal
    ) {
        ReporteEmpleadosResponse reporte = reporteUseCase.obtenerReporteEmpleados(
                empresaId, fechaInicio, fechaFin, principal.getName()
        );
        return ResponseEntity.ok(ApiResponse.success("Reporte de empleados generado", reporte));
    }
    
    /**
     * Obtener reporte de clientes frecuentes
     * 
     * GET /api/reportes/clientes?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-01-31
     */
    @GetMapping("/clientes")
    public ResponseEntity<ApiResponse<ReporteClientesResponse>> obtenerReporteClientes(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Principal principal
    ) {
        ReporteClientesResponse reporte = reporteUseCase.obtenerReporteClientes(
                empresaId, fechaInicio, fechaFin, principal.getName()
        );
        return ResponseEntity.ok(ApiResponse.success("Reporte de clientes generado", reporte));
    }
    
    /**
     * Obtener reporte consolidado con todas las métricas
     * 
     * GET /api/reportes/consolidado?empresaId=1&fechaInicio=2024-01-01&fechaFin=2024-01-31
     */
    @GetMapping("/consolidado")
    public ResponseEntity<ApiResponse<ReporteConsolidadoResponse>> obtenerReporteConsolidado(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Principal principal
    ) {
        ReporteConsolidadoResponse reporte = reporteUseCase.obtenerReporteConsolidado(
                empresaId, fechaInicio, fechaFin, principal.getName()
        );
        return ResponseEntity.ok(ApiResponse.success("Reporte consolidado generado", reporte));
    }
}
