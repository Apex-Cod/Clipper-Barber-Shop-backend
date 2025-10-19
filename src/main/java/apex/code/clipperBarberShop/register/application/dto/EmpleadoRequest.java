package apex.code.clipperBarberShop.register.application.dto;

import apex.code.clipperBarberShop.shared.validation.ValidPassword;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpleadoRequest {
    
    @NotBlank(message = "El ID de la empresa es obligatorio")
    @Pattern(regexp = "^\\d+$", message = "El ID de la empresa debe ser un número válido")
    private String empresaId; // as String to be flexible, we'll parse to Long
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
    private String name;
    
    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido solo puede contener letras y espacios")
    private String lastName;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @ValidPassword
    private String password;
}
