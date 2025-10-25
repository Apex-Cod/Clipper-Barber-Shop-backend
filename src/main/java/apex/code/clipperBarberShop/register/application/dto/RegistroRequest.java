package apex.code.clipperBarberShop.register.application.dto;

import apex.code.clipperBarberShop.Entities.enums.PublicoObjetivo;
import apex.code.clipperBarberShop.shared.validation.ValidPassword;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroRequest {
    
    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre de la empresa debe tener entre 2 y 100 caracteres")
    private String empresaNombre;
    
    @NotBlank(message = "El email de la empresa es obligatorio")
    @Email(message = "El email de la empresa debe ser válido")
    @Size(max = 100, message = "El email de la empresa no puede exceder 100 caracteres")
    private String empresaEmail;
    
    @NotNull(message = "El público objetivo es obligatorio")
    private PublicoObjetivo empresaPublicoObjetivo;
    
    @NotBlank(message = "El nombre del administrador es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre del administrador debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    private String adminName;
    
    @NotBlank(message = "El apellido del administrador es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido del administrador debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido solo puede contener letras y espacios")
    private String adminLastName;
    
    @NotBlank(message = "El email del administrador es obligatorio")
    @Email(message = "El email del administrador debe ser válido")
    @Size(max = 100, message = "El email del administrador no puede exceder 100 caracteres")
    private String adminEmail;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @ValidPassword
    private String adminPassword;
}
