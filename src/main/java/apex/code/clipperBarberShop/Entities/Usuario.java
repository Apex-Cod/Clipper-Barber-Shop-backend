package apex.code.clipperBarberShop.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    @Id
    @Column(length = 255)
    private String id; // uuid

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(length = 20, nullable = false)
    private String role; // OWNER, ADMIN, CLIENT, EMPLOYEE

    @PrePersist
    public void prePersist(){
        if(this.registrationDate == null) this.registrationDate = LocalDateTime.now();
    }
}
