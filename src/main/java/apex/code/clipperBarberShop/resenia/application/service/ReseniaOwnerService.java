package apex.code.clipperBarberShop.resenia.application.service;

import apex.code.clipperBarberShop.Entities.Resenia;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.register.domain.port.out.UsuarioRepositoryPort;
import apex.code.clipperBarberShop.resenia.application.dto.EstadisticasEmpleadoResponse;
import apex.code.clipperBarberShop.resenia.application.dto.EstadisticasEmpresaResponse;
import apex.code.clipperBarberShop.resenia.application.dto.ReseniaResponse;
import apex.code.clipperBarberShop.resenia.domain.exception.ReseniaAccessDeniedException;
import apex.code.clipperBarberShop.resenia.domain.exception.ReseniaNotFoundException;
import apex.code.clipperBarberShop.resenia.domain.port.out.ReseniaRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de reseñas por parte del OWNER
 */
@Service
@RequiredArgsConstructor
public class ReseniaOwnerService {
    
    private final ReseniaRepositoryPort reseniaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    
    /**
     * Obtiene una reseña específica
     */
    public ReseniaResponse obtenerResenia(Long reseniaId, String ownerId) {
        Resenia resenia = reseniaRepository.findByIdAndDeletedFalse(reseniaId)
                .orElseThrow(() -> new ReseniaNotFoundException(reseniaId));
        
        // Validar que el owner es dueño de la empresa
        validarAccesoEmpresa(resenia.getEmpresa().getId(), ownerId);
        
        return mapToResponse(resenia);
    }
    
    /**
     * Lista todas las reseñas de la empresa
     */
    public List<ReseniaResponse> listarReseniasPorEmpresa(String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no tiene una empresa asociada");
        }
        
        return reseniaRepository.findByEmpresaIdAndDeletedFalse(owner.getEmpresa().getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista reseñas paginadas de la empresa
     */
    public Page<ReseniaResponse> listarReseniasPaginadas(String ownerId, Pageable pageable) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no tiene una empresa asociada");
        }
        
        return reseniaRepository.findByEmpresaIdAndDeletedFalse(owner.getEmpresa().getId(), pageable)
                .map(this::mapToResponse);
    }
    
    /**
     * Lista reseñas de un empleado específico
     */
    public List<ReseniaResponse> listarReseniasPorEmpleado(String empleadoId, String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner no encontrado"));
        
        // Validar que el empleado pertenece a la empresa del owner
        Usuario empleado = usuarioRepository.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        
        if (empleado.getEmpresa() == null || 
            !empleado.getEmpresa().getId().equals(owner.getEmpresa().getId())) {
            throw new ReseniaAccessDeniedException("El empleado no pertenece a tu empresa");
        }
        
        return reseniaRepository.findByEmpleadoIdAndDeletedFalse(empleadoId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene estadísticas de la empresa
     */
    public EstadisticasEmpresaResponse obtenerEstadisticasEmpresa(String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no tiene una empresa asociada");
        }
        
        Long empresaId = owner.getEmpresa().getId();
        List<Resenia> resenias = reseniaRepository.findByEmpresaIdAndDeletedFalse(empresaId);
        
        return EstadisticasEmpresaResponse.builder()
                .empresaId(empresaId)
                .empresaNombre(owner.getEmpresa().getNombre())
                .totalResenias((long) resenias.size())
                .promedioCalificacion(reseniaRepository.getAverageCalificacionByEmpresaId(empresaId))
                .resenias5Estrellas(resenias.stream().filter(r -> r.getCalificacionServicio() == 5).count())
                .resenias4Estrellas(resenias.stream().filter(r -> r.getCalificacionServicio() == 4).count())
                .resenias3Estrellas(resenias.stream().filter(r -> r.getCalificacionServicio() == 3).count())
                .resenias2Estrellas(resenias.stream().filter(r -> r.getCalificacionServicio() == 2).count())
                .resenias1Estrella(resenias.stream().filter(r -> r.getCalificacionServicio() == 1).count())
                .build();
    }
    
    /**
     * Obtiene estadísticas de un empleado
     */
    public EstadisticasEmpleadoResponse obtenerEstadisticasEmpleado(String empleadoId, String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner no encontrado"));
        
        // Validar que el empleado pertenece a la empresa del owner
        Usuario empleado = usuarioRepository.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        
        if (empleado.getEmpresa() == null || 
            !empleado.getEmpresa().getId().equals(owner.getEmpresa().getId())) {
            throw new ReseniaAccessDeniedException("El empleado no pertenece a tu empresa");
        }
        
        List<Resenia> resenias = reseniaRepository.findByEmpleadoIdAndDeletedFalse(empleadoId);
        
        return EstadisticasEmpleadoResponse.builder()
                .empleadoId(empleadoId)
                .empleadoNombre(empleado.getName() + " " + empleado.getLastName())
                .totalResenias((long) resenias.size())
                .promedioCalificacion(reseniaRepository.getAverageCalificacionByEmpleadoId(empleadoId))
                .build();
    }
    
    /**
     * Elimina una reseña (soft delete)
     */
    @Transactional
    public void eliminarResenia(Long reseniaId, String ownerId) {
        Resenia resenia = reseniaRepository.findByIdAndDeletedFalse(reseniaId)
                .orElseThrow(() -> new ReseniaNotFoundException(reseniaId));
        
        // Validar que el owner es dueño de la empresa
        validarAccesoEmpresa(resenia.getEmpresa().getId(), ownerId);
        
        resenia.softDelete(ownerId);
        reseniaRepository.save(resenia);
    }
    
    /**
     * Valida que el owner tiene acceso a la empresa
     */
    private void validarAccesoEmpresa(Long empresaId, String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner no encontrado"));
        
        if (owner.getEmpresa() == null || !owner.getEmpresa().getId().equals(empresaId)) {
            throw new ReseniaAccessDeniedException("No tienes permiso para acceder a las reseñas de esta empresa");
        }
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
