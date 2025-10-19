package apex.code.clipperBarberShop.Entities;

import apex.code.clipperBarberShop.Entities.base.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "planes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Plan extends SoftDeletableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(name = "duracion_meses", nullable = false)
    private Integer duracionMeses;

    @Column(name = "max_usuarios")
    private Integer maxUsuarios;

    @Column(name = "max_reservas")
    private Integer maxReservas;
    
    @PrePersist
    public void prePersist(){
        if(this.getDeleted() == null) this.setDeleted(false);
    }
}
