package apex.code.clipperBarberShop.reporte.application.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta para reporte de empleados
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteEmpleadosResponse {
    private Long empresaId;
    private String empresaNombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer totalEmpleados;
    private List<EmpleadoEstadistica> empleados;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmpleadoEstadistica {
        private String empleadoId;
        private String empleadoNombre;
        private String empleadoEmail;
        private String profileImageUrl;
        private Integer cantidadReservas;
        private Integer reservasCompletadas;
        private Integer reservasCanceladas;
        private Double ingresoGenerado;
        private Double promedioCalificacion;
        private Integer totalResenias;
    }
}
