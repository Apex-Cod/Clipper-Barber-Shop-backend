package apex.code.clipperBarberShop.reserva.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelarReservaRequest {
    
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo; // Motivo de la cancelación
}
