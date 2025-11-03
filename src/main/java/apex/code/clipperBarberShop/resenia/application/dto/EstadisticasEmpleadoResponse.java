package apex.code.clipperBarberShop.resenia.application.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasEmpleadoResponse {
    private String empleadoId;
    private String empleadoNombre;
    private Long totalResenias;
    private Double promedioCalificacion;
}
