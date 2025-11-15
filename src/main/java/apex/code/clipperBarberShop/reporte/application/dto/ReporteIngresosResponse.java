package apex.code.clipperBarberShop.reporte.application.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta para reporte de ingresos
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteIngresosResponse {
    private Long empresaId;
    private String empresaNombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Double ingresoTotal;
    private Integer totalReservas;
    private Integer reservasCompletadas;
    private Integer reservasCanceladas;
    private Double ingresoPromedioPorReserva;
    private List<IngresoPorDia> ingresosPorDia;
    private List<IngresoPorServicio> ingresosPorServicio;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IngresoPorDia {
        private LocalDate fecha;
        private Double ingreso;
        private Integer cantidadReservas;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IngresoPorServicio {
        private Long servicioId;
        private String servicioNombre;
        private Double ingreso;
        private Integer cantidadReservas;
    }
}
