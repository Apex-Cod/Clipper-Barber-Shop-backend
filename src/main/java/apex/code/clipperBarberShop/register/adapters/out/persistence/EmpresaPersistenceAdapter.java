package apex.code.clipperBarberShop.register.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.register.domain.port.out.EmpresaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmpresaPersistenceAdapter implements EmpresaRepositoryPort {

    private final SpringDataEmpresaRepository repo;

    @Override
    public Empresa save(Empresa empresa) {
        return repo.save(empresa);
    }

    @Override
    public Optional<Empresa> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public Optional<Empresa> findByIdAndDeletedFalse(Long id) {
        return repo.findByIdAndDeletedFalse(id);
    }

    @Override
    public List<Empresa> findAllByDeletedFalse() {
        return repo.findAllByDeletedFalse();
    }

    @Override
    public Page<Empresa> findAllByDeletedFalse(Pageable pageable) {
        return repo.findAllByDeletedFalse(pageable);
    }
}
