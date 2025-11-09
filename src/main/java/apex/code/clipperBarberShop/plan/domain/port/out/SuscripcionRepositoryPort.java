package apex.code.clipperBarberShop.plan.domain.port.out;

import apex.code.clipperBarberShop.Entities.Suscripcion;
import apex.code.clipperBarberShop.Entities.enums.EstadoSuscripcion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SuscripcionRepositoryPort {
    
    /**
     * Guarda una suscripción
     */
    Suscripcion save(Suscripcion suscripcion);
    
    /**
     * Busca una suscripción por ID
     */
    Optional<Suscripcion> findById(Long id);
    
    /**
     * Busca la suscripción activa de una empresa
     */
    Optional<Suscripcion> findByEmpresaIdAndEstado(Long empresaId, EstadoSuscripcion estado);
    
    /**
     * Busca todas las suscripciones de una empresa
     */
    List<Suscripcion> findByEmpresaId(Long empresaId);
    
    /**
     * Busca suscripciones por estado
     */
    List<Suscripcion> findByEstado(EstadoSuscripcion estado);
    
    /**
     * Busca suscripciones que expiran en una fecha específica
     */
    List<Suscripcion> findByFechaFin(LocalDate fecha);
    
    /**
     * Busca suscripciones que expiran entre dos fechas
     */
    List<Suscripcion> findByFechaFinBetween(LocalDate desde, LocalDate hasta);
    
    /**
     * Busca suscripciones activas que ya expiraron (fecha_fin < hoy)
     */
    List<Suscripcion> findActivasExpiradas();
    
    /**
     * Busca suscripciones que están por vencer (próximos N días)
     */
    List<Suscripcion> findProximasAVencer(int diasAnticipacion);
    
    /**
     * Verifica si una empresa tiene una suscripción activa
     */
    boolean empresaTieneSuscripcionActiva(Long empresaId);
}
