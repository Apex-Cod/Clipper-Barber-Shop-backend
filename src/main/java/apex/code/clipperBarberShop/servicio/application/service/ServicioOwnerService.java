package apex.code.clipperBarberShop.servicio.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Servicio;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.empresa.domain.port.out.StoragePort;
import apex.code.clipperBarberShop.register.domain.port.out.EmpresaRepositoryPort;
import apex.code.clipperBarberShop.register.domain.port.out.UsuarioRepositoryPort;
import apex.code.clipperBarberShop.servicio.application.dto.ActualizarServicioRequest;
import apex.code.clipperBarberShop.servicio.application.dto.ServicioRequest;
import apex.code.clipperBarberShop.servicio.application.dto.ServicioResponse;
import apex.code.clipperBarberShop.servicio.domain.exception.ServicioAccessDeniedException;
import apex.code.clipperBarberShop.servicio.domain.exception.ServicioNotFoundException;
import apex.code.clipperBarberShop.servicio.domain.port.out.ServicioRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de servicios por parte del OWNER
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServicioOwnerService {
    
    private final ServicioRepositoryPort servicioRepository;
    private final EmpresaRepositoryPort empresaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final StoragePort storagePort;
    
    /**
     * Crea un nuevo servicio con imagen opcional
     */
    @Transactional
    public ServicioResponse crearServicio(ServicioRequest request, MultipartFile image, String ownerId) {
        // Obtener el owner y verificar permisos
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (owner.getEmpresa() == null || !owner.getEmpresa().getId().equals(request.getEmpresaId())) {
            throw new ServicioAccessDeniedException("No tiene permisos para crear servicios en esta empresa");
        }
        
        Empresa empresa = empresaRepository.findById(request.getEmpresaId())
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
        if (empresa.getDeleted()) {
            throw new IllegalArgumentException("No se pueden crear servicios en una empresa eliminada");
        }
        
        // Subir imagen si se proporciona
        String imageUrl = request.getImageUrl();
        if (image != null && !image.isEmpty()) {
            try {
                String fileName = "servicio-" + System.currentTimeMillis() + "-" + image.getOriginalFilename();
                imageUrl = storagePort.uploadServiceImage(fileName, image);
                log.info("Imagen subida exitosamente: {}", imageUrl);
            } catch (Exception e) {
                log.error("Error al subir imagen del servicio", e);
                throw new IllegalStateException("Error al subir la imagen: " + e.getMessage());
            }
        }
        
        Servicio servicio = Servicio.builder()
                .empresa(empresa)
                .name(request.getName())
                .description(request.getDescription())
                .duration(request.getDuration())
                .price(request.getPrice())
                .categoria(request.getCategoria())
                .publicoObjetivo(request.getPublicoObjetivo())
                .imageUrl(imageUrl)
                .deleted(false)
                .build();
        
        Servicio saved = servicioRepository.save(servicio);
        return mapToResponse(saved);
    }
    
    /**
     * Crea un nuevo servicio (versión sin imagen para compatibilidad)
     */
    @Transactional
    public ServicioResponse crearServicio(ServicioRequest request, String ownerId) {
        return crearServicio(request, null, ownerId);
    }
    
    /**
     * Actualiza un servicio existente con imagen opcional
     */
    @Transactional
    public ServicioResponse actualizarServicio(Long servicioId, ActualizarServicioRequest request, 
                                               MultipartFile image, String ownerId) {
        Servicio servicio = servicioRepository.findByIdAndDeletedFalse(servicioId)
                .orElseThrow(() -> new ServicioNotFoundException(servicioId));
        
        validarAccesoServicio(servicio, ownerId);
        
        // Subir nueva imagen si se proporciona
        if (image != null && !image.isEmpty()) {
            try {
                String fileName = "servicio-" + servicioId + "-" + System.currentTimeMillis() + "-" + image.getOriginalFilename();
                String imageUrl = storagePort.uploadServiceImage(fileName, image);
                servicio.setImageUrl(imageUrl);
                log.info("Imagen actualizada exitosamente: {}", imageUrl);
            } catch (Exception e) {
                log.error("Error al subir imagen del servicio", e);
                throw new IllegalStateException("Error al subir la imagen: " + e.getMessage());
            }
        } else if (request.getImageUrl() != null) {
            // Si no hay imagen pero sí URL, actualizar la URL
            servicio.setImageUrl(request.getImageUrl());
        }
        
        servicio.setName(request.getName());
        servicio.setDescription(request.getDescription());
        servicio.setDuration(request.getDuration());
        servicio.setPrice(request.getPrice());
        servicio.setCategoria(request.getCategoria());
        servicio.setPublicoObjetivo(request.getPublicoObjetivo());
        
        Servicio updated = servicioRepository.save(servicio);
        return mapToResponse(updated);
    }
    
    /**
     * Actualiza un servicio existente (versión sin imagen para compatibilidad)
     */
    @Transactional
    public ServicioResponse actualizarServicio(Long servicioId, ActualizarServicioRequest request, String ownerId) {
        return actualizarServicio(servicioId, request, null, ownerId);
    }
    
    /**
     * Obtiene un servicio por su ID
     */
    public ServicioResponse obtenerServicio(Long servicioId, String ownerId) {
        Servicio servicio = servicioRepository.findByIdAndDeletedFalse(servicioId)
                .orElseThrow(() -> new ServicioNotFoundException(servicioId));
        
        validarAccesoServicio(servicio, ownerId);
        
        return mapToResponse(servicio);
    }
    
    /**
     * Lista todos los servicios de la empresa del owner
     */
    public List<ServicioResponse> listarServiciosPorEmpresa(String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no está asociado a una empresa");
        }
        
        return servicioRepository.findByEmpresaIdAndDeletedFalse(owner.getEmpresa().getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista servicios paginados de la empresa del owner
     */
    public Page<ServicioResponse> listarServiciosPaginados(String ownerId, Pageable pageable) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no está asociado a una empresa");
        }
        
        return servicioRepository.findByEmpresaIdAndDeletedFalse(owner.getEmpresa().getId(), pageable)
                .map(this::mapToResponse);
    }
    
    /**
     * Lista servicios por categoría
     */
    public List<ServicioResponse> listarServiciosPorCategoria(String categoria, String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no está asociado a una empresa");
        }
        
        return servicioRepository.findByEmpresaIdAndCategoriaAndDeletedFalse(
                owner.getEmpresa().getId(), categoria)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Elimina un servicio (soft delete)
     */
    @Transactional
    public void eliminarServicio(Long servicioId, String ownerId) {
        Servicio servicio = servicioRepository.findByIdAndDeletedFalse(servicioId)
                .orElseThrow(() -> new ServicioNotFoundException(servicioId));
        
        validarAccesoServicio(servicio, ownerId);
        
        servicio.softDelete(ownerId);
        servicioRepository.save(servicio);
    }
    
    /**
     * Restaura un servicio eliminado
     */
    @Transactional
    public ServicioResponse restaurarServicio(Long servicioId, String ownerId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ServicioNotFoundException(servicioId));
        
        if (!servicio.getDeleted()) {
            throw new IllegalStateException("El servicio no está eliminado");
        }
        
        validarAccesoServicio(servicio, ownerId);
        
        servicio.restore();
        Servicio restored = servicioRepository.save(servicio);
        return mapToResponse(restored);
    }
    
    /**
     * Valida que el owner tenga acceso al servicio
     */
    private void validarAccesoServicio(Servicio servicio, String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (owner.getEmpresa() == null || 
            !owner.getEmpresa().getId().equals(servicio.getEmpresa().getId())) {
            throw new ServicioAccessDeniedException("No tiene permisos para acceder a este servicio");
        }
    }
    
    /**
     * Mapea una entidad Servicio a ServicioResponse
     */
    private ServicioResponse mapToResponse(Servicio servicio) {
        return ServicioResponse.builder()
                .id(servicio.getId())
                .empresaId(servicio.getEmpresa().getId())
                .empresaNombre(servicio.getEmpresa().getNombre())
                .name(servicio.getName())
                .description(servicio.getDescription())
                .duration(servicio.getDuration())
                .price(servicio.getPrice())
                .categoria(servicio.getCategoria())
                .publicoObjetivo(servicio.getPublicoObjetivo())
                .imageUrl(servicio.getImageUrl())
                .deleted(servicio.getDeleted())
                .build();
    }
}
