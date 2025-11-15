package apex.code.clipperBarberShop.Entities;

import apex.code.clipperBarberShop.Entities.base.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Usuario extends SoftDeletableEntity {
    @Id
    @Column(length = 255)
    private String id; // uuid

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Column(nullable = false, length = 100)
    private String email; // Max 100 según validaciones

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName; // Max 50 según validaciones

    @Column(nullable = false, length = 50)
    private String name; // Max 50 según validaciones

    @Column(nullable = false, length = 60)
    private String password; // BCrypt hash always generates 60 characters

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(length = 20, nullable = false)
    private String role; // OWNER, ADMIN, CLIENT, EMPLOYEE
    
    @Column(nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean activo = true; // Indica si el usuario está activo (puede usar el sistema)

    @Column(name = "email_verified", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean emailVerified = false; // Indica si el email ha sido verificado
    
    @Column(name = "verification_token", length = 255)
    private String verificationToken; // Token para verificar el email (UUID para web o código de 6 dígitos para móvil)
    
    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry; // Fecha de expiración del token
    
    @Column(name = "verification_attempts", columnDefinition = "integer default 0")
    @Builder.Default
    private Integer verificationAttempts = 0; // Número de intentos de verificación fallidos
    
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl; // URL de la imagen de perfil almacenada en Supabase (opcional)

    @PrePersist
    public void prePersist(){
        if(this.registrationDate == null) this.registrationDate = LocalDateTime.now();
        if(this.getDeleted() == null) this.setDeleted(false);
        if(this.activo == null) this.activo = true;
        if(this.emailVerified == null) this.emailVerified = false;
    }
}
