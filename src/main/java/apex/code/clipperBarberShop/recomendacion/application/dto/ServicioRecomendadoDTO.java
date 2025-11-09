package apex.code.clipperBarberShop.recomendacion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioRecomendadoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer duracion;
    private String categoria;
    private String publicoObjetivo;
    private String imageUrl;
    
    // Datos de la empresa
    private Long empresaId;
    private String empresaNombre;
    private String empresaDireccion;
    private String empresaLogoUrl;
    
    // Métricas de recomendación
    private Double calificacionPromedio;
    private Integer totalResenias;
    private Integer vecesUsadoPorCliente;
    private String motivoRecomendacion;
}
