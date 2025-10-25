package apex.code.clipperBarberShop.promocion.application.dto;

import apex.code.clipperBarberShop.Entities.enums.TipoDescuento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de respuesta de promoción
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromocionResponse {
    
    private Long id;
    private Long empresaId;
    private String empresaNombre;
    private String nombre;
    private String descripcion;
    private TipoDescuento tipoDescuento;
    private Double valorDescuento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean activa;
    private Integer limiteUsos;
    private Integer usosActuales;
    private Double montoMinimo;
    private Long servicioId;
    private String servicioNombre;
    private Boolean vigente;
    
    /**
     * Descripción legible del descuento
     */
    public String getDescripcionDescuento() {
        if (tipoDescuento == TipoDescuento.PORCENTAJE) {
            return String.format("%.0f%% de descuento", valorDescuento);
        } else {
            return String.format("$%.2f de descuento", valorDescuento);
        }
    }
    
    /**
     * Descripción de disponibilidad
     */
    public String getDisponibilidad() {
        if (limiteUsos == null) {
            return "Ilimitado";
        }
        int disponibles = limiteUsos - usosActuales;
        return String.format("%d de %d usos disponibles", disponibles, limiteUsos);
    }
}
