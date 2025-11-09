package apex.code.clipperBarberShop.Entities;

import apex.code.clipperBarberShop.Entities.base.SoftDeletableEntity;
import apex.code.clipperBarberShop.Entities.enums.PublicoObjetivo;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empresas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Empresa extends SoftDeletableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre; // Max 100 según validaciones

    private String direccion;

    private String telefono;

    @Column(length = 100)
    private String email; // Max 100 según validaciones

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(length = 20)
    private String estado; // ACTIVA / INACTIVA

    @Enumerated(EnumType.STRING)
    @Column(name = "publico_objetivo", length = 20, nullable = true)
    @Builder.Default
    private PublicoObjetivo publicoObjetivo = PublicoObjetivo.UNISEX; // Público al que está dirigida la empresa

    @Column
    private Double latitud;

    @Column
    private Double longitud;

    // Horarios de atención
    @Column(name = "horario_lunes", length = 50)
    private String horarioLunes; // Ej: "09:00-18:00" o "09:00-13:00,15:00-19:00" o "CERRADO"

    @Column(name = "horario_martes", length = 50)
    private String horarioMartes;

    @Column(name = "horario_miercoles", length = 50)
    private String horarioMiercoles;

    @Column(name = "horario_jueves", length = 50)
    private String horarioJueves;

    @Column(name = "horario_viernes", length = 50)
    private String horarioViernes;

    @Column(name = "horario_sabado", length = 50)
    private String horarioSabado;

    @Column(name = "horario_domingo", length = 50)
    private String horarioDomingo;

    // Campos de imágenes (Supabase Storage)
    @Column(name = "logo_url", length = 500)
    private String logoUrl; // URL del logo de la empresa en Supabase

    @Column(name = "logo_path", length = 255)
    private String logoPath; // Path del archivo en Supabase Storage (ej: "empresas/123/logo.jpg")

    @Column(name = "banner_url", length = 500)
    private String bannerUrl; // URL del banner/portada de la empresa

    @Column(name = "banner_path", length = 255)
    private String bannerPath; // Path del banner en Supabase Storage

    @Column(length = 1000)
    private String descripcion; // Descripción de la empresa

    @Column(name = "sitio_web", length = 255)
    private String sitioWeb; // Sitio web de la empresa

    @Column(name = "redes_sociales", length = 1000)
    private String redesSociales; // JSON con redes sociales: {"facebook":"url","instagram":"url","twitter":"url"}

    // Relación con Plan (el plan de suscripción de la empresa)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Usuario> usuarios = new ArrayList<>();

    @PrePersist
    public void prePersist(){
        if(this.createdAt == null) this.createdAt = LocalDateTime.now();
        if(this.estado == null) this.estado = "ACTIVA";
        if(this.getDeleted() == null) this.setDeleted(false);
        if(this.publicoObjetivo == null) this.publicoObjetivo = PublicoObjetivo.UNISEX;
    }
}
