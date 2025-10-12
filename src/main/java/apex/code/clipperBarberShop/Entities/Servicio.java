package apex.code.clipperBarberShop.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "servicios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Servicio {
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

    private Boolean activo;
}
