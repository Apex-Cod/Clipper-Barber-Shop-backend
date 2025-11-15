package apex.code.clipperBarberShop.reporte.application.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta para reporte de servicios
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteServiciosResponse {
    private Long empresaId;
    private String empresaNombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer totalServicios;
    private List<ServicioEstadistica> servicios;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ServicioEstadistica {
        private Long servicioId;
        private String servicioNombre;
        private Double precio;
        private Integer duracionMinutos;
        private Integer cantidadReservas;
        private Double ingresoGenerado;
        private Double porcentajeDelTotal;
    }
}
