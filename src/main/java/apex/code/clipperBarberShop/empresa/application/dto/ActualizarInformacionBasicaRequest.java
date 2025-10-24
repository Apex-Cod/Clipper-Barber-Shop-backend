package apex.code.clipperBarberShop.empresa.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar información básica de la empresa
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarInformacionBasicaRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    private String nombre;
    
    @Size(max = 255, message = "La dirección no puede tener más de 255 caracteres")
    private String direccion;
    
    @Size(max = 20, message = "El teléfono no puede tener más de 20 caracteres")
    private String telefono;
    
    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede tener más de 100 caracteres")
    private String email;
    
    @Size(max = 1000, message = "La descripción no puede tener más de 1000 caracteres")
    private String descripcion;
    
    @Size(max = 255, message = "El sitio web no puede tener más de 255 caracteres")
    private String sitioWeb;
    
    @Size(max = 1000, message = "Las redes sociales no pueden tener más de 1000 caracteres")
    private String redesSociales; // JSON string
    
    private String publicoObjetivo; // HOMBRES, MUJERES, UNISEX
}
