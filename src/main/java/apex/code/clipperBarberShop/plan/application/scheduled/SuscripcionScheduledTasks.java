package apex.code.clipperBarberShop.plan.application.scheduled;

import apex.code.clipperBarberShop.Entities.Suscripcion;
import apex.code.clipperBarberShop.plan.application.service.SuscripcionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tareas programadas para gestión automática de suscripciones.
 * Ejecuta verificaciones diarias para marcar suscripciones expiradas
 * y notificar las que están próximas a vencer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SuscripcionScheduledTasks {
    
    private final SuscripcionService suscripcionService;
    
    /**
     * Marca como EXPIRADAS las suscripciones activas que ya pasaron su fecha de fin.
     * Se ejecuta todos los días a las 00:00 (medianoche).
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void marcarSuscripcionesExpiradas() {
        log.info("=== Iniciando verificación automática de suscripciones expiradas ===");
        
        try {
            int totalExpiradas = suscripcionService.marcarSuscripcionesExpiradas();
            
            if (totalExpiradas > 0) {
                log.warn("Total de suscripciones marcadas como EXPIRADAS: {}", totalExpiradas);
            } else {
                log.info("No hay suscripciones para expirar hoy");
            }
        } catch (Exception e) {
            log.error("Error al marcar suscripciones expiradas: {}", e.getMessage(), e);
        }
        
        log.info("=== Fin de verificación de suscripciones expiradas ===");
    }
    
    /**
     * Notifica sobre suscripciones que están próximas a vencer (7 días).
     * Se ejecuta todos los días a las 09:00.
     * 
     * TODO: Integrar con sistema de notificaciones por email
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void notificarSuscripcionesProximasAVencer() {
        log.info("=== Iniciando notificación de suscripciones próximas a vencer ===");
        
        try {
            List<Suscripcion> proximasAVencer = 
                suscripcionService.obtenerSuscripcionesProximasAVencer(7);
            
            if (proximasAVencer.isEmpty()) {
                log.info("No hay suscripciones próximas a vencer en los próximos 7 días");
            } else {
                log.info("{} suscripciones próximas a vencer:", proximasAVencer.size());
                
                proximasAVencer.forEach(suscripcion -> {
                    long diasRestantes = suscripcion.getDiasRestantes();
                    
                    log.info("  - Empresa: {} (ID: {}) - Plan: {} - Vence en {} días ({})", 
                        suscripcion.getEmpresa().getNombre(),
                        suscripcion.getEmpresa().getId(),
                        suscripcion.getPlan().getNombre(),
                        diasRestantes,
                        suscripcion.getFechaFin()
                    );
                    
                    // TODO: Enviar email de notificación
                    // emailService.enviarNotificacionVencimiento(suscripcion);
                    
                    // Notificaciones especiales según días restantes
                    if (diasRestantes == 1) {
                        log.warn("URGENTE: La suscripción de {} vence MAÑANA", 
                            suscripcion.getEmpresa().getNombre());
                    } else if (diasRestantes == 3) {
                        log.warn("La suscripción de {} vence en 3 días", 
                            suscripcion.getEmpresa().getNombre());
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error al notificar suscripciones próximas a vencer: {}", e.getMessage(), e);
        }
        
        log.info("=== Fin de notificación de suscripciones próximas a vencer ===");
    }
    
    /**
     * Genera reporte semanal de suscripciones.
     * Se ejecuta todos los lunes a las 08:00.
     */
    @Scheduled(cron = "0 0 8 * * MON")
    public void generarReporteSemanal() {
        log.info("=== Generando reporte semanal de suscripciones ===");
        
        try {
            // TODO: Implementar reporte completo
            // - Total de suscripciones activas
            // - Total de suscripciones expiradas esta semana
            // - Ingresos de la semana
            // - Renovaciones pendientes
            
            log.info(" Reporte semanal generado (pendiente de implementación)");
        } catch (Exception e) {
            log.error("Error al generar reporte semanal: {}", e.getMessage(), e);
        }
        
        log.info("=== Fin de reporte semanal ===");
    }
}
