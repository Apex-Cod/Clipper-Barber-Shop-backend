package apex.code.clipperBarberShop.plan.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Plan;
import apex.code.clipperBarberShop.Entities.enums.TipoPlan;
import apex.code.clipperBarberShop.plan.domain.port.out.PlanRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PlanRepositoryAdapter implements PlanRepositoryPort {

    private final SpringDataPlanRepository repository;

    @Override
    public Plan save(Plan plan) {
        return repository.save(plan);
    }

    @Override
    public Optional<Plan> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Plan> findByIdAndDeletedFalse(Long id) {
        return repository.findByIdAndDeletedFalse(id);
    }

    @Override
    public Optional<Plan> findByTipo(TipoPlan tipo) {
        return repository.findByTipo(tipo);
    }

    @Override
    public Optional<Plan> findByTipoAndDeletedFalse(TipoPlan tipo) {
        return repository.findByTipoAndDeletedFalse(tipo);
    }

    @Override
    public List<Plan> findAllByDeletedFalse() {
        return repository.findAllByDeletedFalse();
    }

    @Override
    public List<Plan> findAllByActivoTrueAndDeletedFalse() {
        return repository.findAllByActivoTrueAndDeletedFalse();
    }

    @Override
    public Page<Plan> findAllByDeletedFalse(Pageable pageable) {
        return repository.findAllByDeletedFalse(pageable);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByTipo(TipoPlan tipo) {
        return repository.existsByTipo(tipo);
    }
}
