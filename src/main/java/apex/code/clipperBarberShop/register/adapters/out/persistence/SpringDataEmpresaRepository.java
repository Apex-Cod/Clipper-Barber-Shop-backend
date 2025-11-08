package apex.code.clipperBarberShop.register.adapters.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import apex.code.clipperBarberShop.Entities.Empresa;

import java.util.List;
import java.util.Optional;

public interface SpringDataEmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByIdAndDeletedFalse(Long id);
    List<Empresa> findAllByDeletedFalse();
    Page<Empresa> findAllByDeletedFalse(Pageable pageable);
}
