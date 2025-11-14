package apex.code.clipperBarberShop.resenia.domain.port.out;

import apex.code.clipperBarberShop.Entities.Resenia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReseniaRepositoryPort {
    Resenia save(Resenia resenia);
    Optional<Resenia> findById(Long id);
    Optional<Resenia> findByIdAndDeletedFalse(Long id);
    Optional<Resenia> findByReservaIdAndDeletedFalse(Long reservaId);
    List<Resenia> findByEmpresaIdAndDeletedFalse(Long empresaId);
    List<Resenia> findByClienteIdAndDeletedFalse(String clienteId);
    List<Resenia> findByEmpleadoIdAndDeletedFalse(String empleadoId);
    Page<Resenia> findByEmpresaIdAndDeletedFalse(Long empresaId, Pageable pageable);
    boolean existsByReservaIdAndDeletedFalse(Long reservaId);
    Double getAverageCalificacionByEmpresaId(Long empresaId);
    Double getAverageCalificacionByEmpleadoId(String empleadoId);
    Long countByEmpresaIdAndDeletedFalse(Long empresaId);
}
