package apex.code.clipperBarberShop.payment.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Pago;
import apex.code.clipperBarberShop.Entities.enums.EstadoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Pago
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByIdAndDeletedFalse(Long id);
    Optional<Pago> findByPaypalPaymentId(String paypalPaymentId);
    Optional<Pago> findByReserva_IdAndDeletedFalse(Long reservaId);
    List<Pago> findByEmpresa_IdAndDeletedFalse(Long empresaId);
    List<Pago> findByEstadoAndDeletedFalse(EstadoPago estado);
    Page<Pago> findByEmpresa_IdAndDeletedFalse(Long empresaId, Pageable pageable);
    boolean existsByReserva_IdAndEstadoAndDeletedFalse(Long reservaId, EstadoPago estado);
    Long countByEmpresa_IdAndEstadoAndDeletedFalse(Long empresaId, EstadoPago estado);
}
