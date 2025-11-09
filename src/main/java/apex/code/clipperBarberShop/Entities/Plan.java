package apex.code.clipperBarberShop.Entities;

import apex.code.clipperBarberShop.Entities.base.SoftDeletableEntity;
import apex.code.clipperBarberShop.Entities.enums.TipoPlan;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, unique = true, length = 20)
    private TipoPlan tipo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal precio = BigDecimal.ZERO;

    @Column(name = "duracion_meses", nullable = false)
    @Builder.Default
    private Integer duracionMeses = 1;

    @Column(name = "limite_usuarios")
    private Integer limiteUsuarios; // null = ilimitado

    @Column(name = "limite_reservas_mes")
    private Integer limiteReservasMes; // null = ilimitado

    @Column(name = "limite_servicios")
    private Integer limiteServicios; // null = ilimitado

    @Column(name = "limite_promociones")
    private Integer limitePromociones; // null = ilimitado

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @PrePersist
    public void prePersist() {
        if (this.activo == null) this.activo = true;
        if (this.precio == null) this.precio = BigDecimal.ZERO;
        if (this.duracionMeses == null) this.duracionMeses = 1;
        if (this.getDeleted() == null) this.setDeleted(false);
    }

    // Métodos de negocio
    public boolean esGratuito() {
        return this.tipo == TipoPlan.GRATUITO;
    }

    public boolean esBasico() {
        return this.tipo == TipoPlan.BASICO;
    }

    public boolean esPremium() {
        return this.tipo == TipoPlan.PREMIUM;
    }

    public boolean tieneUsuariosIlimitados() {
        return this.limiteUsuarios == null;
    }

    public boolean tieneReservasIlimitadas() {
        return this.limiteReservasMes == null;
    }

    public boolean tieneServiciosIlimitados() {
        return this.limiteServicios == null;
    }

    public boolean tienePromocionesIlimitadas() {
        return this.limitePromociones == null;
    }
}
