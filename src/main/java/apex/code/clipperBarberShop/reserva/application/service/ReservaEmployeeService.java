package apex.code.clipperBarberShop.reserva.application.service;

import apex.code.clipperBarberShop.Entities.Reserva;
import apex.code.clipperBarberShop.Entities.Servicio;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.Entities.enums.ReservaStatus;
import apex.code.clipperBarberShop.reserva.application.dto.*;
import apex.code.clipperBarberShop.reserva.domain.port.out.ReservaRepositoryPort;
import apex.code.clipperBarberShop.register.domain.port.out.UsuarioRepositoryPort;
import apex.code.clipperBarberShop.reserva.domain.exception.ReservaNotFoundException;
import apex.code.clipperBarberShop.reserva.domain.exception.ReservaInvalidStateException;
import apex.code.clipperBarberShop.shared.email.EmailService;
import apex.code.clipperBarberShop.shared.websocket.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de reservas por parte de los EMPLEADOS
 * Los empleados pueden ver y gestionar las reservas asignadas a ellos
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReservaEmployeeService {

    private final ReservaRepositoryPort reservaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final WebSocketNotificationService notificationService;
    private final EmailService emailService;

    /**
     * Lista todas las reservas asignadas al empleado ordenadas por prioridad de estado y fecha
     */
    public List<ReservaResponse> listarMisReservas(String employeeId) {
        List<Reserva> reservas = reservaRepository.findByEmployeeIdAndDeletedFalse(employeeId);
        return reservas.stream()
                .sorted(this::compararReservasPorPrioridad)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista reservas del empleado paginadas
     */
    public Page<ReservaResponse> listarMisReservasPaginadas(String employeeId, Pageable pageable) {
        // Nota: Este método requiere una implementación paginada en el repository
        // Por ahora usamos la lista completa y la convertimos a página
        List<Reserva> reservas = reservaRepository.findByEmployeeIdAndDeletedFalse(employeeId);
        return reservas.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList())
                .stream()
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .collect(Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> new org.springframework.data.domain.PageImpl<>(list, pageable, reservas.size())
                ));
    }

    /**
     * Lista reservas del empleado por estado ordenadas por fecha
     */
    public List<ReservaResponse> listarMisReservasPorEstado(String status, String employeeId) {
        List<Reserva> reservas = reservaRepository.findByEmployeeIdAndDeletedFalse(employeeId);
        return reservas.stream()
                .filter(reserva -> status.equals(reserva.getStatus()))
                .sorted((r1, r2) -> r1.getReservationDate().compareTo(r2.getReservationDate()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista reservas del empleado por fecha
     */
    public List<ReservaResponse> listarReservasPorFecha(String employeeId, String fecha) {
        LocalDate fechaReserva = LocalDate.parse(fecha);
        LocalDateTime startOfDay = fechaReserva.atStartOfDay();
        LocalDateTime endOfDay = fechaReserva.atTime(23, 59, 59);
        
        List<Reserva> todasLasReservas = reservaRepository.findByEmployeeIdAndDeletedFalse(employeeId);
        
        return todasLasReservas.stream()
                .filter(reserva -> reserva.getReservationDate().isAfter(startOfDay) && 
                                 reserva.getReservationDate().isBefore(endOfDay))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los detalles de una reserva específica asignada al empleado
     */
    public ReservaDetalleResponse obtenerReserva(Long reservaId, String employeeId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoEmpleado(reserva, employeeId);
        
        return mapToDetalleResponse(reserva);
    }

    /**
     * Confirma una reserva (cambia de PENDING a CONFIRMED)
     */
    @Transactional
    public ReservaResponse confirmarReserva(Long reservaId, String employeeId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoEmpleado(reserva, employeeId);
        
        if (!"PENDING".equals(reserva.getStatus())) {
            throw new ReservaInvalidStateException("Solo se pueden confirmar reservas pendientes");
        }
        
        reserva.setStatus("CONFIRMED");
        
        Reserva saved = reservaRepository.save(reserva);
        
        // 🔔 Enviar notificación WebSocket de reserva confirmada
        try {
            Usuario clienteObj = usuarioRepository.findById(reserva.getClientId()).orElse(null);
            Usuario empleado = usuarioRepository.findById(employeeId).orElse(null);
            
            Map<String, Object> reservaData = new HashMap<>();
            reservaData.put("id", saved.getId());
            reservaData.put("serviceName", saved.getService().getName());
            reservaData.put("clientName", clienteObj != null ? clienteObj.getName() + " " + clienteObj.getLastName() : "N/A");
            reservaData.put("employeeId", saved.getEmployeeId());
            reservaData.put("employeeName", empleado != null ? empleado.getName() + " " + empleado.getLastName() : "N/A");
            reservaData.put("reservationDate", saved.getReservationDate().toString());
            reservaData.put("finalPrice", saved.getFinalPrice());
            reservaData.put("status", saved.getStatus());
            
            notificationService.sendReservaConfirmada(
                    reserva.getEmpresa().getId(),
                    reserva.getClientId(),
                    reservaData
            );
        } catch (Exception e) {
            log.error("Error enviando notificación WebSocket para reserva confirmada: {}", e.getMessage(), e);
        }
        
        return mapToResponse(saved);
    }

    /**
     * Completa una reserva (cambia de CONFIRMED a COMPLETED)
     */
    @Transactional
    public ReservaResponse completarReserva(Long reservaId, String employeeId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoEmpleado(reserva, employeeId);
        
        if (!"CONFIRMED".equals(reserva.getStatus())) {
            throw new ReservaInvalidStateException("Solo se pueden completar reservas confirmadas");
        }
        
        reserva.setStatus("COMPLETED");
        reserva.setRecordatorioEnviado(false); // ⭐ Reiniciar flag (ya no se necesita)
        
        Reserva saved = reservaRepository.save(reserva);
        
        // � Enviar email de reserva completada
        try {
            Usuario clienteObj = usuarioRepository.findById(reserva.getClientId()).orElse(null);
            Usuario empleado = usuarioRepository.findById(employeeId).orElse(null);
            
            if (clienteObj != null && clienteObj.getEmail() != null && empleado != null) {
                emailService.sendReservaCompletadaEmail(
                        saved,
                        clienteObj.getName() + " " + clienteObj.getLastName(),
                        clienteObj.getEmail(),
                        empleado.getName() + " " + empleado.getLastName(),
                        saved.getService().getName(),
                        saved.getEmpresa().getNombre()
                );
            }
        } catch (Exception e) {
            log.error("Error enviando email de reserva completada: {}", e.getMessage(), e);
        }
        
        // �🔔 Enviar notificación WebSocket de reserva completada
        try {
            Usuario clienteObj = usuarioRepository.findById(reserva.getClientId()).orElse(null);
            Usuario empleado = usuarioRepository.findById(employeeId).orElse(null);
            
            Map<String, Object> reservaData = new HashMap<>();
            reservaData.put("id", saved.getId());
            reservaData.put("serviceName", saved.getService().getName());
            reservaData.put("clientName", clienteObj != null ? clienteObj.getName() + " " + clienteObj.getLastName() : "N/A");
            reservaData.put("employeeId", saved.getEmployeeId());
            reservaData.put("employeeName", empleado != null ? empleado.getName() + " " + empleado.getLastName() : "N/A");
            reservaData.put("reservationDate", saved.getReservationDate().toString());
            reservaData.put("finalPrice", saved.getFinalPrice());
            reservaData.put("status", saved.getStatus());
            
            notificationService.sendReservaCompletada(
                    reserva.getEmpresa().getId(),
                    reserva.getClientId(),
                    reservaData
            );
        } catch (Exception e) {
            log.error("Error enviando notificación WebSocket para reserva completada: {}", e.getMessage(), e);
        }
        
        return mapToResponse(saved);
    }

    /**
     * Cancela una reserva desde el punto de vista del empleado
     */
    @Transactional
    public ReservaResponse cancelarReserva(Long reservaId, CancelarReservaRequest request, String employeeId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoEmpleado(reserva, employeeId);
        
        if ("COMPLETED".equals(reserva.getStatus())) {
            throw new ReservaInvalidStateException("No se pueden cancelar reservas completadas");
        }
        
        reserva.setStatus("CANCELLED");
        reserva.setRecordatorioEnviado(false); // ⭐ Reiniciar flag (ya no se necesita)
        // La entidad Reserva no tiene campo cancellationReason, se podría agregar si es necesario
        
        Reserva saved = reservaRepository.save(reserva);
        
        // � Enviar email de reserva cancelada
        try {
            Usuario clienteObj = usuarioRepository.findById(reserva.getClientId()).orElse(null);
            
            if (clienteObj != null && clienteObj.getEmail() != null) {
                emailService.sendReservaCanceladaEmail(
                        saved,
                        clienteObj.getName() + " " + clienteObj.getLastName(),
                        clienteObj.getEmail(),
                        saved.getService().getName(),
                        saved.getEmpresa().getNombre(),
                        request.getMotivo()
                );
            }
        } catch (Exception e) {
            log.error("Error enviando email de reserva cancelada: {}", e.getMessage(), e);
        }
        
        // �🔔 Enviar notificación WebSocket de reserva cancelada
        try {
            Usuario clienteObj = usuarioRepository.findById(reserva.getClientId()).orElse(null);
            Usuario empleado = usuarioRepository.findById(employeeId).orElse(null);
            
            Map<String, Object> reservaData = new HashMap<>();
            reservaData.put("id", saved.getId());
            reservaData.put("serviceName", saved.getService().getName());
            reservaData.put("clientName", clienteObj != null ? clienteObj.getName() + " " + clienteObj.getLastName() : "N/A");
            reservaData.put("employeeId", saved.getEmployeeId());
            reservaData.put("employeeName", empleado != null ? empleado.getName() + " " + empleado.getLastName() : "N/A");
            reservaData.put("reservationDate", saved.getReservationDate().toString());
            reservaData.put("finalPrice", saved.getFinalPrice());
            reservaData.put("status", saved.getStatus());
            if (request.getMotivo() != null) {
                reservaData.put("motivo", request.getMotivo());
            }
            
            notificationService.sendReservaCancelada(
                    reserva.getEmpresa().getId(),
                    reserva.getClientId(),
                    employeeId,
                    reservaData
            );
        } catch (Exception e) {
            log.error("Error enviando notificación WebSocket para reserva cancelada: {}", e.getMessage(), e);
        }
        
        return mapToResponse(saved);
    }

    /**
     * Obtiene el horario de trabajo del empleado para una fecha específica
     */
    public List<String> obtenerMiHorario(String employeeId, String fecha) {
        LocalDate fechaReserva = LocalDate.parse(fecha);
        LocalDateTime startOfDay = fechaReserva.atStartOfDay();
        LocalDateTime endOfDay = fechaReserva.atTime(23, 59, 59);
        
        List<String> estadosActivos = Arrays.asList("PENDING", "CONFIRMED");
        
        List<Reserva> misReservas = reservaRepository.findByEmpresaIdAndEmployeeIdAndReservationDateBetweenAndStatusIn(
            obtenerEmpresaIdDelEmpleado(employeeId), 
            employeeId,
            startOfDay,
            endOfDay,
            estadosActivos
        );
        
        return misReservas.stream()
                .map(reserva -> reserva.getReservationDate().format(DateTimeFormatter.ofPattern("HH:mm")))
                .sorted()
                .collect(Collectors.toList());
    }

    // Métodos auxiliares privados
    private void validarAccesoEmpleado(Reserva reserva, String employeeId) {
        if (!employeeId.equals(reserva.getEmployeeId())) {
            throw new IllegalArgumentException("No tienes acceso a esta reserva");
        }
    }

    private Long obtenerEmpresaIdDelEmpleado(String employeeId) {
        Usuario empleado = usuarioRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        return empleado.getEmpresa().getId();
    }

    /**
     * Comparador personalizado para ordenar reservas por prioridad
     * Orden: 
     * 1. Estados activos (PENDING, CONFIRMED, RESCHEDULED) por fecha más próxima
     * 2. COMPLETED por fecha más reciente
     * 3. CANCELLED al final por fecha más reciente
     */
    private int compararReservasPorPrioridad(Reserva r1, Reserva r2) {
        int prioridad1 = obtenerPrioridadEstado(r1.getStatus());
        int prioridad2 = obtenerPrioridadEstado(r2.getStatus());
        
        // Primero comparar por prioridad de estado
        if (prioridad1 != prioridad2) {
            return Integer.compare(prioridad1, prioridad2);
        }
        
        // Si tienen la misma prioridad, ordenar por fecha
        // Para estados activos (prioridad 1): fecha más próxima primero
        // Para completadas y canceladas: más recientes primero
        if (prioridad1 == 1) {
            return r1.getReservationDate().compareTo(r2.getReservationDate());
        } else {
            return r2.getReservationDate().compareTo(r1.getReservationDate());
        }
    }
    
    /**
     * Obtiene la prioridad numérica del estado para ordenamiento
     * 1 = Activas (PENDING, CONFIRMED, RESCHEDULED)
     * 2 = Completadas (COMPLETED)
     * 3 = Canceladas (CANCELLED)
     */
    private int obtenerPrioridadEstado(String status) {
        if (status == null) {
            return 3;
        }
        
        switch (status) {
            case "PENDING":
            case "CONFIRMED":
            case "RESCHEDULED":
                return 1;
            case "COMPLETED":
                return 2;
            case "CANCELLED":
                return 3;
            default:
                return 3;
        }
    }

    private ReservaResponse mapToResponse(Reserva reserva) {
        Usuario cliente = usuarioRepository.findById(reserva.getClientId()).orElse(null);
        Usuario empleado = usuarioRepository.findById(reserva.getEmployeeId()).orElse(null);
        
        return ReservaResponse.builder()
                .id(reserva.getId())
                .empresaId(reserva.getEmpresa().getId())
                .empresaNombre(reserva.getEmpresa().getNombre())
                .serviceId(reserva.getService().getId())
                .serviceName(reserva.getService().getName())
                .clientId(reserva.getClientId())
                .clientName(cliente != null ? cliente.getName() + " " + cliente.getLastName() : "N/A")
                .employeeId(reserva.getEmployeeId())
                .employeeName(empleado != null ? empleado.getName() + " " + empleado.getLastName() : "N/A")
                .reservationDate(reserva.getReservationDate())
                .duracionMinutos(reserva.getDuracionMinutos())
                .finalPrice(reserva.getFinalPrice())
                .status(reserva.getStatus())
                .createdAt(reserva.getCreatedAt())
                .build();
    }

    private ReservaDetalleResponse mapToDetalleResponse(Reserva reserva) {
        Usuario cliente = usuarioRepository.findById(reserva.getClientId()).orElse(null);
        Usuario empleado = usuarioRepository.findById(reserva.getEmployeeId()).orElse(null);
        
        return ReservaDetalleResponse.builder()
                .id(reserva.getId())
                .empresaId(reserva.getEmpresa().getId())
                .empresaNombre(reserva.getEmpresa().getNombre())
                .serviceId(reserva.getService().getId())
                .serviceName(reserva.getService().getName())
                .serviceDescription(reserva.getService().getDescription())
                .servicePrice(reserva.getService().getPrice())
                .serviceDuration(reserva.getService().getDuration())
                .clientId(reserva.getClientId())
                .clientName(cliente != null ? cliente.getName() + " " + cliente.getLastName() : "N/A")
                .clientEmail(cliente != null ? cliente.getEmail() : "N/A")
                .employeeId(reserva.getEmployeeId())
                .employeeName(empleado != null ? empleado.getName() + " " + empleado.getLastName() : "N/A")
                .employeeEmail(empleado != null ? empleado.getEmail() : "N/A")
                .reservationDate(reserva.getReservationDate())
                .duracionMinutos(reserva.getDuracionMinutos())
                .finalPrice(reserva.getFinalPrice())
                .status(reserva.getStatus())
                .createdAt(reserva.getCreatedAt())
                .deleted(reserva.getDeleted())
                .build();
    }
}
