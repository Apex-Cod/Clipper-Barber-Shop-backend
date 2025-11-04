package apex.code.clipperBarberShop.servicio.application.service;

import apex.code.clipperBarberShop.Entities.Servicio;
import apex.code.clipperBarberShop.servicio.application.dto.ServicioResponse;
import apex.code.clipperBarberShop.servicio.domain.exception.ServicioNotFoundException;
import apex.code.clipperBarberShop.servicio.domain.port.out.ServicioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para consulta de servicios por parte de los CLIENTES
 * Solo tiene acceso de lectura
 */
@Service
@RequiredArgsConstructor
public class ServicioClientService {
    
    private final ServicioRepositoryPort servicioRepository;
    
    /**
     * Obtiene un servicio por su ID (solo activos)
     */
    public ServicioResponse obtenerServicio(Long servicioId) {
        Servicio servicio = servicioRepository.findByIdAndDeletedFalse(servicioId)
                .orElseThrow(() -> new ServicioNotFoundException(servicioId));
        
        return mapToResponse(servicio);
    }
    
    /**
     * Lista todos los servicios activos de una empresa
     */
    public List<ServicioResponse> listarServiciosPorEmpresa(Long empresaId) {
        return servicioRepository.findByEmpresaIdAndDeletedFalse(empresaId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista servicios paginados de una empresa
     */
    public Page<ServicioResponse> listarServiciosPaginados(Long empresaId, Pageable pageable) {
        return servicioRepository.findByEmpresaIdAndDeletedFalse(empresaId, pageable)
                .map(this::mapToResponse);
    }
    
    /**
     * Lista servicios de una empresa filtrados por categoría
     */
    public List<ServicioResponse> listarServiciosPorCategoria(Long empresaId, String categoria) {
        return servicioRepository.findByEmpresaIdAndCategoriaAndDeletedFalse(empresaId, categoria)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
