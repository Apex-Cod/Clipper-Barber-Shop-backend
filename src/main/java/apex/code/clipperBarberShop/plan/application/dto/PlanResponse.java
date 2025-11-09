package apex.code.clipperBarberShop.plan.application.dto;

import apex.code.clipperBarberShop.Entities.Plan;
import apex.code.clipperBarberShop.Entities.enums.TipoPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {
    private Long id;
    private TipoPlan tipo;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer duracionMeses;
    private Integer limiteUsuarios;
    private Integer limiteReservasMes;
    private Integer limiteServicios;
    private Integer limitePromociones;
    private Boolean activo;

    public static PlanResponse fromEntity(Plan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .tipo(plan.getTipo())
                .nombre(plan.getNombre())
                .descripcion(plan.getDescripcion())
                .precio(plan.getPrecio())
                .duracionMeses(plan.getDuracionMeses())
                .limiteUsuarios(plan.getLimiteUsuarios())
                .limiteReservasMes(plan.getLimiteReservasMes())
                .limiteServicios(plan.getLimiteServicios())
                .limitePromociones(plan.getLimitePromociones())
                .activo(plan.getActivo())
                .build();
    }
}
