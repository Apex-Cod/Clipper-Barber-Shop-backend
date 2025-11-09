package apex.code.clipperBarberShop.plan.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para crear una nueva suscripción
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearSuscripcionRequest {
    
    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long empresaId;
    
    @NotNull(message = "El ID del plan es obligatorio")
    private Long planId;
}
