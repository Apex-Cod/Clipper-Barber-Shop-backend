package apex.code.clipperBarberShop.resenia.application.service;

import apex.code.clipperBarberShop.resenia.application.dto.EstadisticasEmpresaResponse;
import apex.code.clipperBarberShop.resenia.application.dto.ReseniaResponse;
import apex.code.clipperBarberShop.resenia.domain.port.out.ReseniaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para operaciones PÚBLICAS de reseñas (sin autenticación)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReseniaPublicService {
    
    private final ReseniaRepositoryPort reseniaRepository;
    
    /**
     * Lista todas las reseñas activas de una empresa
     */
    public List<ReseniaResponse> listarReseniasPorEmpresa(Long empresaId) {
        return reseniaRepository.findByEmpresaIdAndDeletedFalse(empresaId).stream()
                .map(ReseniaResponse::fromDomain)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista reseñas de una empresa paginadas
     */
    public Page<ReseniaResponse> listarReseniasPaginadas(Long empresaId, Pageable pageable) {
        return reseniaRepository.findByEmpresaIdAndDeletedFalse(empresaId, pageable)
                .map(ReseniaResponse::fromDomain);
    }
    
    /**
     * Obtiene estadísticas de una empresa
     */
    public EstadisticasEmpresaResponse obtenerEstadisticasEmpresa(Long empresaId) {
        List<ReseniaResponse> resenias = listarReseniasPorEmpresa(empresaId);
        
        if (resenias.isEmpty()) {
            return EstadisticasEmpresaResponse.builder()
                    .totalResenias(0L)
                    .promedioCalificacion(0.0)
                    .resenias5Estrellas(0L)
                    .resenias4Estrellas(0L)
                    .resenias3Estrellas(0L)
                    .resenias2Estrellas(0L)
                    .resenias1Estrella(0L)
                    .distribucionCalificaciones(Map.of())
                    .build();
        }
        
        // Calculamos el promedio usando ambas calificaciones
        double promedio = resenias.stream()
                .mapToDouble(r -> (r.getCalificacionServicio() + r.getCalificacionEmpleado()) / 2.0)
                .average()
                .orElse(0.0);
        
        // Distribución basada en la calificación del servicio
        Map<Integer, Long> distribucion = resenias.stream()
                .collect(Collectors.groupingBy(
                        ReseniaResponse::getCalificacionServicio,
                        Collectors.counting()
                ));
        
        return EstadisticasEmpresaResponse.builder()
                .totalResenias((long) resenias.size())
                .promedioCalificacion(Math.round(promedio * 10.0) / 10.0)
                .resenias5Estrellas(distribucion.getOrDefault(5, 0L))
                .resenias4Estrellas(distribucion.getOrDefault(4, 0L))
                .resenias3Estrellas(distribucion.getOrDefault(3, 0L))
                .resenias2Estrellas(distribucion.getOrDefault(2, 0L))
                .resenias1Estrella(distribucion.getOrDefault(1, 0L))
                .distribucionCalificaciones(distribucion)
                .build();
    }
    
    /**
     * Lista reseñas de un empleado específico
     */
    public List<ReseniaResponse> listarReseniasPorEmpleado(String empleadoId) {
        return reseniaRepository.findByEmpleadoIdAndDeletedFalse(empleadoId).stream()
                .map(ReseniaResponse::fromDomain)
                .collect(Collectors.toList());
    }
}
