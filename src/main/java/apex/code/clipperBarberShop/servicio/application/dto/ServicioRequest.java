package apex.code.clipperBarberShop.servicio.application.dto;

import apex.code.clipperBarberShop.Entities.enums.PublicoObjetivo;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioRequest {
    
    @NotNull(message = "El ID de la empresa es obligatorio")
    private Long empresaId;
    
    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String name;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;
    
    @NotNull(message = "La duración es obligatoria")
    @Min(value = 5, message = "La duración mínima es de 5 minutos")
    @Max(value = 480, message = "La duración máxima es de 480 minutos (8 horas)")
    private Integer duration;
    
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @DecimalMax(value = "999999.99", message = "El precio no puede exceder 999999.99")
    private Double price;
    
    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    private String categoria;
    
    @NotNull(message = "El público objetivo es obligatorio")
    private PublicoObjetivo publicoObjetivo;
    
    @Size(max = 500, message = "La URL de la imagen no puede exceder 500 caracteres")
    private String imageUrl; // URL de la imagen del servicio (opcional)
}
