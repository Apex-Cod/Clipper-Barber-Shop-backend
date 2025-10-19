package apex.code.clipperBarberShop.Entities;

import apex.code.clipperBarberShop.Entities.base.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "promociones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Promocion extends SoftDeletableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    private String description;

    private Double discount;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @Deprecated // Usar el campo 'deleted' de SoftDeletableEntity
    private Boolean activa;
    
    @PrePersist
    public void prePersist(){
        if(this.getDeleted() == null) this.setDeleted(false);
    }
}
