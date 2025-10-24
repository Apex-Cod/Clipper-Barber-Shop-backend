package apex.code.clipperBarberShop.Entities;

import apex.code.clipperBarberShop.Entities.base.SoftDeletableEntity;
import apex.code.clipperBarberShop.Entities.enums.CategoriaServicio;
import apex.code.clipperBarberShop.Entities.enums.PublicoObjetivo;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "servicios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Servicio extends SoftDeletableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    private String description;

    @Column(nullable = false)
    private Integer duration;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(length = 50)
    private String categoria; // Ej: "CORTE", "TINTE", "BARBA", "TRATAMIENTO", "MANICURA", "PEDICURA", etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "publico_objetivo", length = 20, nullable = false)
    @Builder.Default
    private PublicoObjetivo publicoObjetivo = PublicoObjetivo.UNISEX; // Público al que está dirigido

    @Deprecated // Usar el campo 'deleted' de SoftDeletableEntity
    private Boolean activo;
    
    @PrePersist
    public void prePersist(){
        if(this.getDeleted() == null) this.setDeleted(false);
        if(this.publicoObjetivo == null) this.publicoObjetivo = PublicoObjetivo.UNISEX;
    }
}
