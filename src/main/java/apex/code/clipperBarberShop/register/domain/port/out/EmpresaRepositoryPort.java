package apex.code.clipperBarberShop.register.domain.port.out;

import java.util.Optional;

import apex.code.clipperBarberShop.Entities.Empresa;

public interface EmpresaRepositoryPort {
    Empresa save(Empresa empresa);
    Optional<Empresa> findById(Long id);
}
