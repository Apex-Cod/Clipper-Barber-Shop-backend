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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de reservas por parte de los CLIENTES
 * Solo puede gestionar sus propias reservas (CRUD completo)
 */
@Service
@RequiredArgsConstructor
public class ReservaClientService {
    
    private final ReservaRepositoryPort reservaRepository;
    private final ServicioRepositoryPort servicioRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final ReservaValidationService validationService;
    
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
        reservaRepository.save(reserva);
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
        reserva.setReservationDate(request.getNuevaFecha());
        reserva.setEmployeeId(request.getEmployeeId());
        reserva.setStatus("RESCHEDULED");
        
        Reserva updated = reservaRepository.save(reserva);
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
     * Lista todas las reservas del cliente
     */
    public List<ReservaResponse> listarMisReservas(String clientId) {
        return reservaRepository.findByClientIdAndDeletedFalse(clientId)
                .stream()
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
     * Lista reservas del cliente por estado
     */
    public List<ReservaResponse> listarMisReservasPorEstado(String status, String clientId) {
        return reservaRepository.findByClientIdAndStatusAndDeletedFalse(clientId, status)
                .stream()
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
                .employeeId(reserva.getEmployeeId())
                .employeeName(empleado != null ? empleado.getName() + " " + empleado.getLastName() : "N/A")
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
