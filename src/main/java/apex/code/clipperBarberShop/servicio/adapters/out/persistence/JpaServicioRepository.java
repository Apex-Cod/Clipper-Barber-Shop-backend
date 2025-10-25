package apex.code.clipperBarberShop.servicio.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Servicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaServicioRepository extends JpaRepository<Servicio, Long> {
    
    Optional<Servicio> findByIdAndDeletedFalse(Long id);
    
    List<Servicio> findByEmpresaIdAndDeletedFalse(Long empresaId);
    
    Page<Servicio> findByEmpresaIdAndDeletedFalse(Long empresaId, Pageable pageable);
    
    List<Servicio> findByEmpresaIdAndCategoriaAndDeletedFalse(Long empresaId, String categoria);
    
    boolean existsByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);
}
