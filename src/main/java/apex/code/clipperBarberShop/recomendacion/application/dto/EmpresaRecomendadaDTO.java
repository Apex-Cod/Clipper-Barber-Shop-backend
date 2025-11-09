package apex.code.clipperBarberShop.recomendacion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaRecomendadaDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private String logoUrl;
    private String bannerUrl;
    private String descripcion;
    private String publicoObjetivo;
    
    // Métricas de recomendación
    private Double calificacionPromedio;
    private Integer totalResenias;
    private Integer vecesVisitadaPorCliente;
    private String motivoRecomendacion;
    
    // Servicios destacados
    private List<ServicioDestacadoDTO> serviciosDestacados;
}
