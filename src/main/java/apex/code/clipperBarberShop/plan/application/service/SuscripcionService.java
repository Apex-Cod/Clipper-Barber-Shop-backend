package apex.code.clipperBarberShop.plan.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Plan;
import apex.code.clipperBarberShop.Entities.Suscripcion;
import apex.code.clipperBarberShop.Entities.enums.EstadoSuscripcion;
import apex.code.clipperBarberShop.plan.domain.exception.SuscripcionException;
import apex.code.clipperBarberShop.plan.domain.port.out.SuscripcionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para gestionar suscripciones de planes.
 * Maneja la creación, activación, renovación y expiración de suscripciones.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SuscripcionService {
    
    private final SuscripcionRepositoryPort suscripcionRepository;

    /**
     * Crea una nueva suscripción para una empresa.
     * La suscripción se crea en estado PENDIENTE_PAGO.
     */
    @Transactional
    public Suscripcion crearSuscripcion(Empresa empresa, Plan plan) {
        // Verificar si ya tiene una suscripción activa
        suscripcionRepository.findByEmpresaIdAndEstado(empresa.getId(), EstadoSuscripcion.ACTIVA)
            .ifPresent(s -> {
                throw new SuscripcionException("La empresa ya tiene una suscripción activa. " +
                    "Debe cancelarla o esperar a que expire antes de crear una nueva.");
            });

        LocalDate hoy = LocalDate.now();
        LocalDate fechaFin = hoy.plusMonths(plan.getDuracionMeses());

        Suscripcion suscripcion = Suscripcion.builder()
            .empresa(empresa)
            .plan(plan)
            .fechaInicio(hoy)
            .fechaFin(fechaFin)
            .estado(EstadoSuscripcion.PENDIENTE_PAGO)
            .autoRenovar(false)
            .build();

        Suscripcion saved = suscripcionRepository.save(suscripcion);
        log.info("Suscripción creada para empresa {} con plan {} (ID: {})", 
            empresa.getId(), plan.getNombre(), saved.getId());
        
        return saved;
    }

    /**
     * Activa una suscripción después de un pago exitoso.
     * Cancela cualquier otra suscripción activa de la misma empresa.
     */
    @Transactional
    public Suscripcion activarSuscripcion(Long suscripcionId, BigDecimal montoPagado) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
            .orElseThrow(() -> new SuscripcionException("Suscripción no encontrada con ID: " + suscripcionId));

        if (suscripcion.getEstado() == EstadoSuscripcion.ACTIVA) {
            throw new SuscripcionException("La suscripción ya está activa");
        }

        // Cancelar cualquier otra suscripción activa de la misma empresa
        suscripcionRepository.findByEmpresaIdAndEstado(
            suscripcion.getEmpresa().getId(), 
            EstadoSuscripcion.ACTIVA
        ).ifPresent(suscripcionActiva -> {
            suscripcionActiva.cancelar("Reemplazada por nueva suscripción");
            suscripcionRepository.save(suscripcionActiva);
            log.info("Suscripción anterior cancelada (ID: {})", suscripcionActiva.getId());
        });

        // Activar la nueva suscripción
        suscripcion.activar(montoPagado);
        Suscripcion saved = suscripcionRepository.save(suscripcion);
        
        log.info("Suscripción activada: {} - Empresa: {} - Plan: {}", 
            saved.getId(), saved.getEmpresa().getId(), saved.getPlan().getNombre());
        
        return saved;
    }

    /**
     * Obtiene la suscripción activa de una empresa.
     * Verifica que no haya expirado.
     */
    @Transactional(readOnly = true)
    public Suscripcion obtenerSuscripcionActiva(Long empresaId) {
        Suscripcion suscripcion = suscripcionRepository
            .findByEmpresaIdAndEstado(empresaId, EstadoSuscripcion.ACTIVA)
            .orElseThrow(() -> new SuscripcionException(
                "La empresa no tiene una suscripción activa. " +
                "Por favor, contrata un plan para continuar usando el servicio."));

        // Verificar si expiró
        if (suscripcion.haExpirado()) {
            throw new SuscripcionException(
                "Tu suscripción expiró el " + suscripcion.getFechaFin() + ". " +
                "Por favor, renueva tu plan para continuar usando el servicio.");
        }

        return suscripcion;
    }

    /**
     * Verifica si una empresa tiene una suscripción activa y válida.
     */
    @Transactional(readOnly = true)
    public boolean empresaTieneSuscripcionValida(Long empresaId) {
        return suscripcionRepository.findByEmpresaIdAndEstado(empresaId, EstadoSuscripcion.ACTIVA)
            .map(Suscripcion::estaActiva)
            .orElse(false);
    }

    /**
     * Obtiene todas las suscripciones de una empresa (historial).
     */
    @Transactional(readOnly = true)
    public List<Suscripcion> obtenerHistorialSuscripciones(Long empresaId) {
        return suscripcionRepository.findByEmpresaId(empresaId);
    }

    /**
     * Cancela una suscripción.
     */
    @Transactional
    public Suscripcion cancelarSuscripcion(Long suscripcionId, String motivo) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
            .orElseThrow(() -> new SuscripcionException("Suscripción no encontrada con ID: " + suscripcionId));

        if (suscripcion.getEstado() == EstadoSuscripcion.CANCELADA) {
            throw new SuscripcionException("La suscripción ya está cancelada");
        }

        suscripcion.cancelar(motivo);
        Suscripcion saved = suscripcionRepository.save(suscripcion);
        
        log.info("Suscripción cancelada: {} - Motivo: {}", suscripcionId, motivo);
        
        return saved;
    }

    /**
     * Marca como expiradas todas las suscripciones activas que ya pasaron su fecha de fin.
     * Este método debe ser ejecutado por un job programado diariamente.
     */
    @Transactional
    public int marcarSuscripcionesExpiradas() {
        List<Suscripcion> expiradas = suscripcionRepository.findActivasExpiradas();
        
        expiradas.forEach(suscripcion -> {
            suscripcion.marcarComoExpirada();
            suscripcionRepository.save(suscripcion);
            log.info("Suscripción expirada automáticamente: {} - Empresa: {}", 
                suscripcion.getId(), suscripcion.getEmpresa().getId());
        });

        log.info("Total de suscripciones expiradas: {}", expiradas.size());
        return expiradas.size();
    }

    /**
     * Obtiene suscripciones que están por vencer en los próximos N días.
     * Útil para enviar notificaciones.
     */
    @Transactional(readOnly = true)
    public List<Suscripcion> obtenerSuscripcionesProximasAVencer(int diasAnticipacion) {
        return suscripcionRepository.findProximasAVencer(diasAnticipacion);
    }

    /**
     * Renueva una suscripción creando una nueva que inicia donde termina la actual.
     */
    @Transactional
    public Suscripcion renovarSuscripcion(Long suscripcionId) {
        Suscripcion suscripcionActual = suscripcionRepository.findById(suscripcionId)
            .orElseThrow(() -> new SuscripcionException("Suscripción no encontrada con ID: " + suscripcionId));

        Suscripcion nuevaSuscripcion = suscripcionActual.renovar();
        Suscripcion saved = suscripcionRepository.save(nuevaSuscripcion);
        
        log.info("Suscripción renovada: {} -> {} - Empresa: {}", 
            suscripcionId, saved.getId(), saved.getEmpresa().getId());
        
        return saved;
    }

    /**
     * Obtiene información del estado de la suscripción de una empresa.
     */
    @Transactional(readOnly = true)
    public SuscripcionInfo obtenerInfoSuscripcion(Long empresaId) {
        Suscripcion suscripcion = suscripcionRepository
            .findByEmpresaIdAndEstado(empresaId, EstadoSuscripcion.ACTIVA)
            .orElse(null);

        if (suscripcion == null) {
            return SuscripcionInfo.builder()
                .tieneActiva(false)
                .mensaje("No hay suscripción activa")
                .build();
        }

        boolean estaActiva = suscripcion.estaActiva();
        long diasRestantes = suscripcion.getDiasRestantes();

        String mensaje;
        if (!estaActiva) {
            mensaje = "Suscripción expirada el " + suscripcion.getFechaFin();
        } else if (diasRestantes <= 7) {
            mensaje = "Tu suscripción vence en " + diasRestantes + " días";
        } else {
            mensaje = "Suscripción activa hasta " + suscripcion.getFechaFin();
        }

        return SuscripcionInfo.builder()
            .tieneActiva(estaActiva)
            .suscripcionId(suscripcion.getId())
            .planNombre(suscripcion.getPlan().getNombre())
            .fechaInicio(suscripcion.getFechaInicio())
            .fechaFin(suscripcion.getFechaFin())
            .diasRestantes(diasRestantes)
            .mensaje(mensaje)
            .proximaAVencer(diasRestantes <= 7)
            .build();
    }

    /**
     * Clase interna para devolver información de suscripción
     */
    @lombok.Builder
    @lombok.Getter
    public static class SuscripcionInfo {
        private boolean tieneActiva;
        private Long suscripcionId;
        private String planNombre;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private long diasRestantes;
        private String mensaje;
        private boolean proximaAVencer;
    }
}
