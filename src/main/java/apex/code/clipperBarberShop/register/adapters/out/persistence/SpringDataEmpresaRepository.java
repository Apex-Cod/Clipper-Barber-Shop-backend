package apex.code.clipperBarberShop.register.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import apex.code.clipperBarberShop.Entities.Empresa;

public interface SpringDataEmpresaRepository extends JpaRepository<Empresa, Long> {
}
