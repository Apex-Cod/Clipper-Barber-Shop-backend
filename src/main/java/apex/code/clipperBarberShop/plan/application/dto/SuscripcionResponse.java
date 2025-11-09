package apex.code.clipperBarberShop.plan.application.dto;

import apex.code.clipperBarberShop.Entities.enums.EstadoSuscripcion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response con información de una suscripción
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuscripcionResponse {
    
    private Long id;
    private Long empresaId;
    private String empresaNombre;
    private Long planId;
    private String planNombre;
    private String tipoPlan;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoSuscripcion estado;
    private BigDecimal montoPagado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActivacion;
    private Boolean autoRenovar;
    private Long diasRestantes;
    private Boolean estaActiva;
    private String notas;
}
