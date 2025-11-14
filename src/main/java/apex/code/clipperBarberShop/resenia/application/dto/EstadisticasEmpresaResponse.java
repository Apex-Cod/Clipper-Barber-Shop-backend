package apex.code.clipperBarberShop.resenia.application.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasEmpresaResponse {
    private Long empresaId;
    private String empresaNombre;
    private Long totalResenias;
    private Double promedioCalificacion;
    private Long resenias5Estrellas;
    private Long resenias4Estrellas;
    private Long resenias3Estrellas;
    private Long resenias2Estrellas;
    private Long resenias1Estrella;
    private Map<Integer, Long> distribucionCalificaciones;
}
