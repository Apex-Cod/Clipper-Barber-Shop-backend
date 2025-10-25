package apex.code.clipperBarberShop.servicio.domain.port.out;

import apex.code.clipperBarberShop.Entities.Servicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para operaciones de repositorio de Servicio
 */
public interface ServicioRepositoryPort {
    
    Servicio save(Servicio servicio);
    
    Optional<Servicio> findById(Long id);
    
    Optional<Servicio> findByIdAndDeletedFalse(Long id);
    
    List<Servicio> findByEmpresaIdAndDeletedFalse(Long empresaId);
    
    Page<Servicio> findByEmpresaIdAndDeletedFalse(Long empresaId, Pageable pageable);
    
    List<Servicio> findByEmpresaIdAndCategoriaAndDeletedFalse(Long empresaId, String categoria);
    
    boolean existsByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);
    
    void delete(Servicio servicio);
}
