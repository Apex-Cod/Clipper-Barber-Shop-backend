package apex.code.clipperBarberShop.servicio.application.dto;

import apex.code.clipperBarberShop.Entities.enums.PublicoObjetivo;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioResponse {
    private Long id;
    private Long empresaId;
    private String empresaNombre;
    private String name;
    private String description;
    private Integer duration;
    private Double price;
    private String categoria;
    private PublicoObjetivo publicoObjetivo;
    private String imageUrl;
    private Boolean deleted;
}
