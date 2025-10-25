package apex.code.clipperBarberShop.reserva.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Reserva;
import apex.code.clipperBarberShop.reserva.domain.port.out.ReservaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReservaRepositoryAdapter implements ReservaRepositoryPort {
    
    private final JpaReservaRepository jpaReservaRepository;
    
    @Override
    public Reserva save(Reserva reserva) {
        return jpaReservaRepository.save(reserva);
    }
    
    @Override
    public Optional<Reserva> findById(Long id) {
        return jpaReservaRepository.findById(id);
    }
    
    @Override
    public Optional<Reserva> findByIdAndDeletedFalse(Long id) {
        return jpaReservaRepository.findByIdAndDeletedFalse(id);
    }
    
    @Override
    public List<Reserva> findByClientIdAndDeletedFalse(String clientId) {
        return jpaReservaRepository.findByClientIdAndDeletedFalse(clientId);
    }
    
    @Override
    public Page<Reserva> findByClientIdAndDeletedFalse(String clientId, Pageable pageable) {
        return jpaReservaRepository.findByClientIdAndDeletedFalse(clientId, pageable);
    }
    
    @Override
    public List<Reserva> findByEmpresaIdAndDeletedFalse(Long empresaId) {
        return jpaReservaRepository.findByEmpresaIdAndDeletedFalse(empresaId);
    }
    
    @Override
    public Page<Reserva> findByEmpresaIdAndDeletedFalse(Long empresaId, Pageable pageable) {
        return jpaReservaRepository.findByEmpresaIdAndDeletedFalse(empresaId, pageable);
    }
    
    @Override
    public List<Reserva> findByEmployeeIdAndDeletedFalse(String employeeId) {
        return jpaReservaRepository.findByEmployeeIdAndDeletedFalse(employeeId);
    }
    
    @Override
    public List<Reserva> findByClientIdAndStatusAndDeletedFalse(String clientId, String status) {
        return jpaReservaRepository.findByClientIdAndStatusAndDeletedFalse(clientId, status);
    }
    
    @Override
    public List<Reserva> findByEmpresaIdAndStatusAndDeletedFalse(Long empresaId, String status) {
        return jpaReservaRepository.findByEmpresaIdAndStatusAndDeletedFalse(empresaId, status);
    }
    
    @Override
    public List<Reserva> findByEmployeeIdAndReservationDateBetweenAndStatusNotAndDeletedFalse(
            String employeeId, LocalDateTime start, LocalDateTime end, String status) {
        return jpaReservaRepository.findByEmployeeIdAndReservationDateBetweenAndStatusNotAndDeletedFalse(
                employeeId, start, end, status);
    }
    
    @Override
    public List<Reserva> findByEmpresaIdAndReservationDateBetweenAndDeletedFalse(
            Long empresaId, LocalDateTime start, LocalDateTime end) {
        return jpaReservaRepository.findByEmpresaIdAndReservationDateBetweenAndDeletedFalse(
                empresaId, start, end);
    }
    
    @Override
    public boolean existsByIdAndClientIdAndDeletedFalse(Long id, String clientId) {
        return jpaReservaRepository.existsByIdAndClientIdAndDeletedFalse(id, clientId);
    }
    
    @Override
    public boolean existsByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId) {
        return jpaReservaRepository.existsByIdAndEmpresaIdAndDeletedFalse(id, empresaId);
    }
}
