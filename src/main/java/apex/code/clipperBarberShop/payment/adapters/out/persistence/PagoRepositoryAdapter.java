package apex.code.clipperBarberShop.payment.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Pago;
import apex.code.clipperBarberShop.Entities.enums.EstadoPago;
import apex.code.clipperBarberShop.payment.domain.port.out.PagoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador del repositorio de pagos
 */
@Component
@RequiredArgsConstructor
public class PagoRepositoryAdapter implements PagoRepositoryPort {
    
    private final PagoRepository pagoRepository;
    
    @Override
    public Pago save(Pago pago) {
        return pagoRepository.save(pago);
    }
    
    @Override
    public Optional<Pago> findById(Long id) {
        return pagoRepository.findById(id);
    }
    
    @Override
    public Optional<Pago> findByIdAndDeletedFalse(Long id) {
        return pagoRepository.findByIdAndDeletedFalse(id);
    }
    
    @Override
    public Optional<Pago> findByPaypalPaymentId(String paypalPaymentId) {
        return pagoRepository.findByPaypalPaymentId(paypalPaymentId);
    }
    
    @Override
    public Optional<Pago> findByReservaIdAndDeletedFalse(Long reservaId) {
        return pagoRepository.findByReserva_IdAndDeletedFalse(reservaId);
    }
    
    @Override
    public List<Pago> findByEmpresaIdAndDeletedFalse(Long empresaId) {
        return pagoRepository.findByEmpresa_IdAndDeletedFalse(empresaId);
    }
    
    @Override
    public List<Pago> findByEstadoAndDeletedFalse(EstadoPago estado) {
        return pagoRepository.findByEstadoAndDeletedFalse(estado);
    }
    
    @Override
    public Page<Pago> findByEmpresaIdAndDeletedFalse(Long empresaId, Pageable pageable) {
        return pagoRepository.findByEmpresa_IdAndDeletedFalse(empresaId, pageable);
    }
    
    @Override
    public boolean existsByReservaIdAndEstadoAndDeletedFalse(Long reservaId, EstadoPago estado) {
        return pagoRepository.existsByReserva_IdAndEstadoAndDeletedFalse(reservaId, estado);
    }
    
    @Override
    public Long countByEmpresaIdAndEstadoAndDeletedFalse(Long empresaId, EstadoPago estado) {
        return pagoRepository.countByEmpresa_IdAndEstadoAndDeletedFalse(empresaId, estado);
    }
}
