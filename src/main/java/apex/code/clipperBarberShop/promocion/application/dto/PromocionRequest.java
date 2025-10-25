package apex.code.clipperBarberShop.promocion.application.dto;

import apex.code.clipperBarberShop.Entities.enums.TipoDescuento;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para crear una promoción
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromocionRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    @NotNull(message = "El tipo de descuento es obligatorio")
    private TipoDescuento tipoDescuento;
    
    @NotNull(message = "El valor del descuento es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El valor del descuento debe ser mayor a 0")
    @DecimalMax(value = "100.0", message = "El porcentaje no puede ser mayor a 100")
    private Double valorDescuento;
    
    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio debe ser hoy o posterior")
    private LocalDate fechaInicio;
    
    @NotNull(message = "La fecha de fin es obligatoria")
    @Future(message = "La fecha de fin debe ser posterior a hoy")
    private LocalDate fechaFin;
    
    @Min(value = 1, message = "El límite de usos debe ser al menos 1")
    private Integer limiteUsos;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto mínimo debe ser mayor a 0")
    private Double montoMinimo;
    
    // ID del servicio (opcional - null aplica a todos los servicios)
    private Long servicioId;
    
    /**
     * Validaciones personalizadas
     */
    @AssertTrue(message = "La fecha de fin debe ser posterior a la fecha de inicio")
    public boolean isFechaFinValida() {
        if (fechaInicio == null || fechaFin == null) {
            return true; // Dejar que otras validaciones manejen los nulls
        }
        return fechaFin.isAfter(fechaInicio);
    }
    
    @AssertTrue(message = "El valor del descuento es inválido para el tipo seleccionado")
    public boolean isValorDescuentoValido() {
        if (tipoDescuento == null || valorDescuento == null) {
            return true;
        }
        
        if (tipoDescuento == TipoDescuento.PORCENTAJE) {
            return valorDescuento > 0 && valorDescuento <= 100;
        } else {
            return valorDescuento > 0;
        }
    }
}
