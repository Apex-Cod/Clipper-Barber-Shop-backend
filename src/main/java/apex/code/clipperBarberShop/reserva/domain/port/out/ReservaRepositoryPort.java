package apex.code.clipperBarberShop.reserva.domain.port.out;

import apex.code.clipperBarberShop.Entities.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para operaciones de repositorio de Reserva
 */
public interface ReservaRepositoryPort {
    
    Reserva save(Reserva reserva);
    
    Optional<Reserva> findById(Long id);
    
    Optional<Reserva> findByIdAndDeletedFalse(Long id);
    
    List<Reserva> findByClientIdAndDeletedFalse(String clientId);
    
    Page<Reserva> findByClientIdAndDeletedFalse(String clientId, Pageable pageable);
    
    List<Reserva> findByEmpresaIdAndDeletedFalse(Long empresaId);
    
    Page<Reserva> findByEmpresaIdAndDeletedFalse(Long empresaId, Pageable pageable);
    
    List<Reserva> findByEmployeeIdAndDeletedFalse(String employeeId);
    
    List<Reserva> findByClientIdAndStatusAndDeletedFalse(String clientId, String status);
    
    List<Reserva> findByEmpresaIdAndStatusAndDeletedFalse(Long empresaId, String status);
    
    // Verificar conflictos de reserva
    List<Reserva> findByEmployeeIdAndReservationDateBetweenAndStatusNotAndDeletedFalse(
            String employeeId, 
            LocalDateTime start, 
            LocalDateTime end, 
            String status);
    
    // Buscar reservas en un rango de fechas
    List<Reserva> findByEmpresaIdAndReservationDateBetweenAndDeletedFalse(
            Long empresaId, 
            LocalDateTime start, 
            LocalDateTime end);
    
    boolean existsByIdAndClientIdAndDeletedFalse(Long id, String clientId);
    
    boolean existsByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);
}
