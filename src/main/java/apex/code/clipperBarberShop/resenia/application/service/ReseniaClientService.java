package apex.code.clipperBarberShop.resenia.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Reserva;
import apex.code.clipperBarberShop.Entities.Resenia;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.register.domain.port.out.UsuarioRepositoryPort;
import apex.code.clipperBarberShop.resenia.application.dto.*;
import apex.code.clipperBarberShop.resenia.domain.exception.ReseniaAccessDeniedException;
import apex.code.clipperBarberShop.resenia.domain.exception.ReseniaAlreadyExistsException;
import apex.code.clipperBarberShop.resenia.domain.exception.ReseniaNotFoundException;
import apex.code.clipperBarberShop.resenia.domain.exception.ReservaNotCompletedException;
import apex.code.clipperBarberShop.resenia.domain.port.out.ReseniaRepositoryPort;
import apex.code.clipperBarberShop.reserva.domain.exception.ReservaNotFoundException;
import apex.code.clipperBarberShop.reserva.domain.port.out.ReservaRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de reseñas por parte de los CLIENTES
 */
@Service
@RequiredArgsConstructor
public class ReseniaClientService {
    
    private final ReseniaRepositoryPort reseniaRepository;
    private final ReservaRepositoryPort reservaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    
    /**
     * Crea una nueva reseña para una reserva completada
     */
    @Transactional
    public ReseniaResponse crearResenia(CrearReseniaRequest request, String clientId) {
        // Validar que la reserva existe
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(request.getReservaId())
                .orElseThrow(() -> new ReservaNotFoundException(request.getReservaId()));
        
        // Validar que el cliente es el dueño de la reserva
        if (!reserva.getClientId().equals(clientId)) {
            throw new ReseniaAccessDeniedException("No tienes permiso para crear una reseña para esta reserva");
        }
        
        // Validar que la reserva está completada
        if (!"COMPLETED".equals(reserva.getStatus())) {
            throw new ReservaNotCompletedException(request.getReservaId());
        }
        
        // Validar que no existe ya una reseña para esta reserva
        if (reseniaRepository.existsByReservaIdAndDeletedFalse(request.getReservaId())) {
            throw new ReseniaAlreadyExistsException(request.getReservaId());
        }
        
        // Crear la reseña
        Resenia resenia = Resenia.builder()
                .reserva(reserva)
                .clienteId(clientId)
                .empleadoId(reserva.getEmployeeId())
                .empresa(reserva.getEmpresa())
                .calificacionServicio(request.getCalificacionServicio())
                .calificacionEmpleado(request.getCalificacionEmpleado())
                .comentario(request.getComentario())
                .fecha(LocalDateTime.now())
                .deleted(false)
                .build();
        
        Resenia saved = reseniaRepository.save(resenia);
        return mapToResponse(saved);
    }
    
    /**
     * Actualiza una reseña existente
     */
    @Transactional
    public ReseniaResponse actualizarResenia(Long reseniaId, ActualizarReseniaRequest request, String clientId) {
        Resenia resenia = reseniaRepository.findByIdAndDeletedFalse(reseniaId)
                .orElseThrow(() -> new ReseniaNotFoundException(reseniaId));
        
        // Validar que el cliente es el dueño de la reseña
        if (!resenia.getClienteId().equals(clientId)) {
            throw new ReseniaAccessDeniedException("No tienes permiso para actualizar esta reseña");
        }
        
        // Actualizar campos
        resenia.setCalificacionServicio(request.getCalificacionServicio());
        resenia.setCalificacionEmpleado(request.getCalificacionEmpleado());
        resenia.setComentario(request.getComentario());
        
        Resenia updated = reseniaRepository.save(resenia);
        return mapToResponse(updated);
    }
    
    /**
     * Obtiene una reseña específica
     */
    public ReseniaResponse obtenerResenia(Long reseniaId, String clientId) {
        Resenia resenia = reseniaRepository.findByIdAndDeletedFalse(reseniaId)
                .orElseThrow(() -> new ReseniaNotFoundException(reseniaId));
        
        // Validar que el cliente es el dueño de la reseña
        if (!resenia.getClienteId().equals(clientId)) {
            throw new ReseniaAccessDeniedException("No tienes permiso para ver esta reseña");
        }
        
        return mapToResponse(resenia);
    }
    
    /**
     * Lista todas las reseñas del cliente
     */
    public List<ReseniaResponse> listarMisResenias(String clientId) {
        return reseniaRepository.findByClienteIdAndDeletedFalse(clientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Elimina una reseña (soft delete)
     */
    @Transactional
    public void eliminarResenia(Long reseniaId, String clientId) {
        Resenia resenia = reseniaRepository.findByIdAndDeletedFalse(reseniaId)
                .orElseThrow(() -> new ReseniaNotFoundException(reseniaId));
        
        // Validar que el cliente es el dueño de la reseña
        if (!resenia.getClienteId().equals(clientId)) {
            throw new ReseniaAccessDeniedException("No tienes permiso para eliminar esta reseña");
        }
        
        resenia.softDelete(clientId);
        reseniaRepository.save(resenia);
    }
    
    /**
     * Obtiene la reseña de una reserva específica
     */
    public ReseniaResponse obtenerReseniaPorReserva(Long reservaId, String clientId) {
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        
        // Validar que el cliente es el dueño de la reserva
        if (!reserva.getClientId().equals(clientId)) {
            throw new ReseniaAccessDeniedException("No tienes permiso para ver la reseña de esta reserva");
        }
        
        Resenia resenia = reseniaRepository.findByReservaIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new ReseniaNotFoundException(reservaId));
        
        return mapToResponse(resenia);
    }
    
    /**
     * Mapea una entidad Resenia a ReseniaResponse
     */
    private ReseniaResponse mapToResponse(Resenia resenia) {
        Usuario cliente = usuarioRepository.findById(resenia.getClienteId()).orElse(null);
        Usuario empleado = usuarioRepository.findById(resenia.getEmpleadoId()).orElse(null);
        
        return ReseniaResponse.builder()
                .id(resenia.getId())
                .reservaId(resenia.getReserva().getId())
                .empresaId(resenia.getEmpresa().getId())
                .empresaNombre(resenia.getEmpresa().getNombre())
                .clienteId(resenia.getClienteId())
                .clienteNombre(cliente != null ? cliente.getName() + " " + cliente.getLastName() : "N/A")
                .empleadoId(resenia.getEmpleadoId())
                .empleadoNombre(empleado != null ? empleado.getName() + " " + empleado.getLastName() : "N/A")
                .servicioId(resenia.getReserva().getService().getId())
                .servicioNombre(resenia.getReserva().getService().getName())
                .calificacionServicio(resenia.getCalificacionServicio())
                .calificacionEmpleado(resenia.getCalificacionEmpleado())
                .comentario(resenia.getComentario())
                .fecha(resenia.getFecha())
                .reservationDate(resenia.getReserva().getReservationDate())
                .build();
    }
}
