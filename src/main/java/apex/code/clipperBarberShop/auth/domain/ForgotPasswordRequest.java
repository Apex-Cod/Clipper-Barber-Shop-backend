package apex.code.clipperBarberShop.auth.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la solicitud de recuperación de contraseña
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;
}
