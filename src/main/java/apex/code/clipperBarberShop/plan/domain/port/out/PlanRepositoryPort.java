package apex.code.clipperBarberShop.plan.domain.port.out;

import apex.code.clipperBarberShop.Entities.Plan;
import apex.code.clipperBarberShop.Entities.enums.TipoPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PlanRepositoryPort {
    Plan save(Plan plan);
    Optional<Plan> findById(Long id);
    Optional<Plan> findByIdAndDeletedFalse(Long id);
    Optional<Plan> findByTipo(TipoPlan tipo);
    Optional<Plan> findByTipoAndDeletedFalse(TipoPlan tipo);
    List<Plan> findAllByDeletedFalse();
    List<Plan> findAllByActivoTrueAndDeletedFalse();
    Page<Plan> findAllByDeletedFalse(Pageable pageable);
    void deleteById(Long id);
    boolean existsByTipo(TipoPlan tipo);
}
