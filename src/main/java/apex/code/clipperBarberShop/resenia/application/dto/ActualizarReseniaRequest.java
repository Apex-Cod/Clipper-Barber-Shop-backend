package apex.code.clipperBarberShop.resenia.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarReseniaRequest {
    
    @NotNull(message = "La calificación del servicio es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer calificacionServicio;
    
    @Min(value = 1, message = "La calificación del empleado mínima es 1")
    @Max(value = 5, message = "La calificación del empleado máxima es 5")
    private Integer calificacionEmpleado;
    
    @Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
    private String comentario;
}
