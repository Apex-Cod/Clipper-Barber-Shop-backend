package apex.code.clipperBarberShop.plan.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Plan;
import apex.code.clipperBarberShop.Entities.enums.TipoPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataPlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByIdAndDeletedFalse(Long id);
    Optional<Plan> findByTipo(TipoPlan tipo);
    Optional<Plan> findByTipoAndDeletedFalse(TipoPlan tipo);
    List<Plan> findAllByDeletedFalse();
    List<Plan> findAllByActivoTrueAndDeletedFalse();
    Page<Plan> findAllByDeletedFalse(Pageable pageable);
    boolean existsByTipo(TipoPlan tipo);
}
