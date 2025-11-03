package apex.code.clipperBarberShop.Entities;

import apex.code.clipperBarberShop.Entities.base.SoftDeletableEntity;
import apex.code.clipperBarberShop.Entities.enums.EstadoPago;
import apex.code.clipperBarberShop.Entities.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un pago en el sistema.
 * Puede ser un pago de reserva o de suscripción.
 */
@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Pago extends SoftDeletableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    // Relación opcional con suscripción
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suscripcion_id")
    private Suscripcion suscripcion;
    
    // Relación opcional con reserva
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @Column(nullable = false)
    private BigDecimal monto;
    
    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda; // USD, EUR, COP, etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPago estado;
    
    // Campos específicos de PayPal
    @Column(name = "paypal_payment_id", unique = true)
    private String paypalPaymentId;
    
    @Column(name = "paypal_payer_id")
    private String paypalPayerId;
    
    @Column(name = "paypal_sale_id")
    private String paypalSaleId;
    
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "notas", length = 1000)
    private String notas;

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoPago.PENDIENTE;
        }
        if (this.moneda == null) {
            this.moneda = "USD";
        }
        if (this.getDeleted() == null) {
            this.setDeleted(false);
        }
    }
    
    /**
     * Marca el pago como completado
     */
    public void marcarComoCompletado(String payerId, String saleId) {
        this.estado = EstadoPago.COMPLETADO;
        this.paypalPayerId = payerId;
        this.paypalSaleId = saleId;
        this.fechaPago = LocalDateTime.now();
    }
    
    /**
     * Marca el pago como fallido
     */
    public void marcarComoFallido(String motivo) {
        this.estado = EstadoPago.FALLIDO;
        this.notas = motivo;
    }
    
    /**
     * Marca el pago como cancelado
     */
    public void marcarComoCancelado(String motivo) {
        this.estado = EstadoPago.CANCELADO;
        this.notas = motivo;
    }
    
    /**
     * Marca el pago como reembolsado
     */
    public void marcarComoReembolsado(String motivo) {
        this.estado = EstadoPago.REEMBOLSADO;
        this.notas = motivo;
    }
}
