package apex.code.clipperBarberShop.promocion.application.dto;

import apex.code.clipperBarberShop.Entities.enums.TipoDescuento;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para actualizar una promoción
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPromocionRequest {
    
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    private TipoDescuento tipoDescuento;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "El valor del descuento debe ser mayor a 0")
    private Double valorDescuento;
    
    @FutureOrPresent(message = "La fecha de inicio debe ser hoy o posterior")
    private LocalDate fechaInicio;
    
    @Future(message = "La fecha de fin debe ser posterior a hoy")
    private LocalDate fechaFin;
    
    private Boolean activa;
    
    @Min(value = 1, message = "El límite de usos debe ser al menos 1")
    private Integer limiteUsos;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto mínimo debe ser mayor a 0")
    private Double montoMinimo;
    
    private Long servicioId;
}
