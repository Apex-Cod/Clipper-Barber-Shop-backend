package apex.code.clipperBarberShop.Entities;

import apex.code.clipperBarberShop.Entities.base.SoftDeletableEntity;
import apex.code.clipperBarberShop.Entities.enums.TipoDescuento;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Entidad Promocion
 * Representa las promociones y descuentos ofrecidos por las empresas
 */
@Entity
@Table(name = "promociones")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Promocion extends SoftDeletableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento", nullable = false, length = 20)
    private TipoDescuento tipoDescuento;
    
    // Valor del descuento (porcentaje 0-100 o monto fijo)
    @Column(name = "valor_descuento", nullable = false)
    private Double valorDescuento;
    
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;
    
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;
    
    // Cantidad máxima de usos (null = ilimitado)
    @Column(name = "limite_usos")
    private Integer limiteUsos;
    
    // Contador de usos actuales
    @Column(name = "usos_actuales", nullable = false)
    @Builder.Default
    private Integer usosActuales = 0;
    
    // Monto mínimo de compra para aplicar la promoción
    @Column(name = "monto_minimo")
    private Double montoMinimo;
    
    // Relación opcional con servicio específico (null = aplica a todos)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;
    
    /**
     * Verifica si la promoción está vigente
     */
    public boolean isVigente() {
        LocalDate hoy = LocalDate.now();
        return activa && 
               !getDeleted() &&
               !hoy.isBefore(fechaInicio) && 
               !hoy.isAfter(fechaFin) &&
               (limiteUsos == null || usosActuales < limiteUsos);
    }
    
    /**
     * Verifica si la promoción es aplicable a un monto dado
     */
    public boolean isAplicable(Double monto) {
        return isVigente() && (montoMinimo == null || monto >= montoMinimo);
    }
    
    /**
     * Verifica si la promoción es aplicable a un servicio específico
     */
    public boolean isAplicableAServicio(Long servicioId) {
        return servicio == null || servicio.getId().equals(servicioId);
    }
    
    /**
     * Calcula el descuento para un monto dado
     */
    public Double calcularDescuento(Double montoBase) {
        if (!isAplicable(montoBase)) {
            return 0.0;
        }
        
        if (tipoDescuento == TipoDescuento.PORCENTAJE) {
            return montoBase * (valorDescuento / 100.0);
        } else {
            // MONTO_FIJO - no puede exceder el monto base
            return Math.min(valorDescuento, montoBase);
        }
    }
    
    /**
     * Incrementa el contador de usos
     */
    public void incrementarUsos() {
        this.usosActuales++;
    }
    
    @PrePersist
    public void prePersist() {
        if (this.getDeleted() == null) this.setDeleted(false);
        if (this.activa == null) this.activa = true;
        if (this.usosActuales == null) this.usosActuales = 0;
    }
}
