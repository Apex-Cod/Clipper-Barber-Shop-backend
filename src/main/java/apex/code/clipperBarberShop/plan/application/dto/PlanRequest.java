package apex.code.clipperBarberShop.plan.application.dto;

import apex.code.clipperBarberShop.Entities.enums.TipoPlan;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequest {
    
    @NotNull(message = "El tipo de plan es obligatorio")
    private TipoPlan tipo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    private BigDecimal precio;

    @NotNull(message = "La duración en meses es obligatoria")
    @Min(value = 1, message = "La duración debe ser al menos 1 mes")
    private Integer duracionMeses;

    @Min(value = 1, message = "El límite de usuarios debe ser al menos 1")
    private Integer limiteUsuarios; // null = ilimitado

    @Min(value = 1, message = "El límite de reservas debe ser al menos 1")
    private Integer limiteReservasMes; // null = ilimitado

    @Min(value = 1, message = "El límite de servicios debe ser al menos 1")
    private Integer limiteServicios; // null = ilimitado

    @Min(value = 1, message = "El límite de promociones debe ser al menos 1")
    private Integer limitePromociones; // null = ilimitado

    private Boolean activo;
}
