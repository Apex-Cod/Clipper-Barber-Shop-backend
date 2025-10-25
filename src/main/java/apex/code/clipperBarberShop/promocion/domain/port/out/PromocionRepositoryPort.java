package apex.code.clipperBarberShop.promocion.domain.port.out;

import apex.code.clipperBarberShop.Entities.Promocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para operaciones de repositorio de promociones
 */
public interface PromocionRepositoryPort {
    
    Promocion save(Promocion promocion);
    
    Optional<Promocion> findByIdAndDeletedFalse(Long id);
    
    List<Promocion> findByEmpresaIdAndDeletedFalse(Long empresaId);
    
    Page<Promocion> findByEmpresaIdAndDeletedFalse(Long empresaId, Pageable pageable);
    
    List<Promocion> findByEmpresaIdAndActivaTrueAndDeletedFalse(Long empresaId);
    
    List<Promocion> findPromocionesVigentesParaServicio(Long empresaId, Long servicioId);
    
    void deleteById(Long id);
}
