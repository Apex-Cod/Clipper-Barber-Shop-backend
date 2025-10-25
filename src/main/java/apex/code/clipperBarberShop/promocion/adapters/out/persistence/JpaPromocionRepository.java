package apex.code.clipperBarberShop.promocion.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Promocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA de promociones
 */
@Repository
public interface JpaPromocionRepository extends JpaRepository<Promocion, Long> {
    
    Optional<Promocion> findByIdAndDeletedFalse(Long id);
    
    List<Promocion> findByEmpresaIdAndDeletedFalse(Long empresaId);
    
    Page<Promocion> findByEmpresaIdAndDeletedFalse(Long empresaId, Pageable pageable);
    
    List<Promocion> findByEmpresaIdAndActivaTrueAndDeletedFalse(Long empresaId);
    
    /**
     * Encuentra promociones vigentes para un servicio específico o general
     * Una promoción es vigente si:
     * - No está eliminada (deleted = false)
     * - Está activa (activa = true)
     * - La fecha actual está entre fechaInicio y fechaFin
     * - Aún tiene usos disponibles (usosActuales < limiteUsos o limiteUsos es null)
     * - Se aplica al servicio específico o a todos los servicios (servicio_id = servicioId o servicio_id is null)
     */
    @Query("""
        SELECT p FROM Promocion p
        WHERE p.empresa.id = :empresaId
        AND p.deleted = false
        AND p.activa = true
        AND :fechaActual BETWEEN p.fechaInicio AND p.fechaFin
        AND (p.limiteUsos IS NULL OR p.usosActuales < p.limiteUsos)
        AND (p.servicio.id = :servicioId OR p.servicio IS NULL)
        ORDER BY p.valorDescuento DESC
        """)
    List<Promocion> findPromocionesVigentesParaServicio(
            @Param("empresaId") Long empresaId,
            @Param("servicioId") Long servicioId,
            @Param("fechaActual") LocalDate fechaActual
    );
}
