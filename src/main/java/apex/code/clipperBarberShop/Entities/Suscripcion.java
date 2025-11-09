package apex.code.clipperBarberShop.Entities;

import apex.code.clipperBarberShop.Entities.base.SoftDeletableEntity;
import apex.code.clipperBarberShop.Entities.enums.EstadoSuscripcion;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una suscripción de una empresa a un plan.
 * Una empresa puede tener múltiples suscripciones a lo largo del tiempo,
 * pero solo una debe estar ACTIVA en un momento dado.
 */
@Entity
@Table(name = "suscripciones", indexes = {
    @Index(name = "idx_empresa_estado", columnList = "empresa_id,estado"),
    @Index(name = "idx_fecha_fin", columnList = "fecha_fin")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Suscripcion extends SoftDeletableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSuscripcion estado; // ACTIVA, EXPIRADA, CANCELADA, PENDIENTE_PAGO

    @Column(name = "monto_pagado", precision = 10, scale = 2)
    private BigDecimal montoPagado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_activacion")
    private LocalDateTime fechaActivacion; // Cuando se activó (después del pago)

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion; // Si fue cancelada manualmente

    @Column(name = "auto_renovar")
    private Boolean autoRenovar; // Si se debe renovar automáticamente

    @Column(length = 500)
    private String notas;

    // Relación con los pagos asociados a esta suscripción
    @OneToMany(mappedBy = "suscripcion", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Pago> pagos = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoSuscripcion.PENDIENTE_PAGO;
        }
        if (this.autoRenovar == null) {
            this.autoRenovar = false;
        }
        if (this.getDeleted() == null) {
            this.setDeleted(false);
        }
    }

    /**
     * Activa la suscripción después de un pago exitoso
     */
    public void activar(BigDecimal monto) {
        this.estado = EstadoSuscripcion.ACTIVA;
        this.fechaActivacion = LocalDateTime.now();
        this.montoPagado = monto;
    }

    /**
     * Verifica si la suscripción está activa y no ha expirado
     */
    public boolean estaActiva() {
        return this.estado == EstadoSuscripcion.ACTIVA 
            && LocalDate.now().isBefore(this.fechaFin.plusDays(1)); // Incluye el día de fin
    }

    /**
     * Verifica si la suscripción ha expirado
     */
    public boolean haExpirado() {
        return LocalDate.now().isAfter(this.fechaFin);
    }

    /**
     * Marca la suscripción como expirada
     */
    public void marcarComoExpirada() {
        if (this.estado == EstadoSuscripcion.ACTIVA) {
            this.estado = EstadoSuscripcion.EXPIRADA;
        }
    }

    /**
     * Cancela la suscripción
     */
    public void cancelar(String motivo) {
        this.estado = EstadoSuscripcion.CANCELADA;
        this.fechaCancelacion = LocalDateTime.now();
        this.notas = motivo;
    }

    /**
     * Renueva la suscripción por la duración del plan
     */
    public Suscripcion renovar() {
        return Suscripcion.builder()
            .empresa(this.empresa)
            .plan(this.plan)
            .fechaInicio(this.fechaFin.plusDays(1))
            .fechaFin(this.fechaFin.plusMonths(this.plan.getDuracionMeses()).plusDays(1))
            .estado(EstadoSuscripcion.PENDIENTE_PAGO)
            .autoRenovar(this.autoRenovar)
            .build();
    }

    /**
     * Calcula los días restantes de la suscripción
     */
    public long getDiasRestantes() {
        if (!estaActiva()) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), this.fechaFin);
    }
}
