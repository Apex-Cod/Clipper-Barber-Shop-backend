package apex.code.clipperBarberShop.resenia.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Resenia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReseniaRepository extends JpaRepository<Resenia, Long> {
    Optional<Resenia> findByIdAndDeletedFalse(Long id);
    
    Optional<Resenia> findByReserva_IdAndDeletedFalse(Long reservaId);
    
    List<Resenia> findByEmpresa_IdAndDeletedFalse(Long empresaId);
    
    List<Resenia> findByClienteIdAndDeletedFalse(String clienteId);
    
    List<Resenia> findByEmpleadoIdAndDeletedFalse(String empleadoId);
    
    Page<Resenia> findByEmpresa_IdAndDeletedFalse(Long empresaId, Pageable pageable);
    
    boolean existsByReserva_IdAndDeletedFalse(Long reservaId);
    
    @Query("SELECT AVG(r.calificacionServicio) FROM Resenia r WHERE r.empresa.id = :empresaId AND r.deleted = false")
    Double getAverageCalificacionByEmpresaId(@Param("empresaId") Long empresaId);
    
    @Query("SELECT AVG(r.calificacionEmpleado) FROM Resenia r WHERE r.empleadoId = :empleadoId AND r.calificacionEmpleado IS NOT NULL AND r.deleted = false")
    Double getAverageCalificacionByEmpleadoId(@Param("empleadoId") String empleadoId);
    
    Long countByEmpresa_IdAndDeletedFalse(Long empresaId);
}
