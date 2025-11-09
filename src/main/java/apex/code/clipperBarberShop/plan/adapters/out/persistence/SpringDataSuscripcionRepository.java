package apex.code.clipperBarberShop.plan.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Suscripcion;
import apex.code.clipperBarberShop.Entities.enums.EstadoSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataSuscripcionRepository extends JpaRepository<Suscripcion, Long> {
    
    /**
     * Busca la suscripción activa de una empresa
     */
    Optional<Suscripcion> findByEmpresaIdAndEstadoAndDeletedFalse(Long empresaId, EstadoSuscripcion estado);
    
    /**
     * Busca todas las suscripciones de una empresa (no eliminadas)
     */
    List<Suscripcion> findByEmpresaIdAndDeletedFalseOrderByFechaCreacionDesc(Long empresaId);
    
    /**
     * Busca suscripciones por estado (no eliminadas)
     */
    List<Suscripcion> findByEstadoAndDeletedFalse(EstadoSuscripcion estado);
    
    /**
     * Busca suscripciones que expiran en una fecha específica
     */
    List<Suscripcion> findByFechaFinAndDeletedFalse(LocalDate fecha);
    
    /**
     * Busca suscripciones que expiran entre dos fechas
     */
    List<Suscripcion> findByFechaFinBetweenAndDeletedFalse(LocalDate desde, LocalDate hasta);
    
    /**
     * Busca suscripciones activas que ya expiraron
     */
    @Query("SELECT s FROM Suscripcion s WHERE s.estado = 'ACTIVA' AND s.fechaFin < :hoy AND s.deleted = false")
    List<Suscripcion> findActivasExpiradas(@Param("hoy") LocalDate hoy);
    
    /**
     * Busca suscripciones activas que están por vencer
     */
    @Query("SELECT s FROM Suscripcion s WHERE s.estado = 'ACTIVA' AND s.fechaFin BETWEEN :hoy AND :fechaLimite AND s.deleted = false")
    List<Suscripcion> findProximasAVencer(@Param("hoy") LocalDate hoy, @Param("fechaLimite") LocalDate fechaLimite);
    
    /**
     * Verifica si una empresa tiene una suscripción activa
     */
    boolean existsByEmpresaIdAndEstadoAndDeletedFalse(Long empresaId, EstadoSuscripcion estado);
}
