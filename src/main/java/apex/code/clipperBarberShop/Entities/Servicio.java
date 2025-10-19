package apex.code.clipperBarberShop.Entities;

import apex.code.clipperBarberShop.Entities.base.SoftDeletableEntity;
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

    @Deprecated // Usar el campo 'deleted' de SoftDeletableEntity
    private Boolean activo;
    
    @PrePersist
    public void prePersist(){
        if(this.getDeleted() == null) this.setDeleted(false);
    }
}
