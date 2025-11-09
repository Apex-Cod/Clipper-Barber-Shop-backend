package apex.code.clipperBarberShop.plan.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Suscripcion;
import apex.code.clipperBarberShop.Entities.enums.EstadoSuscripcion;
import apex.code.clipperBarberShop.plan.domain.port.out.SuscripcionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SuscripcionRepositoryAdapter implements SuscripcionRepositoryPort {
    
    private final SpringDataSuscripcionRepository repository;

    @Override
    public Suscripcion save(Suscripcion suscripcion) {
        return repository.save(suscripcion);
    }

    @Override
    public Optional<Suscripcion> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Suscripcion> findByEmpresaIdAndEstado(Long empresaId, EstadoSuscripcion estado) {
        return repository.findByEmpresaIdAndEstadoAndDeletedFalse(empresaId, estado);
    }

    @Override
    public List<Suscripcion> findByEmpresaId(Long empresaId) {
        return repository.findByEmpresaIdAndDeletedFalseOrderByFechaCreacionDesc(empresaId);
    }

    @Override
    public List<Suscripcion> findByEstado(EstadoSuscripcion estado) {
        return repository.findByEstadoAndDeletedFalse(estado);
    }

    @Override
    public List<Suscripcion> findByFechaFin(LocalDate fecha) {
        return repository.findByFechaFinAndDeletedFalse(fecha);
    }

    @Override
    public List<Suscripcion> findByFechaFinBetween(LocalDate desde, LocalDate hasta) {
        return repository.findByFechaFinBetweenAndDeletedFalse(desde, hasta);
    }

    @Override
    public List<Suscripcion> findActivasExpiradas() {
        return repository.findActivasExpiradas(LocalDate.now());
    }

    @Override
    public List<Suscripcion> findProximasAVencer(int diasAnticipacion) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(diasAnticipacion);
        return repository.findProximasAVencer(hoy, fechaLimite);
    }

    @Override
    public boolean empresaTieneSuscripcionActiva(Long empresaId) {
        return repository.existsByEmpresaIdAndEstadoAndDeletedFalse(empresaId, EstadoSuscripcion.ACTIVA);
    }
}
