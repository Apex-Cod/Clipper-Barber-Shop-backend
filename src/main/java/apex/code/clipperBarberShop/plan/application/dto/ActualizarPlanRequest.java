package apex.code.clipperBarberShop.plan.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPlanRequest {

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    private BigDecimal precio;

    @Min(value = 1, message = "La duración debe ser al menos 1 mes")
    private Integer duracionMeses;

    @Min(value = 1, message = "El límite de usuarios debe ser al menos 1")
    private Integer limiteUsuarios;

    @Min(value = 1, message = "El límite de reservas debe ser al menos 1")
    private Integer limiteReservasMes;

    @Min(value = 1, message = "El límite de servicios debe ser al menos 1")
    private Integer limiteServicios;

    @Min(value = 1, message = "El límite de promociones debe ser al menos 1")
    private Integer limitePromociones;

    private Boolean activo;
}
