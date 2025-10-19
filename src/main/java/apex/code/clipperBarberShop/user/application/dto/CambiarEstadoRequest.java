package apex.code.clipperBarberShop.user.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para cambiar el estado activo/inactivo de un usuario
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambiarEstadoRequest {
    
    @NotNull(message = "El estado es obligatorio")
    private Boolean activo;
    
    private String motivo; // Opcional: razón del cambio de estado
}
