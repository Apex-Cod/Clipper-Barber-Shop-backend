package apex.code.clipperBarberShop.register.domain.port.out;

import java.util.List;
import java.util.Optional;

import apex.code.clipperBarberShop.Entities.Empresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmpresaRepositoryPort {
    Empresa save(Empresa empresa);
    Optional<Empresa> findById(Long id);
    Optional<Empresa> findByIdAndDeletedFalse(Long id);
    List<Empresa> findAllByDeletedFalse();
    Page<Empresa> findAllByDeletedFalse(Pageable pageable);
}
