package apex.code.clipperBarberShop.reserva.application.service;

import apex.code.clipperBarberShop.Entities.Reserva;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.register.domain.port.out.UsuarioRepositoryPort;
import apex.code.clipperBarberShop.reserva.adapters.out.persistence.JpaReservaRepository;
import apex.code.clipperBarberShop.shared.email.EmailService;
import apex.code.clipperBarberShop.shared.websocket.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio programado para gestión automática de reservas
 * - Cancela automáticamente reservas que pasaron 5 minutos después de su hora programada
 * - Envía recordatorios por email 15 minutos antes de la reserva
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaScheduledService {

    private final JpaReservaRepository reservaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final EmailService emailService;

    /**
     * Tarea programada que se ejecuta cada minuto para verificar reservas expiradas
     * Cancela automáticamente reservas en estado PENDING o CONFIRMED que:
     * - Ya pasaron su hora programada + duracion + 5 minutos de tolerancia
     * - No están eliminadas lógicamente
     */
    @Scheduled(cron = "0 */1 * * * *") // Ejecuta cada minuto
    @Transactional
    public void cancelarReservasExpiradas() {
        try {
            LocalDateTime ahora = LocalDateTime.now();
            
            // Buscar todas las reservas activas (no eliminadas)
            List<Reserva> todasLasReservas = reservaRepository.findAll();
            
            List<Reserva> reservasExpiradas = todasLasReservas.stream()
                    .filter(reserva -> !reserva.getDeleted()) // No eliminadas
                    .filter(reserva -> {
                        String status = reserva.getStatus();
                        return "PENDING".equals(status) || "CONFIRMED".equals(status);
                    })
                    .filter(reserva -> {
                        // Calcular la hora de fin de la reserva + 5 minutos de tolerancia
                        LocalDateTime finReserva = reserva.getReservationDate()
                                .plusMinutes(reserva.getDuracionMinutos() != null ? reserva.getDuracionMinutos() : 0)
                                .plusMinutes(5); // 5 minutos de tolerancia
                        
                        return ahora.isAfter(finReserva);
                    })
                    .toList();

            if (!reservasExpiradas.isEmpty()) {
                log.info("Encontradas {} reservas expiradas para cancelación automática", reservasExpiradas.size());
                
                for (Reserva reserva : reservasExpiradas) {
                    String estadoAnterior = reserva.getStatus();
                    reserva.setStatus("CANCELLED");
                    reserva.setRecordatorioEnviado(false); // ⭐ Reiniciar flag (ya no se necesita)
                    reservaRepository.save(reserva);
                    
                    log.info("Reserva ID {} cancelada automáticamente. Estado anterior: {}, Fecha: {}", 
                            reserva.getId(), estadoAnterior, reserva.getReservationDate());
                    
                    // 📧 Enviar email de cancelación automática
                    try {
                        Usuario cliente = usuarioRepository.findById(reserva.getClientId()).orElse(null);
                        if (cliente != null && cliente.getEmail() != null) {
                            emailService.sendReservaCanceladaEmail(
                                    reserva,
                                    cliente.getName() + " " + cliente.getLastName(),
                                    cliente.getEmail(),
                                    reserva.getService().getName(),
                                    reserva.getEmpresa().getNombre(),
                                    "La reserva fue cancelada automáticamente por no presentarse en el horario programado."
                            );
                        }
                    } catch (Exception e) {
                        log.error("Error enviando email de cancelación automática para reserva ID: {}", reserva.getId(), e);
                    }
                    
                    // Enviar notificaciones WebSocket
                    enviarNotificacionesCancelacionAutomatica(reserva, estadoAnterior);
                }
                
                log.info("Proceso de cancelación automática completado. {} reservas canceladas", reservasExpiradas.size());
            }
        } catch (Exception e) {
            log.error("Error en el proceso de cancelación automática de reservas", e);
        }
    }

    /**
     * Tarea programada que se ejecuta cada minuto para enviar recordatorios
     * Envía email 15 minutos antes de la reserva para estados PENDING o CONFIRMED
     */
    @Scheduled(cron = "0 */1 * * * *") // Ejecuta cada minuto
    public void enviarRecordatoriosReservas() {
        try {
            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime limiteInferior = ahora.plusMinutes(14); // 14 minutos en el futuro
            LocalDateTime limiteSuperior = ahora.plusMinutes(16); // 16 minutos en el futuro
            
            // Buscar reservas que están entre 14 y 16 minutos en el futuro
            // Y que NO hayan recibido el recordatorio aún
            List<Reserva> reservasProximas = reservaRepository.findAll().stream()
                    .filter(reserva -> !reserva.getDeleted()) // No eliminadas
                    .filter(reserva -> !reserva.getRecordatorioEnviado()) // ⭐ NO se ha enviado recordatorio
                    .filter(reserva -> {
                        String status = reserva.getStatus();
                        return "PENDING".equals(status) || "CONFIRMED".equals(status);
                    })
                    .filter(reserva -> {
                        LocalDateTime fechaReserva = reserva.getReservationDate();
                        return fechaReserva.isAfter(limiteInferior) && fechaReserva.isBefore(limiteSuperior);
                    })
                    .toList();

            if (!reservasProximas.isEmpty()) {
                log.info("Encontradas {} reservas próximas (15 min) para enviar recordatorios", reservasProximas.size());
                
                for (Reserva reserva : reservasProximas) {
                    try {
                        enviarRecordatorioReserva(reserva);
                    } catch (Exception e) {
                        log.error("Error al enviar recordatorio para reserva ID: {}", reserva.getId(), e);
                    }
                }
                
                log.info("Proceso de recordatorios completado. {} recordatorios enviados", reservasProximas.size());
            }
        } catch (Exception e) {
            log.error("Error en el proceso de envío de recordatorios", e);
        }
    }

    /**
     * Envía un recordatorio por email al cliente 15 minutos antes de su reserva
     * Marca la reserva como recordatorio enviado para evitar duplicados
     */
    private void enviarRecordatorioReserva(Reserva reserva) {
        try {
            // Obtener información del cliente
            usuarioRepository.findById(reserva.getClientId()).ifPresent(cliente -> {
                // Obtener información del empleado
                usuarioRepository.findById(reserva.getEmployeeId()).ifPresent(empleado -> {
                    String clientName = cliente.getName() + " " + cliente.getLastName();
                    String employeeName = empleado.getName() + " " + empleado.getLastName();
                    String empresaDireccion = reserva.getEmpresa().getDireccion() != null 
                            ? reserva.getEmpresa().getDireccion() 
                            : "Dirección no disponible";
                    
                    // Enviar email de recordatorio
                    emailService.sendReservaRecordatorioEmail(
                            reserva,
                            clientName,
                            cliente.getEmail(),
                            employeeName,
                            reserva.getService().getName(),
                            reserva.getEmpresa().getNombre(),
                            empresaDireccion
                    );
                    
                    // ⭐ Marcar la reserva como recordatorio enviado para evitar duplicados
                    reserva.setRecordatorioEnviado(true);
                    reservaRepository.save(reserva);
                    
                    log.info("Recordatorio enviado y marcado para reserva ID: {} - Cliente: {}", 
                            reserva.getId(), cliente.getEmail());
                });
            });
        } catch (Exception e) {
            log.error("Error al enviar recordatorio para reserva ID: {}", reserva.getId(), e);
            // No marcar como enviado si hubo error, para que se reintente
        }
    }

    /**
     * Envía notificaciones WebSocket a todos los involucrados cuando se cancela automáticamente una reserva
     */
    private void enviarNotificacionesCancelacionAutomatica(Reserva reserva, String estadoAnterior) {
        try {
            Map<String, Object> datosReserva = new HashMap<>();
            datosReserva.put("id", reserva.getId());
            datosReserva.put("empresaId", reserva.getEmpresa().getId());
            datosReserva.put("servicioNombre", reserva.getService().getName());
            datosReserva.put("fechaReserva", reserva.getReservationDate().toString());
            datosReserva.put("estadoAnterior", estadoAnterior);
            datosReserva.put("motivoCancelacion", "CANCELACION_AUTOMATICA_POR_EXPIRACION");

            // Enviar notificación de cancelación automática
            webSocketNotificationService.sendReservaCancelada(
                    reserva.getEmpresa().getId(),
                    reserva.getClientId(),
                    reserva.getEmployeeId(),
                    datosReserva
            );

            log.debug("Notificaciones de cancelación automática enviadas para reserva ID: {}", reserva.getId());
        } catch (Exception e) {
            log.error("Error al enviar notificaciones de cancelación automática para reserva ID: {}", 
                    reserva.getId(), e);
        }
    }
}
