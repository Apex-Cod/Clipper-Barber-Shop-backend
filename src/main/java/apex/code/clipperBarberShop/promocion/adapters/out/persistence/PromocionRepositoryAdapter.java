package apex.code.clipperBarberShop.promocion.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Promocion;
import apex.code.clipperBarberShop.promocion.domain.port.out.PromocionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador del repositorio de promociones
 */
@Component
@RequiredArgsConstructor
public class PromocionRepositoryAdapter implements PromocionRepositoryPort {
    
    private final JpaPromocionRepository jpaRepository;
    
    @Override
    public Promocion save(Promocion promocion) {
        return jpaRepository.save(promocion);
    }
    
    @Override
    public Optional<Promocion> findByIdAndDeletedFalse(Long id) {
        return jpaRepository.findByIdAndDeletedFalse(id);
    }
    
    @Override
    public List<Promocion> findByEmpresaIdAndDeletedFalse(Long empresaId) {
        return jpaRepository.findByEmpresaIdAndDeletedFalse(empresaId);
    }
    
    @Override
    public Page<Promocion> findByEmpresaIdAndDeletedFalse(Long empresaId, Pageable pageable) {
        return jpaRepository.findByEmpresaIdAndDeletedFalse(empresaId, pageable);
    }
    
    @Override
    public List<Promocion> findByEmpresaIdAndActivaTrueAndDeletedFalse(Long empresaId) {
        return jpaRepository.findByEmpresaIdAndActivaTrueAndDeletedFalse(empresaId);
    }
    
    @Override
    public List<Promocion> findPromocionesVigentesParaServicio(Long empresaId, Long servicioId) {
        return jpaRepository.findPromocionesVigentesParaServicio(
                empresaId, servicioId, LocalDate.now());
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
