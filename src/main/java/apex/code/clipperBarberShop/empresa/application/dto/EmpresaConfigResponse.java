package apex.code.clipperBarberShop.empresa.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta con la configuración completa de una empresa
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaConfigResponse {
    
    private Long id;
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private String estado;
    private String publicoObjetivo;
    
    // Ubicación
    private Double latitud;
    private Double longitud;
    
    // Horarios
    private String horarioLunes;
    private String horarioMartes;
    private String horarioMiercoles;
    private String horarioJueves;
    private String horarioViernes;
    private String horarioSabado;
    private String horarioDomingo;
    
    // Imágenes
    private String logoUrl;
    private String logoPath;
    private String bannerUrl;
    private String bannerPath;
    
    // Información adicional
    private String descripcion;
    private String sitioWeb;
    private String redesSociales;
    
    private LocalDateTime createdAt;
}
