package apex.code.clipperBarberShop.reserva.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Reserva;
import apex.code.clipperBarberShop.Entities.Servicio;
import apex.code.clipperBarberShop.Entities.Usuario;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de reservas por parte del OWNER
 * Puede gestionar todas las reservas de su empresa
 */
@Service
@RequiredArgsConstructor
public class ReservaOwnerService {
    
    private final ReservaRepositoryPort reservaRepository;
    private final ServicioRepositoryPort servicioRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final ReservaValidationService validationService;
    
    /**
     * Crea una nueva reserva (el owner puede crear reservas para cualquier cliente)
     */
    @Transactional
    public ReservaResponse crearReserva(CrearReservaRequest request, String clientId, String ownerId) {
        // Validar que el owner tenga acceso
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no está asociado a una empresa");
        }
        
        Empresa empresa = owner.getEmpresa();
        
        // Obtener el servicio
        Servicio servicio = servicioRepository.findByIdAndDeletedFalse(request.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        
        // Validar que el servicio pertenezca a la empresa del owner
        if (!servicio.getEmpresa().getId().equals(empresa.getId())) {
            throw new IllegalArgumentException("El servicio no pertenece a su empresa");
        }
        
        // Validar cliente
        Usuario cliente = usuarioRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        
        if (!"CLIENT".equals(cliente.getRole())) {
            throw new IllegalArgumentException("El usuario no es un cliente");
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
     * Actualiza una reserva existente
     */
    @Transactional
    public ReservaResponse actualizarReserva(Long reservaId, ActualizarReservaRequest request, String ownerId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoReserva(reserva, ownerId);
        
        // No se puede actualizar una reserva completada o cancelada
        if ("COMPLETED".equals(reserva.getStatus()) || "CANCELLED".equals(reserva.getStatus())) {
            throw new ReservaInvalidStateException(
                    "No se puede actualizar una reserva " + reserva.getStatus().toLowerCase());
        }
        
        // Obtener el servicio
        Servicio servicio = servicioRepository.findByIdAndDeletedFalse(request.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        
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
     * Confirma una reserva
     */
    @Transactional
    public ReservaResponse confirmarReserva(Long reservaId, String ownerId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoReserva(reserva, ownerId);
        
        if (!"PENDING".equals(reserva.getStatus())) {
            throw new ReservaInvalidStateException("Solo se pueden confirmar reservas pendientes");
        }
        
        reserva.setStatus("CONFIRMED");
        Reserva updated = reservaRepository.save(reserva);
        return mapToResponse(updated);
    }
    
    /**
     * Marca una reserva como completada
     */
    @Transactional
    public ReservaResponse completarReserva(Long reservaId, String ownerId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoReserva(reserva, ownerId);
        
        if ("CANCELLED".equals(reserva.getStatus())) {
            throw new ReservaInvalidStateException("No se puede completar una reserva cancelada");
        }
        
        reserva.setStatus("COMPLETED");
        Reserva updated = reservaRepository.save(reserva);
        return mapToResponse(updated);
    }
    
    /**
     * Cancela una reserva
     */
    @Transactional
    public void cancelarReserva(Long reservaId, CancelarReservaRequest request, String ownerId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoReserva(reserva, ownerId);
        
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
     * Reprograma una reserva
     */
    @Transactional
    public ReservaResponse reprogramarReserva(Long reservaId, ReprogramarReservaRequest request, String ownerId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoReserva(reserva, ownerId);
        
        if ("COMPLETED".equals(reserva.getStatus()) || "CANCELLED".equals(reserva.getStatus())) {
            throw new ReservaInvalidStateException(
                    "No se puede reprogramar una reserva " + reserva.getStatus().toLowerCase());
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
     * Obtiene una reserva por ID
     */
    public ReservaDetalleResponse obtenerReserva(Long reservaId, String ownerId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoReserva(reserva, ownerId);
        
        return mapToDetalleResponse(reserva);
    }
    
    /**
     * Lista todas las reservas de la empresa
     */
    public List<ReservaResponse> listarReservasPorEmpresa(String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no está asociado a una empresa");
        }
        
        return reservaRepository.findByEmpresaIdAndDeletedFalse(owner.getEmpresa().getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista reservas paginadas de la empresa
     */
    public Page<ReservaResponse> listarReservasPaginadas(String ownerId, Pageable pageable) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no está asociado a una empresa");
        }
        
        return reservaRepository.findByEmpresaIdAndDeletedFalse(owner.getEmpresa().getId(), pageable)
                .map(this::mapToResponse);
    }
    
    /**
     * Lista reservas por estado
     */
    public List<ReservaResponse> listarReservasPorEstado(String status, String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no está asociado a una empresa");
        }
        
        return reservaRepository.findByEmpresaIdAndStatusAndDeletedFalse(
                owner.getEmpresa().getId(), status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista reservas por empleado
     */
    public List<ReservaResponse> listarReservasPorEmpleado(String employeeId, String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no está asociado a una empresa");
        }
        
        // Validar que el empleado pertenezca a la empresa
        validationService.validarEmpleadoEmpresa(employeeId, owner.getEmpresa().getId());
        
        return reservaRepository.findByEmployeeIdAndDeletedFalse(employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista reservas en un rango de fechas
     */
    public List<ReservaResponse> listarReservasPorRangoFechas(
            LocalDateTime inicio, LocalDateTime fin, String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no está asociado a una empresa");
        }
        
        return reservaRepository.findByEmpresaIdAndReservationDateBetweenAndDeletedFalse(
                owner.getEmpresa().getId(), inicio, fin)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Elimina una reserva (soft delete)
     */
    @Transactional
    public void eliminarReserva(Long reservaId, String ownerId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        validarAccesoReserva(reserva, ownerId);
        
        reserva.softDelete(ownerId);
        reservaRepository.save(reserva);
    }
    
    /**
     * Valida que el owner tenga acceso a la reserva
     */
    private void validarAccesoReserva(Reserva reserva, String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (owner.getEmpresa() == null || 
            !owner.getEmpresa().getId().equals(reserva.getEmpresa().getId())) {
            throw new ReservaAccessDeniedException("No tiene permisos para acceder a esta reserva");
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
}
