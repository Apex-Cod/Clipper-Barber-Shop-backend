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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de reservas por parte de los EMPLEADOS
 * Los empleados pueden ver y gestionar las reservas asignadas a ellos
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservaEmployeeService {

    private final ReservaRepositoryPort reservaRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    /**
     * Lista todas las reservas asignadas al empleado
     */
    public List<ReservaResponse> listarMisReservas(String employeeId) {
        List<Reserva> reservas = reservaRepository.findByEmployeeIdAndDeletedFalse(employeeId);
        return reservas.stream()
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
     * Lista reservas del empleado por estado
     */
    public List<ReservaResponse> listarMisReservasPorEstado(String status, String employeeId) {
        List<Reserva> reservas = reservaRepository.findByEmployeeIdAndDeletedFalse(employeeId);
        return reservas.stream()
                .filter(reserva -> status.equals(reserva.getStatus()))
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
        
        Reserva saved = reservaRepository.save(reserva);
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
        // La entidad Reserva no tiene campo cancellationReason, se podría agregar si es necesario
        
        Reserva saved = reservaRepository.save(reserva);
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
