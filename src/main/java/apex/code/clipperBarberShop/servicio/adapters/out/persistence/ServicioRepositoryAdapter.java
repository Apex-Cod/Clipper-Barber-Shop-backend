package apex.code.clipperBarberShop.servicio.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Servicio;
import apex.code.clipperBarberShop.servicio.domain.port.out.ServicioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ServicioRepositoryAdapter implements ServicioRepositoryPort {
    
    private final JpaServicioRepository jpaServicioRepository;
    
    @Override
    public Servicio save(Servicio servicio) {
        return jpaServicioRepository.save(servicio);
    }
    
    @Override
    public Optional<Servicio> findById(Long id) {
        return jpaServicioRepository.findById(id);
    }
    
    @Override
    public Optional<Servicio> findByIdAndDeletedFalse(Long id) {
        return jpaServicioRepository.findByIdAndDeletedFalse(id);
    }
    
    @Override
    public List<Servicio> findByEmpresaIdAndDeletedFalse(Long empresaId) {
        return jpaServicioRepository.findByEmpresaIdAndDeletedFalse(empresaId);
    }
    
    @Override
    public Page<Servicio> findByEmpresaIdAndDeletedFalse(Long empresaId, Pageable pageable) {
        return jpaServicioRepository.findByEmpresaIdAndDeletedFalse(empresaId, pageable);
    }
    
    @Override
    public List<Servicio> findByEmpresaIdAndCategoriaAndDeletedFalse(Long empresaId, String categoria) {
        return jpaServicioRepository.findByEmpresaIdAndCategoriaAndDeletedFalse(empresaId, categoria);
    }
    
    @Override
    public boolean existsByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId) {
        return jpaServicioRepository.existsByIdAndEmpresaIdAndDeletedFalse(id, empresaId);
    }
    
    @Override
    public void delete(Servicio servicio) {
        jpaServicioRepository.delete(servicio);
    }
}
