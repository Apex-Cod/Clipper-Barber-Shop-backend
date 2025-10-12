package apex.code.clipperBarberShop.register.application.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpleadoRequest {
    private String empresaId; // as String to be flexible, we'll parse to Long
    private String name;
    private String lastName;
    private String email;
    private String password;
}
