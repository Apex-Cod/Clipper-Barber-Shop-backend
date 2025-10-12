package apex.code.clipperBarberShop.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracion_empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionEmpresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false, unique = true)
    private Empresa empresa;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "color_tema")
    private String colorTema;

    @Column(name = "zona_horaria")
    private String zonaHoraria;
}
