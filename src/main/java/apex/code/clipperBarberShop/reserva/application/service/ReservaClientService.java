package apex.code.clipperBarberShop.reserva.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Reserva;
import apex.code.clipperBarberShop.Entities.Servicio;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.Entities.enums.ReservaStatus;
import apex.code.clipperBarberShop.register.domain.port.out.UsuarioRepositoryPort;
import apex.code.clipperBarberShop.reserva.application.dto.*;
import apex.code.clipperBarberShop.reserva.domain.exception.ReservaAccessDeniedException;
import apex.code.clipperBarberShop.reserva.domain.exception.ReservaInvalidStateException;
import apex.code.clipperBarberShop.reserva.domain.exception.ReservaNotFoundException;
import apex.code.clipperBarberShop.reserva.domain.port.out.ReservaRepositoryPort;
import apex.code.clipperBarberShop.servicio.domain.port.out.ServicioRepositoryPort;
import apex.code.clipperBarberShop.shared.websocket.service.WebSocketNotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de reservas por parte de los CLIENTES
 * Solo puede gestionar sus propias reservas (CRUD completo)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaClientService {
    
    private final ReservaRepositoryPort reservaRepository;
    private final ServicioRepositoryPort servicioRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final ReservaValidationService validationService;
    private final WebSocketNotificationService notificationService;
    private final apex.code.clipperBarberShop.shared.email.EmailService emailService;
    
    /**
     * Crea una nueva reserva para el cliente
     */
    @Transactional
    public ReservaResponse crearReserva(CrearReservaRequest request, String clientId) {
        // Validar cliente
        Usuario cliente = usuarioRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        
        if (!"CLIENT".equals(cliente.getRole())) {
            throw new IllegalArgumentException("El usuario no es un cliente");
        }
        
        // Obtener el servicio
        Servicio servicio = servicioRepository.findByIdAndDeletedFalse(request.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        
        Empresa empresa = servicio.getEmpresa();
        
        if (empresa.getDeleted()) {
            throw new IllegalArgumentException("No se pueden crear reservas en una empresa eliminada");
        }
        
        // Validaciones
        validationService.validarFechaFutura(request.getReservationDate());
        validationService.validarEmpleadoEmpresa(request.getEmployeeId(), empresa.getId());
        validationService.validarHorarioComercial(request.getReservationDate(), empresa.getId());
        validationService.validarDisponibilidad(
                request.getServiceId(), 
                request.getEmployeeId(), 
                request.getReservationDate());
        
        // Calcular precio final
        Double precioFinal = validationService.calcularPrecioFinal(servicio, request.getPromocionId());
        
        // Crear la reserva
        Reserva reserva = Reserva.builder()
                .empresa(empresa)
                .service(servicio)
                .clientId(clientId)
                .employeeId(request.getEmployeeId())
                .reservationDate(request.getReservationDate())
                .duracionMinutos(servicio.getDuration())
                .finalPrice(precioFinal)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();
        
        Reserva saved = reservaRepository.save(reserva);
        
        // � Enviar email de confirmación de reserva
        try {
            Usuario empleado = usuarioRepository.findById(saved.getEmployeeId()).orElse(null);
            String employeeName = empleado != null ? empleado.getName() + " " + empleado.getLastName() : "N/A";
            
            emailService.sendReservaConfirmacionEmail(
                    saved,
                    cliente.getName() + " " + cliente.getLastName(),
                    cliente.getEmail(),
                    employeeName,
                    servicio.getName(),
                    empresa.getNombre(),
                    empresa.getDireccion() != null ? empresa.getDireccion() : "Dirección no disponible"
            );
        } catch (Exception e) {
            // No fallar si el email falla
            log.error("Error enviando email de confirmación de reserva: {}", e.getMessage(), e);
        }
        
        // �🔔 Enviar notificación WebSocket de reserva creada
        try {
            Usuario empleado = usuarioRepository.findById(saved.getEmployeeId()).orElse(null);
            
            Map<String, Object> reservaData = new HashMap<>();
            reservaData.put("id", saved.getId());
            reservaData.put("serviceName", servicio.getName());
            reservaData.put("clientName", cliente.getName() + " " + cliente.getLastName());
            reservaData.put("employeeId", saved.getEmployeeId());
            reservaData.put("employeeName", empleado != null ? empleado.getName() + " " + empleado.getLastName() : "N/A");
            reservaData.put("reservationDate", saved.getReservationDate().toString());
            reservaData.put("finalPrice", saved.getFinalPrice());
            reservaData.put("status", saved.getStatus());
            
            notificationService.sendReservaCreada(
                    empresa.getId(),
                    clientId,
                    saved.getEmployeeId(), // Notificar al empleado asignado
                    reservaData
            );
        } catch (Exception e) {
            // No fallar si la notificación falla
            log.error("Error enviando notificación WebSocket para reserva creada: {}", e.getMessage(), e);
        }
        
        return mapToResponse(saved);
    }
    
    /**
     * Actualiza una reserva del cliente
     * Solo puede actualizar si está en estado PENDING
     */
    @Transactional
    public ReservaResponse actualizarReserva(Long reservaId, ActualizarReservaRequest request, String clientId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoCliente(reserva, clientId);
        
        // Los clientes solo pueden actualizar reservas pendientes
        if (!"PENDING".equals(reserva.getStatus())) {
            throw new ReservaInvalidStateException(
                    "Solo puedes actualizar reservas pendientes. Para cambios en reservas confirmadas, contacta a la empresa.");
        }
        
        // Obtener el servicio
        Servicio servicio = servicioRepository.findByIdAndDeletedFalse(request.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        
        // Validar que el servicio pertenezca a la misma empresa
        if (!servicio.getEmpresa().getId().equals(reserva.getEmpresa().getId())) {
            throw new IllegalArgumentException("El servicio debe pertenecer a la misma empresa");
        }
        
        // Validaciones
        validationService.validarFechaFutura(request.getReservationDate());
        validationService.validarEmpleadoEmpresa(request.getEmployeeId(), reserva.getEmpresa().getId());
        validationService.validarHorarioComercial(request.getReservationDate(), reserva.getEmpresa().getId());
        validationService.validarDisponibilidadParaReprogramacion(
                reservaId,
                request.getServiceId(), 
                request.getEmployeeId(), 
                request.getReservationDate());
        
        // Calcular nuevo precio
        Double precioFinal = validationService.calcularPrecioFinal(servicio, request.getPromocionId());
        
        // Actualizar
        reserva.setService(servicio);
        reserva.setEmployeeId(request.getEmployeeId());
        reserva.setReservationDate(request.getReservationDate());
        reserva.setDuracionMinutos(servicio.getDuration());
        reserva.setFinalPrice(precioFinal);
        
        Reserva updated = reservaRepository.save(reserva);
        
        // 🔔 Enviar notificación WebSocket de reserva actualizada
        try {
            Usuario cliente = usuarioRepository.findById(clientId).orElse(null);
            Usuario empleado = usuarioRepository.findById(updated.getEmployeeId()).orElse(null);
            
            Map<String, Object> reservaData = new HashMap<>();
            reservaData.put("id", updated.getId());
            reservaData.put("serviceName", servicio.getName());
            reservaData.put("clientName", cliente != null ? cliente.getName() + " " + cliente.getLastName() : "N/A");
            reservaData.put("employeeId", updated.getEmployeeId());
            reservaData.put("employeeName", empleado != null ? empleado.getName() + " " + empleado.getLastName() : "N/A");
            reservaData.put("reservationDate", updated.getReservationDate().toString());
            reservaData.put("finalPrice", updated.getFinalPrice());
            reservaData.put("status", updated.getStatus());
            
            notificationService.sendReservaActualizada(
                    reserva.getEmpresa().getId(),
                    clientId,
                    updated.getEmployeeId(), // Notificar al empleado
                    reservaData
            );
        } catch (Exception e) {
            log.error("Error enviando notificación WebSocket para reserva actualizada: {}", e.getMessage(), e);
        }
        
        return mapToResponse(updated);
    }
    
    /**
     * Cancela una reserva del cliente
     */
    @Transactional
    public void cancelarReserva(Long reservaId, CancelarReservaRequest request, String clientId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoCliente(reserva, clientId);
        
        if ("COMPLETED".equals(reserva.getStatus())) {
            throw new ReservaInvalidStateException("No se puede cancelar una reserva completada");
        }
        
        if ("CANCELLED".equals(reserva.getStatus())) {
            throw new ReservaInvalidStateException("La reserva ya está cancelada");
        }
        
        reserva.setStatus("CANCELLED");
        reserva.setRecordatorioEnviado(false); // Reset reminder flag for data consistency: ensures no reminders are sent for cancelled reservations
        Reserva saved = reservaRepository.save(reserva);
        
        // � Enviar email de reserva cancelada
        try {
            Usuario cliente = usuarioRepository.findById(clientId).orElse(null);
            if (cliente != null && cliente.getEmail() != null) {
                emailService.sendReservaCanceladaEmail(
                        saved,
                        cliente.getName() + " " + cliente.getLastName(),
                        cliente.getEmail(),
                        saved.getService().getName(),
                        saved.getEmpresa().getNombre(),
                        request.getMotivo()
                );
            }
        } catch (Exception e) {
            log.error("Error enviando email de reserva cancelada: {}", e.getMessage(), e);
        }
        
        // �🔔 Enviar notificación WebSocket de reserva cancelada
        
        // �🔔 Enviar notificación WebSocket de reserva cancelada
        try {
            Usuario cliente = usuarioRepository.findById(clientId).orElse(null);
            Usuario empleado = usuarioRepository.findById(saved.getEmployeeId()).orElse(null);
            
            Map<String, Object> reservaData = new HashMap<>();
            reservaData.put("id", saved.getId());
            reservaData.put("serviceName", saved.getService().getName());
            reservaData.put("clientName", cliente != null ? cliente.getName() + " " + cliente.getLastName() : "N/A");
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
                    clientId,
                    saved.getEmployeeId(), // Notificar al empleado
                    reservaData
            );
        } catch (Exception e) {
            log.error("Error enviando notificación WebSocket para reserva cancelada: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Reprograma una reserva del cliente
     * Solo si está en estado PENDING
     */
    @Transactional
    public ReservaResponse reprogramarReserva(Long reservaId, ReprogramarReservaRequest request, String clientId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoCliente(reserva, clientId);
        
        // Los clientes solo pueden reprogramar reservas pendientes
        if (!"PENDING".equals(reserva.getStatus())) {
            throw new ReservaInvalidStateException(
                    "Solo puedes reprogramar reservas pendientes. Para cambios en reservas confirmadas, contacta a la empresa.");
        }
        
        // Validaciones
        validationService.validarFechaFutura(request.getNuevaFecha());
        validationService.validarEmpleadoEmpresa(request.getEmployeeId(), reserva.getEmpresa().getId());
        validationService.validarHorarioComercial(request.getNuevaFecha(), reserva.getEmpresa().getId());
        validationService.validarDisponibilidadParaReprogramacion(
                reservaId,
                reserva.getService().getId(),
                request.getEmployeeId(), 
                request.getNuevaFecha());
        
        // Actualizar
        LocalDateTime fechaAnterior = reserva.getReservationDate(); // Guardar fecha anterior
        reserva.setReservationDate(request.getNuevaFecha());
        reserva.setEmployeeId(request.getEmployeeId());
        reserva.setStatus("RESCHEDULED");
        reserva.setRecordatorioEnviado(false); // ⭐ Reiniciar flag para nueva fecha
        
        Reserva updated = reservaRepository.save(reserva);
        
        // 📧 Enviar email de reserva reprogramada
        try {
            Usuario cliente = usuarioRepository.findById(clientId).orElse(null);
            Usuario empleado = usuarioRepository.findById(updated.getEmployeeId()).orElse(null);
            
            if (cliente != null && cliente.getEmail() != null && empleado != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                String fechaAnteriorStr = fechaAnterior.format(formatter);
                
                emailService.sendReservaReprogramadaEmail(
                        updated,
                        cliente.getName() + " " + cliente.getLastName(),
                        cliente.getEmail(),
                        empleado.getName() + " " + empleado.getLastName(),
                        updated.getService().getName(),
                        updated.getEmpresa().getNombre(),
                        fechaAnteriorStr
                );
            }
        } catch (Exception e) {
            log.error("Error enviando email de reserva reprogramada: {}", e.getMessage(), e);
        }
        
        // 🔔 Enviar notificación WebSocket de reserva reprogramada
        try {
            Usuario cliente = usuarioRepository.findById(clientId).orElse(null);
            Usuario empleado = usuarioRepository.findById(updated.getEmployeeId()).orElse(null);
            
            Map<String, Object> reservaData = new HashMap<>();
            reservaData.put("id", updated.getId());
            reservaData.put("serviceName", updated.getService().getName());
            reservaData.put("clientName", cliente != null ? cliente.getName() + " " + cliente.getLastName() : "N/A");
            reservaData.put("employeeId", updated.getEmployeeId());
            reservaData.put("employeeName", empleado != null ? empleado.getName() + " " + empleado.getLastName() : "N/A");
            reservaData.put("reservationDate", updated.getReservationDate().toString());
            reservaData.put("nuevaFecha", request.getNuevaFecha().toString());
            reservaData.put("finalPrice", updated.getFinalPrice());
            reservaData.put("status", updated.getStatus());
            
            notificationService.sendReservaReprogramada(
                    reserva.getEmpresa().getId(),
                    clientId,
                    updated.getEmployeeId(), // Notificar al empleado
                    reservaData
            );
        } catch (Exception e) {
            log.error("Error enviando notificación WebSocket para reserva reprogramada: {}", e.getMessage(), e);
        }
        
        return mapToResponse(updated);
    }
    
    /**
     * Obtiene una reserva específica del cliente
     */
    public ReservaDetalleResponse obtenerReserva(Long reservaId, String clientId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoCliente(reserva, clientId);
        
        return mapToDetalleResponse(reserva);
    }
    
    /**
     * Lista todas las reservas del cliente ordenadas por prioridad de estado y fecha
     * Orden: PENDING/CONFIRMED/RESCHEDULED primero (más próximas primero), luego COMPLETED, y CANCELLED al final
     */
    public List<ReservaResponse> listarMisReservas(String clientId) {
        return reservaRepository.findByClientIdAndDeletedFalse(clientId)
                .stream()
                .sorted(this::compararReservasPorPrioridad)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista reservas paginadas del cliente
     */
    public Page<ReservaResponse> listarMisReservasPaginadas(String clientId, Pageable pageable) {
        return reservaRepository.findByClientIdAndDeletedFalse(clientId, pageable)
                .map(this::mapToResponse);
    }
    
    /**
     * Lista reservas del cliente por estado ordenadas por fecha más próxima
     */
    public List<ReservaResponse> listarMisReservasPorEstado(String status, String clientId) {
        return reservaRepository.findByClientIdAndStatusAndDeletedFalse(clientId, status)
                .stream()
                .sorted((r1, r2) -> r1.getReservationDate().compareTo(r2.getReservationDate()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Elimina una reserva del cliente (soft delete)
     * Solo si está cancelada o completada
     */
    @Transactional
    public void eliminarReserva(Long reservaId, String clientId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoCliente(reserva, clientId);
        
        // Los clientes solo pueden eliminar reservas canceladas o completadas
        if (!"CANCELLED".equals(reserva.getStatus()) && !"COMPLETED".equals(reserva.getStatus())) {
            throw new ReservaInvalidStateException(
                    "Solo puedes eliminar reservas canceladas o completadas. " +
                    "Para cancelar una reserva activa, usa la opción de cancelar.");
        }
        
        reserva.softDelete(clientId);
        reservaRepository.save(reserva);
    }
    
    /**
     * Valida que el cliente tenga acceso a la reserva
     */
    private void validarAccesoCliente(Reserva reserva, String clientId) {
        if (!reserva.getClientId().equals(clientId)) {
            throw new ReservaAccessDeniedException("No tienes permisos para acceder a esta reserva");
        }
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
    
    /**
     * Mapea una entidad Reserva a ReservaResponse
     */
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
                .clientImageUrl(cliente != null ? cliente.getProfileImageUrl() : null)
                .employeeId(reserva.getEmployeeId())
                .employeeName(empleado != null ? empleado.getName() + " " + empleado.getLastName() : "N/A")
                .employeeImageUrl(empleado != null ? empleado.getProfileImageUrl() : null)
                .reservationDate(reserva.getReservationDate())
                .duracionMinutos(reserva.getDuracionMinutos())
                .finalPrice(reserva.getFinalPrice())
                .status(reserva.getStatus())
                .createdAt(reserva.getCreatedAt())
                .build();
    }
    
    /**
     * Mapea una entidad Reserva a ReservaDetalleResponse
     */
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
                .promocionId(reserva.getPromocion() != null ? reserva.getPromocion().getId() : null)
                .deleted(reserva.getDeleted())
                .deletedAt(reserva.getDeletedAt())
                .deletedBy(reserva.getDeletedBy())
                .build();
    }

public List<String> obtenerHorariosOcupados(String empresaId, String fecha, String empleadoId) {
    LocalDate fechaReserva = LocalDate.parse(fecha);
    LocalDateTime startOfDay = fechaReserva.atStartOfDay();
    LocalDateTime endOfDay = fechaReserva.atTime(23, 59, 59);
    
    List<String> estadosOcupados = Arrays.asList("PENDING", "CONFIRMED");
    
    List<Reserva> reservasOcupadas;
    
    if (empleadoId != null && !empleadoId.isEmpty()) {
        // Filtrar por empleado específico
        reservasOcupadas = reservaRepository.findByEmpresaIdAndEmployeeIdAndReservationDateBetweenAndStatusIn(
            Long.parseLong(empresaId), 
            empleadoId, // employeeId es String, no necesita parseo
            startOfDay,
            endOfDay,
            estadosOcupados
        );
    } else {
        // Todos los empleados de la empresa
        reservasOcupadas = reservaRepository.findByEmpresaIdAndReservationDateBetweenAndStatusIn(
            Long.parseLong(empresaId), 
            startOfDay,
            endOfDay,
            estadosOcupados
        );
    }
    
    return reservasOcupadas.stream()
        .map(reserva -> {
            LocalTime horaInicio = reserva.getReservationDate().toLocalTime();
            return horaInicio.format(DateTimeFormatter.ofPattern("HH:mm"));
        })
        .distinct()
        .sorted()
        .collect(Collectors.toList());
}
}
