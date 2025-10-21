package apex.code.clipperBarberShop.Entities.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Clase base para entidades que requieren borrado lógico
 * Las entidades que extiendan esta clase tendrán soporte automático para soft delete
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class SoftDeletableEntity {
    
    /**
     * Indica si el registro ha sido eliminado lógicamente
     */
    @Column(name = "deleted", nullable = false, columnDefinition = "boolean default false")
    private Boolean deleted = false;
    
    /**
     * Fecha y hora en que se realizó el borrado lógico
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    /**
     * ID del usuario que realizó el borrado lógico
     */
    @Column(name = "deleted_by")
    private String deletedBy;
    
    /**
     * Marca el registro como eliminado
     * @param deletedBy ID del usuario que realiza la eliminación
     */
    public void softDelete(String deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
    
    /**
     * Restaura un registro eliminado lógicamente
     */
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }
    
    /**
     * Verifica si el registro está activo (no eliminado)
     * @return true si el registro no ha sido eliminado
     */
    public boolean isActive() {
        return !this.deleted;
    }
}
