package apex.code.clipperBarberShop.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suscripcion_id", nullable = false)
    private Suscripcion suscripcion;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(length = 20)
    private String metodo; // CARD, PAYPAL, TRANSFERENCIA

    private LocalDateTime fecha;

    @Column(length = 20)
    private String estado; // PENDIENTE, COMPLETADO, FALLIDO

    @PrePersist
    public void prePersist(){
        if(this.fecha == null) this.fecha = LocalDateTime.now();
    }
}
