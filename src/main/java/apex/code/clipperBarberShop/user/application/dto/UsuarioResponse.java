package apex.code.clipperBarberShop.user.application.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO de respuesta con información de un usuario
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {
    private String id;
    private Long empresaId;
    private String empresaNombre;
    private String name;
    private String lastName;
    private String email;
    private String role;
    private Boolean activo;
    private LocalDateTime registrationDate;
    private Boolean deleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
