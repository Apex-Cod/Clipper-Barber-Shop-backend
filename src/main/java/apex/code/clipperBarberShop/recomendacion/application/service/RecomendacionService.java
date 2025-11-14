package apex.code.clipperBarberShop.recomendacion.application.service;

import apex.code.clipperBarberShop.Entities.*;
import apex.code.clipperBarberShop.Entities.enums.PublicoObjetivo;
import apex.code.clipperBarberShop.recomendacion.application.dto.*;
import apex.code.clipperBarberShop.register.adapters.out.persistence.SpringDataEmpresaRepository;
import apex.code.clipperBarberShop.resenia.adapters.out.persistence.ReseniaRepository;
import apex.code.clipperBarberShop.reserva.adapters.out.persistence.JpaReservaRepository;
import apex.code.clipperBarberShop.servicio.adapters.out.persistence.JpaServicioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio simple de recomendaciones basado en:
 * 1. Historial de reservas del cliente
 * 2. Calificaciones de reseñas
 * 3. Género/público objetivo del servicio
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecomendacionService {

    private final JpaReservaRepository reservaRepository;
    private final JpaServicioRepository servicioRepository;
    private final ReseniaRepository reseniaRepository;
    private final SpringDataEmpresaRepository empresaRepository;

    /**
     * Recomienda servicios basándose en el historial y preferencias del cliente
     * 
     * @param clienteId ID del cliente
     * @param publicoObjetivo Filtro opcional de público objetivo (HOMBRES, MUJERES, UNISEX, etc)
     * @param limit Cantidad máxima de resultados
     * @return Lista de servicios recomendados ordenados por relevancia
     */
    @Transactional(readOnly = true)
    public List<ServicioRecomendadoDTO> recomendarServicios(
            String clienteId, 
            PublicoObjetivo publicoObjetivo,
            Integer limit) {
        
        log.info("Generando recomendaciones de servicios para cliente: {}", clienteId);
        
        // 1. Obtener historial de reservas del cliente
        List<Reserva> historialReservas = reservaRepository.findByClientIdAndDeletedFalse(clienteId);
        
        // 2. Obtener servicios ya usados por el cliente
        Map<Long, Integer> serviciosUsados = contarServiciosUsados(historialReservas);
        
        // 3. Obtener categorías más usadas por el cliente
        Set<String> categoriasPreferidas = obtenerCategoriasPreferidas(historialReservas);
        
        // 4. Obtener todos los servicios activos
        List<Servicio> todosServicios = servicioRepository.findAll().stream()
                .filter(s -> !s.getDeleted())
                .collect(Collectors.toList());
        
        // 5. Filtrar por público objetivo si se especifica
        if (publicoObjetivo != null) {
            todosServicios = todosServicios.stream()
                    .filter(s -> s.getPublicoObjetivo() == publicoObjetivo || 
                                 s.getPublicoObjetivo() == PublicoObjetivo.UNISEX)
                    .collect(Collectors.toList());
        }
        
        // 6. Calcular score para cada servicio y convertir a DTO
        List<ServicioRecomendadoDTO> recomendaciones = todosServicios.stream()
                .map(servicio -> calcularRecomendacionServicio(
                        servicio, 
                        serviciosUsados, 
                        categoriasPreferidas,
                        clienteId))
                .sorted((a, b) -> {
                    // Ordenar por: calificación promedio desc, veces usado desc
                    int cmpCalificacion = Double.compare(
                            b.getCalificacionPromedio() != null ? b.getCalificacionPromedio() : 0.0,
                            a.getCalificacionPromedio() != null ? a.getCalificacionPromedio() : 0.0
                    );
                    if (cmpCalificacion != 0) return cmpCalificacion;
                    
                    return Integer.compare(b.getVecesUsadoPorCliente(), a.getVecesUsadoPorCliente());
                })
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());
        
        log.info("Se generaron {} recomendaciones de servicios", recomendaciones.size());
        return recomendaciones;
    }

    /**
     * Recomienda empresas basándose en el historial y preferencias del cliente
     */
    @Transactional(readOnly = true)
    public List<EmpresaRecomendadaDTO> recomendarEmpresas(
            String clienteId,
            PublicoObjetivo publicoObjetivo,
            Integer limit) {
        
        log.info("Generando recomendaciones de empresas para cliente: {}", clienteId);
        
        // 1. Obtener historial de reservas del cliente
        List<Reserva> historialReservas = reservaRepository.findByClientIdAndDeletedFalse(clienteId);
        
        // 2. Contar empresas visitadas
        Map<Long, Integer> empresasVisitadas = contarEmpresasVisitadas(historialReservas);
        
        // 3. Obtener todas las empresas activas
        List<Empresa> todasEmpresas = empresaRepository.findAllByDeletedFalse();
        
        // 4. Filtrar por público objetivo si se especifica
        if (publicoObjetivo != null) {
            todasEmpresas = todasEmpresas.stream()
                    .filter(e -> e.getPublicoObjetivo() == publicoObjetivo || 
                                 e.getPublicoObjetivo() == PublicoObjetivo.UNISEX)
                    .collect(Collectors.toList());
        }
        
        // 5. Calcular score y convertir a DTO
        List<EmpresaRecomendadaDTO> recomendaciones = todasEmpresas.stream()
                .filter(e -> "ACTIVA".equals(e.getEstado()))
                .map(empresa -> calcularRecomendacionEmpresa(
                        empresa,
                        empresasVisitadas,
                        clienteId))
                .sorted((a, b) -> {
                    // Ordenar por: calificación promedio desc, veces visitada desc
                    int cmpCalificacion = Double.compare(
                            b.getCalificacionPromedio() != null ? b.getCalificacionPromedio() : 0.0,
                            a.getCalificacionPromedio() != null ? a.getCalificacionPromedio() : 0.0
                    );
                    if (cmpCalificacion != 0) return cmpCalificacion;
                    
                    return Integer.compare(b.getVecesVisitadaPorCliente(), a.getVecesVisitadaPorCliente());
                })
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());
        
        log.info("Se generaron {} recomendaciones de empresas", recomendaciones.size());
        return recomendaciones;
    }

    /**
     * Obtiene servicios mejor calificados por público objetivo
     */
    @Transactional(readOnly = true)
    public List<ServicioRecomendadoDTO> obtenerMejoresServiciosPorGenero(
            PublicoObjetivo publicoObjetivo,
            Integer limit) {
        
        log.info("Obteniendo mejores servicios para público: {}", publicoObjetivo);
        
        List<Servicio> servicios = servicioRepository.findAll().stream()
                .filter(s -> !s.getDeleted())
                .filter(s -> s.getPublicoObjetivo() == publicoObjetivo || 
                            s.getPublicoObjetivo() == PublicoObjetivo.UNISEX)
                .collect(Collectors.toList());
        
        return servicios.stream()
                .map(servicio -> calcularRecomendacionServicio(servicio, 
                        Collections.emptyMap(), 
                        Collections.emptySet(), 
                        null))
                .sorted((a, b) -> Double.compare(
                        b.getCalificacionPromedio() != null ? b.getCalificacionPromedio() : 0.0,
                        a.getCalificacionPromedio() != null ? a.getCalificacionPromedio() : 0.0
                ))
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());
    }

    /**
     * Cuenta cuántas veces el cliente ha usado cada servicio
     */
    private Map<Long, Integer> contarServiciosUsados(List<Reserva> reservas) {
        Map<Long, Integer> contador = new HashMap<>();
        for (Reserva reserva : reservas) {
            if ("COMPLETED".equals(reserva.getStatus()) && reserva.getService() != null) {
                Long servicioId = reserva.getService().getId();
                contador.put(servicioId, contador.getOrDefault(servicioId, 0) + 1);
            }
        }
        return contador;
    }

    /**
     * Cuenta cuántas veces el cliente ha visitado cada empresa
     */
    private Map<Long, Integer> contarEmpresasVisitadas(List<Reserva> reservas) {
        Map<Long, Integer> contador = new HashMap<>();
        for (Reserva reserva : reservas) {
            if ("COMPLETED".equals(reserva.getStatus()) && reserva.getEmpresa() != null) {
                Long empresaId = reserva.getEmpresa().getId();
                contador.put(empresaId, contador.getOrDefault(empresaId, 0) + 1);
            }
        }
        return contador;
    }

    /**
     * Obtiene las categorías más usadas por el cliente
     */
    private Set<String> obtenerCategoriasPreferidas(List<Reserva> reservas) {
        Map<String, Integer> categoriasCount = new HashMap<>();
        
        for (Reserva reserva : reservas) {
            if ("COMPLETED".equals(reserva.getStatus()) && 
                reserva.getService() != null && 
                reserva.getService().getCategoria() != null) {
                
                String categoria = reserva.getService().getCategoria();
                categoriasCount.put(categoria, categoriasCount.getOrDefault(categoria, 0) + 1);
            }
        }
        
        // Retornar las top 3 categorías
        return categoriasCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Calcula la recomendación de un servicio específico
     */
    private ServicioRecomendadoDTO calcularRecomendacionServicio(
            Servicio servicio,
            Map<Long, Integer> serviciosUsados,
            Set<String> categoriasPreferidas,
            String clienteId) {
        
        // Calcular calificación promedio del servicio
        Double calificacionPromedio = calcularCalificacionPromedioServicio(servicio.getId());
        Integer totalResenias = contarReseniasServicio(servicio.getId());
        Integer vecesUsado = serviciosUsados.getOrDefault(servicio.getId(), 0);
        
        // Determinar motivo de recomendación
        List<String> motivos = new ArrayList<>();
        if (vecesUsado > 0) {
            motivos.add("Ya lo has usado " + vecesUsado + (vecesUsado == 1 ? " vez" : " veces"));
        }
        if (categoriasPreferidas.contains(servicio.getCategoria())) {
            motivos.add("Categoría de tu preferencia");
        }
        if (calificacionPromedio != null && calificacionPromedio >= 4.5) {
            motivos.add("Excelentemente calificado");
        } else if (calificacionPromedio != null && calificacionPromedio >= 4.0) {
            motivos.add("Bien calificado");
        }
        if (totalResenias > 20) {
            motivos.add("Popular");
        }
        
        String motivoRecomendacion = motivos.isEmpty() ? 
                "Nuevo para ti" : String.join(" • ", motivos);
        
        return ServicioRecomendadoDTO.builder()
                .id(servicio.getId())
                .nombre(servicio.getName())
                .descripcion(servicio.getDescription())
                .precio(servicio.getPrice())
                .duracion(servicio.getDuration())
                .categoria(servicio.getCategoria())
                .publicoObjetivo(servicio.getPublicoObjetivo() != null ? 
                        servicio.getPublicoObjetivo().name() : null)
                .imageUrl(servicio.getImageUrl())
                .empresaId(servicio.getEmpresa().getId())
                .empresaNombre(servicio.getEmpresa().getNombre())
                .empresaDireccion(servicio.getEmpresa().getDireccion())
                .empresaLogoUrl(servicio.getEmpresa().getLogoUrl())
                .calificacionPromedio(calificacionPromedio)
                .totalResenias(totalResenias)
                .vecesUsadoPorCliente(vecesUsado)
                .motivoRecomendacion(motivoRecomendacion)
                .build();
    }

    /**
     * Calcula la recomendación de una empresa específica
     */
    private EmpresaRecomendadaDTO calcularRecomendacionEmpresa(
            Empresa empresa,
            Map<Long, Integer> empresasVisitadas,
            String clienteId) {
        
        Double calificacionPromedio = calcularCalificacionPromedioEmpresa(empresa.getId());
        Integer totalResenias = contarReseniasEmpresa(empresa.getId());
        Integer vecesVisitada = empresasVisitadas.getOrDefault(empresa.getId(), 0);
        
        // Determinar motivo de recomendación
        List<String> motivos = new ArrayList<>();
        if (vecesVisitada > 0) {
            motivos.add("Ya la has visitado " + vecesVisitada + (vecesVisitada == 1 ? " vez" : " veces"));
        }
        if (calificacionPromedio != null && calificacionPromedio >= 4.5) {
            motivos.add("Excelentemente calificada");
        } else if (calificacionPromedio != null && calificacionPromedio >= 4.0) {
            motivos.add("Bien calificada");
        }
        if (totalResenias > 50) {
            motivos.add("Muy popular");
        } else if (totalResenias > 20) {
            motivos.add("Popular");
        }
        
        String motivoRecomendacion = motivos.isEmpty() ? 
                "Nueva opción para ti" : String.join(" • ", motivos);
        
        // Obtener servicios destacados de la empresa
        List<ServicioDestacadoDTO> serviciosDestacados = obtenerServiciosDestacados(empresa.getId());
        
        return EmpresaRecomendadaDTO.builder()
                .id(empresa.getId())
                .nombre(empresa.getNombre())
                .direccion(empresa.getDireccion())
                .telefono(empresa.getTelefono())
                .email(empresa.getEmail())
                .logoUrl(empresa.getLogoUrl())
                .bannerUrl(empresa.getBannerUrl())
                .descripcion(empresa.getDescripcion())
                .publicoObjetivo(empresa.getPublicoObjetivo() != null ? 
                        empresa.getPublicoObjetivo().name() : null)
                .calificacionPromedio(calificacionPromedio)
                .totalResenias(totalResenias)
                .vecesVisitadaPorCliente(vecesVisitada)
                .motivoRecomendacion(motivoRecomendacion)
                .serviciosDestacados(serviciosDestacados)
                .build();
    }

    /**
     * Obtiene los servicios más destacados de una empresa
     */
    private List<ServicioDestacadoDTO> obtenerServiciosDestacados(Long empresaId) {
        return servicioRepository.findByEmpresaIdAndDeletedFalse(empresaId).stream()
                .limit(3)
                .map(s -> ServicioDestacadoDTO.builder()
                        .id(s.getId())
                        .nombre(s.getName())
                        .precio(s.getPrice())
                        .categoria(s.getCategoria())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Calcula la calificación promedio de un servicio basado en las reseñas
     */
    private Double calcularCalificacionPromedioServicio(Long servicioId) {
        try {
            List<Resenia> resenias = reseniaRepository.findAll().stream()
                    .filter(r -> !r.getDeleted())
                    .filter(r -> r.getReserva() != null && 
                                r.getReserva().getService() != null &&
                                r.getReserva().getService().getId().equals(servicioId))
                    .collect(Collectors.toList());
            
            if (resenias.isEmpty()) return null;
            
            double promedio = resenias.stream()
                    .mapToInt(Resenia::getCalificacionServicio)
                    .average()
                    .orElse(0.0);
            
            return Math.round(promedio * 10.0) / 10.0; // Redondear a 1 decimal
        } catch (Exception e) {
            log.warn("Error calculando calificación de servicio {}: {}", servicioId, e.getMessage());
            return null;
        }
    }

    /**
     * Cuenta las reseñas de un servicio
     */
    private Integer contarReseniasServicio(Long servicioId) {
        try {
            return (int) reseniaRepository.findAll().stream()
                    .filter(r -> !r.getDeleted())
                    .filter(r -> r.getReserva() != null && 
                                r.getReserva().getService() != null &&
                                r.getReserva().getService().getId().equals(servicioId))
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Calcula la calificación promedio de una empresa
     */
    private Double calcularCalificacionPromedioEmpresa(Long empresaId) {
        try {
            Double promedio = reseniaRepository.getAverageCalificacionByEmpresaId(empresaId);
            return promedio != null ? Math.round(promedio * 10.0) / 10.0 : null;
        } catch (Exception e) {
            log.warn("Error calculando calificación de empresa {}: {}", empresaId, e.getMessage());
            return null;
        }
    }

    /**
     * Cuenta las reseñas de una empresa
     */
    private Integer contarReseniasEmpresa(Long empresaId) {
        try {
            Long count = reseniaRepository.countByEmpresa_IdAndDeletedFalse(empresaId);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
