package apex.code.clipperBarberShop.reporte.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Reserva;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.register.domain.port.out.EmpresaRepositoryPort;
import apex.code.clipperBarberShop.reporte.application.dto.*;
import apex.code.clipperBarberShop.reporte.domain.port.in.ReporteUseCase;
import apex.code.clipperBarberShop.reporte.domain.port.out.ReporteRepositoryPort;
import apex.code.clipperBarberShop.user.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de reportes para OWNERS
 */
@Service
@RequiredArgsConstructor
public class ReporteService implements ReporteUseCase {
    
    private final ReporteRepositoryPort reporteRepository;
    private final EmpresaRepositoryPort empresaRepository;
    private final UserRepositoryPort userRepository;
    
    @Override
    public ReporteIngresosResponse obtenerReporteIngresos(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin, 
            String ownerId
    ) {
        validarAccesoEmpresa(empresaId, ownerId);
        Empresa empresa = obtenerEmpresa(empresaId);
        
        List<Reserva> reservas = reporteRepository.findReservasByEmpresaAndPeriodo(
                empresaId, fechaInicio, fechaFin
        );
        
        List<Reserva> completadas = reservas.stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .collect(Collectors.toList());
        
        List<Reserva> canceladas = reservas.stream()
                .filter(r -> "CANCELLED".equals(r.getStatus()))
                .collect(Collectors.toList());
        
        double ingresoTotal = completadas.stream()
                .mapToDouble(Reserva::getFinalPrice)
                .sum();
        
        double ingresoPromedio = completadas.isEmpty() ? 0.0 : 
                ingresoTotal / completadas.size();
        
        // Ingresos por día
        List<ReporteIngresosResponse.IngresoPorDia> ingresosPorDia = calcularIngresosPorDia(
                completadas, fechaInicio, fechaFin
        );
        
        // Ingresos por servicio
        List<ReporteIngresosResponse.IngresoPorServicio> ingresosPorServicio = 
                calcularIngresosPorServicio(completadas);
        
        return ReporteIngresosResponse.builder()
                .empresaId(empresaId)
                .empresaNombre(empresa.getNombre())
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .ingresoTotal(ingresoTotal)
                .totalReservas(reservas.size())
                .reservasCompletadas(completadas.size())
                .reservasCanceladas(canceladas.size())
                .ingresoPromedioPorReserva(ingresoPromedio)
                .ingresosPorDia(ingresosPorDia)
                .ingresosPorServicio(ingresosPorServicio)
                .build();
    }
    
    @Override
    public ReporteServiciosResponse obtenerReporteServicios(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin, 
            String ownerId
    ) {
        validarAccesoEmpresa(empresaId, ownerId);
        Empresa empresa = obtenerEmpresa(empresaId);
        
        List<Reserva> completadas = reporteRepository.findReservasCompletadasByEmpresaAndPeriodo(
                empresaId, fechaInicio, fechaFin
        );
        
        double ingresoTotal = completadas.stream()
                .mapToDouble(Reserva::getFinalPrice)
                .sum();
        
        // Agrupar por servicio
        Map<Long, List<Reserva>> reservasPorServicio = completadas.stream()
                .collect(Collectors.groupingBy(r -> r.getService().getId()));
        
        List<ReporteServiciosResponse.ServicioEstadistica> servicios = reservasPorServicio.entrySet()
                .stream()
                .map(entry -> {
                    List<Reserva> reservasServicio = entry.getValue();
                    Reserva primera = reservasServicio.get(0);
                    
                    double ingresoServicio = reservasServicio.stream()
                            .mapToDouble(Reserva::getFinalPrice)
                            .sum();
                    
                    double porcentaje = ingresoTotal > 0 ? 
                            (ingresoServicio / ingresoTotal) * 100 : 0.0;
                    
                    return ReporteServiciosResponse.ServicioEstadistica.builder()
                            .servicioId(primera.getService().getId())
                            .servicioNombre(primera.getService().getName())
                            .precio(primera.getService().getPrice())
                            .duracionMinutos(primera.getService().getDuration())
                            .cantidadReservas(reservasServicio.size())
                            .ingresoGenerado(ingresoServicio)
                            .porcentajeDelTotal(porcentaje)
                            .build();
                })
                .sorted(Comparator.comparingInt(
                        ReporteServiciosResponse.ServicioEstadistica::getCantidadReservas
                ).reversed())
                .collect(Collectors.toList());
        
        Set<Long> serviciosUnicos = completadas.stream()
                .map(r -> r.getService().getId())
                .collect(Collectors.toSet());
        
        return ReporteServiciosResponse.builder()
                .empresaId(empresaId)
                .empresaNombre(empresa.getNombre())
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .totalServicios(serviciosUnicos.size())
                .servicios(servicios)
                .build();
    }
    
    @Override
    public ReporteEmpleadosResponse obtenerReporteEmpleados(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin, 
            String ownerId
    ) {
        validarAccesoEmpresa(empresaId, ownerId);
        Empresa empresa = obtenerEmpresa(empresaId);
        
        List<Reserva> reservas = reporteRepository.findReservasByEmpresaAndPeriodo(
                empresaId, fechaInicio, fechaFin
        );
        
        // Agrupar por empleado
        Map<String, List<Reserva>> reservasPorEmpleado = reservas.stream()
                .collect(Collectors.groupingBy(Reserva::getEmployeeId));
        
        List<ReporteEmpleadosResponse.EmpleadoEstadistica> empleados = reservasPorEmpleado.entrySet()
                .stream()
                .map(entry -> {
                    String empleadoId = entry.getKey();
                    List<Reserva> reservasEmpleado = entry.getValue();
                    
                    Usuario empleado = userRepository.findById(empleadoId).orElse(null);
                    
                    long completadas = reservasEmpleado.stream()
                            .filter(r -> "COMPLETED".equals(r.getStatus()))
                            .count();
                    
                    long canceladas = reservasEmpleado.stream()
                            .filter(r -> "CANCELLED".equals(r.getStatus()))
                            .count();
                    
                    double ingresoGenerado = reservasEmpleado.stream()
                            .filter(r -> "COMPLETED".equals(r.getStatus()))
                            .mapToDouble(Reserva::getFinalPrice)
                            .sum();
                    
                    return ReporteEmpleadosResponse.EmpleadoEstadistica.builder()
                            .empleadoId(empleadoId)
                            .empleadoNombre(empleado != null ? 
                                    empleado.getName() + " " + empleado.getLastName() : "N/A")
                            .empleadoEmail(empleado != null ? empleado.getEmail() : "N/A")
                            .profileImageUrl(empleado != null ? empleado.getProfileImageUrl() : null)
                            .cantidadReservas(reservasEmpleado.size())
                            .reservasCompletadas((int) completadas)
                            .reservasCanceladas((int) canceladas)
                            .ingresoGenerado(ingresoGenerado)
                            .promedioCalificacion(0.0) // TODO: Implementar cuando esté el módulo de reseñas
                            .totalResenias(0) // TODO: Implementar cuando esté el módulo de reseñas
                            .build();
                })
                .sorted(Comparator.comparingDouble(
                        ReporteEmpleadosResponse.EmpleadoEstadistica::getIngresoGenerado
                ).reversed())
                .collect(Collectors.toList());
        
        Set<String> empleadosUnicos = reservas.stream()
                .map(Reserva::getEmployeeId)
                .collect(Collectors.toSet());
        
        return ReporteEmpleadosResponse.builder()
                .empresaId(empresaId)
                .empresaNombre(empresa.getNombre())
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .totalEmpleados(empleadosUnicos.size())
                .empleados(empleados)
                .build();
    }
    
    @Override
    public ReporteClientesResponse obtenerReporteClientes(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin, 
            String ownerId
    ) {
        validarAccesoEmpresa(empresaId, ownerId);
        Empresa empresa = obtenerEmpresa(empresaId);
        
        List<Reserva> reservas = reporteRepository.findReservasByEmpresaAndPeriodo(
                empresaId, fechaInicio, fechaFin
        );
        
        // Obtener clientes nuevos
        List<String> clientesNuevos = reporteRepository.findClientesNuevosByPeriodo(
                empresaId, fechaInicio, fechaFin
        );
        
        // Agrupar por cliente
        Map<String, List<Reserva>> reservasPorCliente = reservas.stream()
                .collect(Collectors.groupingBy(Reserva::getClientId));
        
        List<ReporteClientesResponse.ClienteEstadistica> topClientes = reservasPorCliente.entrySet()
                .stream()
                .map(entry -> {
                    String clienteId = entry.getKey();
                    List<Reserva> reservasCliente = entry.getValue();
                    
                    Usuario cliente = userRepository.findById(clienteId).orElse(null);
                    
                    double totalGastado = reservasCliente.stream()
                            .filter(r -> "COMPLETED".equals(r.getStatus()))
                            .mapToDouble(Reserva::getFinalPrice)
                            .sum();
                    
                    // Servicio favorito (el más reservado)
                    String servicioFavorito = reservasCliente.stream()
                            .collect(Collectors.groupingBy(
                                    r -> r.getService().getName(), 
                                    Collectors.counting()
                            ))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("N/A");
                    
                    // Última reserva
                    Optional<Reserva> ultimaReserva = reservasCliente.stream()
                            .max(Comparator.comparing(Reserva::getReservationDate));
                    
                    return ReporteClientesResponse.ClienteEstadistica.builder()
                            .clienteId(clienteId)
                            .clienteNombre(cliente != null ? 
                                    cliente.getName() + " " + cliente.getLastName() : "N/A")
                            .clienteEmail(cliente != null ? cliente.getEmail() : "N/A")
                            .profileImageUrl(cliente != null ? cliente.getProfileImageUrl() : null)
                            .cantidadReservas(reservasCliente.size())
                            .totalGastado(totalGastado)
                            .ultimaReserva(ultimaReserva.map(Reserva::getReservationDate).orElse(null))
                            .servicioFavorito(servicioFavorito)
                            .build();
                })
                .sorted(Comparator.comparingDouble(
                        ReporteClientesResponse.ClienteEstadistica::getTotalGastado
                ).reversed())
                .limit(20) // Top 20 clientes
                .collect(Collectors.toList());
        
        long clientesRecurrentes = reservasPorCliente.values().stream()
                .filter(list -> list.size() > 1)
                .count();
        
        return ReporteClientesResponse.builder()
                .empresaId(empresaId)
                .empresaNombre(empresa.getNombre())
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .totalClientes(reservasPorCliente.size())
                .clientesNuevos(clientesNuevos.size())
                .clientesRecurrentes((int) clientesRecurrentes)
                .topClientes(topClientes)
                .build();
    }
    
    @Override
    public ReporteConsolidadoResponse obtenerReporteConsolidado(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin, 
            String ownerId
    ) {
        validarAccesoEmpresa(empresaId, ownerId);
        Empresa empresa = obtenerEmpresa(empresaId);
        
        List<Reserva> reservas = reporteRepository.findReservasByEmpresaAndPeriodo(
                empresaId, fechaInicio, fechaFin
        );
        
        List<Reserva> completadas = reservas.stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .collect(Collectors.toList());
        
        List<Reserva> canceladas = reservas.stream()
                .filter(r -> "CANCELLED".equals(r.getStatus()))
                .collect(Collectors.toList());
        
        double ingresoTotal = completadas.stream()
                .mapToDouble(Reserva::getFinalPrice)
                .sum();
        
        double tasaCompletado = reservas.isEmpty() ? 0.0 : 
                (completadas.size() * 100.0) / reservas.size();
        
        double tasaCancelacion = reservas.isEmpty() ? 0.0 : 
                (canceladas.size() * 100.0) / reservas.size();
        
        // Servicio más popular
        Map.Entry<String, Long> servicioMasPopular = completadas.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getService().getName(), 
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        
        // Mejor empleado (por ingresos)
        Map<String, Double> ingresosPorEmpleado = completadas.stream()
                .collect(Collectors.groupingBy(
                        Reserva::getEmployeeId,
                        Collectors.summingDouble(Reserva::getFinalPrice)
                ));
        
        Map.Entry<String, Double> mejorEmpleado = ingresosPorEmpleado.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        
        Usuario empleado = mejorEmpleado != null ? 
                userRepository.findById(mejorEmpleado.getKey()).orElse(null) : null;
        
        // Clientes
        Set<String> clientesUnicos = reservas.stream()
                .map(Reserva::getClientId)
                .collect(Collectors.toSet());
        
        List<String> clientesNuevos = reporteRepository.findClientesNuevosByPeriodo(
                empresaId, fechaInicio, fechaFin
        );
        
        Map<String, Long> reservasPorCliente = reservas.stream()
                .collect(Collectors.groupingBy(Reserva::getClientId, Collectors.counting()));
        
        long clientesRecurrentes = reservasPorCliente.values().stream()
                .filter(count -> count > 1)
                .count();
        
        double promedioReservasPorCliente = clientesUnicos.isEmpty() ? 0.0 : 
                (double) reservas.size() / clientesUnicos.size();
        
        // Promedios
        long diasPeriodo = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
        double ingresoPromedioPorDia = ingresoTotal / diasPeriodo;
        
        double ingresoPromedioPorReserva = completadas.isEmpty() ? 0.0 : 
                ingresoTotal / completadas.size();
        
        double duracionPromedioReserva = completadas.isEmpty() ? 0.0 :
                completadas.stream()
                        .mapToInt(Reserva::getDuracionMinutos)
                        .average()
                        .orElse(0.0);
        
        Set<Long> serviciosUnicos = completadas.stream()
                .map(r -> r.getService().getId())
                .collect(Collectors.toSet());
        
        Set<String> empleadosActivos = reservas.stream()
                .map(Reserva::getEmployeeId)
                .collect(Collectors.toSet());
        
        return ReporteConsolidadoResponse.builder()
                .empresaId(empresaId)
                .empresaNombre(empresa.getNombre())
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .ingresoTotal(ingresoTotal)
                .totalReservas(reservas.size())
                .reservasCompletadas(completadas.size())
                .reservasCanceladas(canceladas.size())
                .tasaCompletado(tasaCompletado)
                .tasaCancelacion(tasaCancelacion)
                .totalServicios(serviciosUnicos.size())
                .servicioMasPopular(servicioMasPopular != null ? servicioMasPopular.getKey() : "N/A")
                .reservasServicioMasPopular(servicioMasPopular != null ? 
                        servicioMasPopular.getValue().intValue() : 0)
                .totalEmpleados(empleadosActivos.size())
                .empleadosActivos(empleadosActivos.size())
                .mejorEmpleado(empleado != null ? 
                        empleado.getName() + " " + empleado.getLastName() : "N/A")
                .ingresosMejorEmpleado(mejorEmpleado != null ? mejorEmpleado.getValue() : 0.0)
                .totalClientes(clientesUnicos.size())
                .clientesNuevos(clientesNuevos.size())
                .clientesRecurrentes((int) clientesRecurrentes)
                .promedioReservasPorCliente(promedioReservasPorCliente)
                .ingresoPromedioPorDia(ingresoPromedioPorDia)
                .ingresoPromedioPorReserva(ingresoPromedioPorReserva)
                .duracionPromedioPorReserva(duracionPromedioReserva)
                .build();
    }
    
    /**
     * Calcula los ingresos por día
     */
    private List<ReporteIngresosResponse.IngresoPorDia> calcularIngresosPorDia(
            List<Reserva> completadas, 
            LocalDate fechaInicio, 
            LocalDate fechaFin
    ) {
        Map<LocalDate, List<Reserva>> reservasPorDia = completadas.stream()
                .collect(Collectors.groupingBy(r -> r.getReservationDate().toLocalDate()));
        
        List<ReporteIngresosResponse.IngresoPorDia> ingresosPorDia = new ArrayList<>();
        
        LocalDate fecha = fechaInicio;
        while (!fecha.isAfter(fechaFin)) {
            List<Reserva> reservasDia = reservasPorDia.getOrDefault(fecha, Collections.emptyList());
            
            double ingresoDia = reservasDia.stream()
                    .mapToDouble(Reserva::getFinalPrice)
                    .sum();
            
            ingresosPorDia.add(ReporteIngresosResponse.IngresoPorDia.builder()
                    .fecha(fecha)
                    .ingreso(ingresoDia)
                    .cantidadReservas(reservasDia.size())
                    .build());
            
            fecha = fecha.plusDays(1);
        }
        
        return ingresosPorDia;
    }
    
    /**
     * Calcula los ingresos por servicio
     */
    private List<ReporteIngresosResponse.IngresoPorServicio> calcularIngresosPorServicio(
            List<Reserva> completadas
    ) {
        Map<Long, List<Reserva>> reservasPorServicio = completadas.stream()
                .collect(Collectors.groupingBy(r -> r.getService().getId()));
        
        return reservasPorServicio.entrySet().stream()
                .map(entry -> {
                    List<Reserva> reservas = entry.getValue();
                    Reserva primera = reservas.get(0);
                    
                    double ingreso = reservas.stream()
                            .mapToDouble(Reserva::getFinalPrice)
                            .sum();
                    
                    return ReporteIngresosResponse.IngresoPorServicio.builder()
                            .servicioId(primera.getService().getId())
                            .servicioNombre(primera.getService().getName())
                            .ingreso(ingreso)
                            .cantidadReservas(reservas.size())
                            .build();
                })
                .sorted(Comparator.comparingDouble(
                        ReporteIngresosResponse.IngresoPorServicio::getIngreso
                ).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Valida que el owner tenga acceso a la empresa
     */
    private void validarAccesoEmpresa(Long empresaId, String ownerId) {
        Usuario owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner no encontrado"));
        
        if (!"OWNER".equals(owner.getRole())) {
            throw new IllegalArgumentException("Solo los OWNERS pueden acceder a reportes");
        }
        
        if (owner.getEmpresa() == null || !owner.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("No tiene permisos para acceder a esta empresa");
        }
    }
    
    /**
     * Obtiene una empresa por ID
     */
    private Empresa obtenerEmpresa(Long empresaId) {
        return empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
    }
}
