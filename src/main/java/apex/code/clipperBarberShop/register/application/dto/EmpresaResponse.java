package apex.code.clipperBarberShop.register.application.dto;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.enums.PublicoObjetivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaResponse {
    private Long id;
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private String descripcion;
    private PublicoObjetivo publicoObjetivo;
    
    // Horarios (formato String como en la BD)
    private String horarioLunes;
    private String horarioMartes;
    private String horarioMiercoles;
    private String horarioJueves;
    private String horarioViernes;
    private String horarioSabado;
    private String horarioDomingo;
    
    // Imágenes
    private String logoUrl;
    private String bannerUrl;
    
    // Ubicación
    private Double latitud;
    private Double longitud;
    
    // Otros
    private String sitioWeb;
    private String redesSociales;

    public static EmpresaResponse fromEntity(Empresa empresa) {
        return EmpresaResponse.builder()
                .id(empresa.getId())
                .nombre(empresa.getNombre())
                .direccion(empresa.getDireccion())
                .telefono(empresa.getTelefono())
                .email(empresa.getEmail())
                .descripcion(empresa.getDescripcion())
                .publicoObjetivo(empresa.getPublicoObjetivo())
                .horarioLunes(empresa.getHorarioLunes())
                .horarioMartes(empresa.getHorarioMartes())
                .horarioMiercoles(empresa.getHorarioMiercoles())
                .horarioJueves(empresa.getHorarioJueves())
                .horarioViernes(empresa.getHorarioViernes())
                .horarioSabado(empresa.getHorarioSabado())
                .horarioDomingo(empresa.getHorarioDomingo())
                .logoUrl(empresa.getLogoUrl())
                .bannerUrl(empresa.getBannerUrl())
                .latitud(empresa.getLatitud())
                .longitud(empresa.getLongitud())
                .sitioWeb(empresa.getSitioWeb())
                .redesSociales(empresa.getRedesSociales())
                .build();
    }
}
