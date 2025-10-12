package apex.code.clipperBarberShop.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resenias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resenia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @Column(name = "cliente_id", nullable = false)
    private String clienteId;

    @Column(name = "empleado_id", nullable = false)
    private String empleadoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "calificacion_servicio", nullable = false)
    private Integer calificacionServicio;

    @Column(name = "calificacion_empleado")
    private Integer calificacionEmpleado;

    private String comentario;

    private LocalDateTime fecha;

    @PrePersist
    public void prePersist(){
        if(this.fecha == null) this.fecha = LocalDateTime.now();
    }
}
