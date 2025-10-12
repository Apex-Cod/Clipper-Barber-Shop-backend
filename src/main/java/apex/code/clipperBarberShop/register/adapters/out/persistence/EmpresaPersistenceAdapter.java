package apex.code.clipperBarberShop.register.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.register.domain.port.out.EmpresaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}
