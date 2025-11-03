package apex.code.clipperBarberShop.payment.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Request para crear un pago de reserva
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearPagoReservaRequest {
    
    @NotNull(message = "El ID de la reserva es obligatorio")
    private Long reservaId;
    
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private Double monto;
    
    private String descripcion;
    
    private String moneda; // Si no se especifica, usa USD por defecto
}
