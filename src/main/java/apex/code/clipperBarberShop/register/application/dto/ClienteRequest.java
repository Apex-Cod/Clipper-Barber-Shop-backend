package apex.code.clipperBarberShop.register.application.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequest {
    private String name;
    private String lastName;
    private String email;
    private String password;
}
