package apex.code.clipperBarberShop.reserva.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReprogramarReservaRequest {
    
    @NotNull(message = "La nueva fecha y hora es obligatoria")
    @Future(message = "La fecha de la reserva debe ser en el futuro")
    private LocalDateTime nuevaFecha;
    
    @NotBlank(message = "El ID del empleado es obligatorio")
    private String employeeId;
    
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo; // Motivo de la reprogramación
}
