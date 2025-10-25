package apex.code.clipperBarberShop.reserva.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearReservaRequest {
    
    @NotNull(message = "El ID del servicio es obligatorio")
    private Long serviceId;
    
    @NotNull(message = "La fecha y hora de la reserva es obligatoria")
    @Future(message = "La fecha de la reserva debe ser en el futuro")
    private LocalDateTime reservationDate;
    
    @NotBlank(message = "El ID del empleado es obligatorio")
    private String employeeId;
    
    private Long promocionId; // Opcional
    
    private String notas; // Notas adicionales del cliente
}
