package apex.code.clipperBarberShop.register.application.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroRequest {
    private String empresaNombre;
    private String empresaEmail;
    private String adminName;
    private String adminLastName;
    private String adminEmail;
    private String adminPassword;
}
