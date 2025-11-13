package apex.code.clipperBarberShop.resenia.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Resenia;
import apex.code.clipperBarberShop.resenia.domain.port.out.ReseniaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReseniaRepositoryAdapter implements ReseniaRepositoryPort {
    
    private final ReseniaRepository repository;
    
    @Override
    public Resenia save(Resenia resenia) {
        return repository.save(resenia);
    }
    
    @Override
    public Optional<Resenia> findById(Long id) {
        return repository.findById(id);
    }
    
    @Override
    public Optional<Resenia> findByIdAndDeletedFalse(Long id) {
        return repository.findByIdAndDeletedFalse(id);
    }
    
    @Override
    public Optional<Resenia> findByReservaIdAndDeletedFalse(Long reservaId) {
        return repository.findByReserva_IdAndDeletedFalse(reservaId);
    }
    
    @Override
    public List<Resenia> findByEmpresaIdAndDeletedFalse(Long empresaId) {
        return repository.findByEmpresa_IdAndDeletedFalse(empresaId);
    }
    
    @Override
    public List<Resenia> findByClienteIdAndDeletedFalse(String clienteId) {
        return repository.findByClienteIdAndDeletedFalse(clienteId);
    }
    
    @Override
    public List<Resenia> findByEmpleadoIdAndDeletedFalse(String empleadoId) {
        return repository.findByEmpleadoIdAndDeletedFalse(empleadoId);
    }
    
    @Override
    public Page<Resenia> findByEmpresaIdAndDeletedFalse(Long empresaId, Pageable pageable) {
        return repository.findByEmpresa_IdAndDeletedFalse(empresaId, pageable);
    }
    
    @Override
    public boolean existsByReservaIdAndDeletedFalse(Long reservaId) {
        return repository.existsByReserva_IdAndDeletedFalse(reservaId);
    }
    
    @Override
    public Double getAverageCalificacionByEmpresaId(Long empresaId) {
        return repository.getAverageCalificacionByEmpresaId(empresaId);
    }
    
    @Override
    public Double getAverageCalificacionByEmpleadoId(String empleadoId) {
        return repository.getAverageCalificacionByEmpleadoId(empleadoId);
    }
    
    @Override
    public Long countByEmpresaIdAndDeletedFalse(Long empresaId) {
        return repository.countByEmpresa_IdAndDeletedFalse(empresaId);
    }
}
