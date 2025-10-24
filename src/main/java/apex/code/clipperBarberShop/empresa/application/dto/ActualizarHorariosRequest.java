package apex.code.clipperBarberShop.empresa.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar horarios de atención
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarHorariosRequest {
    
    private String horarioLunes;
    private String horarioMartes;
    private String horarioMiercoles;
    private String horarioJueves;
    private String horarioViernes;
    private String horarioSabado;
    private String horarioDomingo;
}
