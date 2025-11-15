package apex.code.clipperBarberShop.reporte.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Reserva;
import apex.code.clipperBarberShop.reporte.domain.port.out.ReporteRepositoryPort;
import apex.code.clipperBarberShop.reserva.domain.port.out.ReservaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para reportes
 */
@Component
@RequiredArgsConstructor
public class ReporteRepositoryAdapter implements ReporteRepositoryPort {
    
    private final ReservaRepositoryPort reservaRepository;
    
    @Override
    public List<Reserva> findReservasByEmpresaAndPeriodo(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin
    ) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
        
        // Usar el método existente del repositorio de reservas
        return reservaRepository.findByEmpresaIdAndReservationDateBetweenAndDeletedFalse(
                empresaId, inicio, fin
        );
    }
    
    @Override
    public List<Reserva> findReservasCompletadasByEmpresaAndPeriodo(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin
    ) {
        return findReservasByEmpresaAndPeriodo(empresaId, fechaInicio, fechaFin).stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .collect(Collectors.toList());
    }
    
    @Override
    public Long countClientesUnicosByEmpresaAndPeriodo(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin
    ) {
        return findReservasByEmpresaAndPeriodo(empresaId, fechaInicio, fechaFin).stream()
                .map(Reserva::getClientId)
                .distinct()
                .count();
    }
    
    @Override
    public List<String> findClientesNuevosByPeriodo(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin
    ) {
        // Clientes que hicieron su primera reserva en este período
        List<Reserva> reservasPeriodo = findReservasByEmpresaAndPeriodo(
                empresaId, fechaInicio, fechaFin
        );
        
        // Todas las reservas de la empresa (usamos un rango amplio)
        LocalDateTime inicioHistorico = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime finActual = LocalDateTime.now();
        
        List<Reserva> todasReservas = reservaRepository.findByEmpresaIdAndReservationDateBetweenAndDeletedFalse(
                empresaId, inicioHistorico, finActual
        );
        
        return reservasPeriodo.stream()
                .map(Reserva::getClientId)
                .distinct()
                .filter(clienteId -> {
                    // Verificar si su primera reserva fue en este período
                    LocalDateTime primeraReserva = todasReservas.stream()
                            .filter(r -> r.getClientId().equals(clienteId))
                            .map(Reserva::getReservationDate)
                            .min(LocalDateTime::compareTo)
                            .orElse(null);
                    
                    if (primeraReserva == null) return false;
                    
                    LocalDateTime inicio = fechaInicio.atStartOfDay();
                    LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
                    
                    return !primeraReserva.isBefore(inicio) && !primeraReserva.isAfter(fin);
                })
                .collect(Collectors.toList());
    }
}
