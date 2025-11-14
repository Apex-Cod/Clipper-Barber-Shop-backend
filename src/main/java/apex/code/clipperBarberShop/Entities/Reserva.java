package apex.code.clipperBarberShop.Entities;

import apex.code.clipperBarberShop.Entities.base.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Reserva extends SoftDeletableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(name = "final_price")
    private Double finalPrice;

    @Column(name = "reservation_date", nullable = false)
    private LocalDateTime reservationDate;

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @Column(length = 20)
    private String status; // PENDING, CONFIRMED, COMPLETED, CANCELLED

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Servicio service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promocion_id")
    private Promocion promocion;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "recordatorio_enviado", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean recordatorioEnviado = false; // Indica si ya se envió el recordatorio por email

    @PrePersist
    public void prePersist(){
        if(this.createdAt == null) this.createdAt = LocalDateTime.now();
        if(this.status == null) this.status = "PENDING";
        if(this.getDeleted() == null) this.setDeleted(false);
        if(this.recordatorioEnviado == null) this.recordatorioEnviado = false;
    }
}
