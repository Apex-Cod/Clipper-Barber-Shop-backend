package apex.code.clipperBarberShop.user.application.dto;

import apex.code.clipperBarberShop.shared.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO para cambiar la contraseña de un usuario
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambiarPasswordRequest {
    
    @NotBlank(message = "La contraseña actual es obligatoria")
    private String currentPassword;
    
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @ValidPassword
    private String newPassword;
    
    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    private String confirmPassword;
}
