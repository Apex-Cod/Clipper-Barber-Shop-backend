package apex.code.clipperBarberShop.recomendacion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioDestacadoDTO {
    private Long id;
    private String nombre;
    private Double precio;
    private String categoria;
}
