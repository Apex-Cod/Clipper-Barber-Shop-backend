package apex.code.clipperBarberShop.payment.domain.port.out;

import apex.code.clipperBarberShop.Entities.Pago;
import apex.code.clipperBarberShop.Entities.enums.EstadoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para el repositorio de pagos
 */
public interface PagoRepositoryPort {
    Pago save(Pago pago);
    Optional<Pago> findById(Long id);
    Optional<Pago> findByIdAndDeletedFalse(Long id);
    Optional<Pago> findByPaypalPaymentId(String paypalPaymentId);
    Optional<Pago> findByReservaIdAndDeletedFalse(Long reservaId);
    List<Pago> findByEmpresaIdAndDeletedFalse(Long empresaId);
    List<Pago> findByEstadoAndDeletedFalse(EstadoPago estado);
    Page<Pago> findByEmpresaIdAndDeletedFalse(Long empresaId, Pageable pageable);
    boolean existsByReservaIdAndEstadoAndDeletedFalse(Long reservaId, EstadoPago estado);
    Long countByEmpresaIdAndEstadoAndDeletedFalse(Long empresaId, EstadoPago estado);
}
