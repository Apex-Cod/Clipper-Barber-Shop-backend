package apex.code.clipperBarberShop.plan.application.service;

import apex.code.clipperBarberShop.Entities.Plan;
import apex.code.clipperBarberShop.Entities.enums.TipoPlan;
import apex.code.clipperBarberShop.plan.application.dto.PlanResponse;
import apex.code.clipperBarberShop.plan.domain.exception.PlanNotFoundException;
import apex.code.clipperBarberShop.plan.domain.port.out.PlanRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PlanClientService {

    private final PlanRepositoryPort planRepository;

    /**
     * Lista todos los planes activos disponibles para clientes
     */
    public List<PlanResponse> listarPlanesActivos() {
        log.info("Listando planes activos disponibles");
        
        return planRepository.findAllByActivoTrueAndDeletedFalse()
                .stream()
                .map(PlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un plan específico por ID (solo si está activo)
     */
    public PlanResponse obtenerPlan(Long id) {
        log.info("Obteniendo plan ID: {}", id);
        
        Plan plan = planRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new PlanNotFoundException("Plan no encontrado con ID: " + id));

        if (!plan.getActivo()) {
            throw new PlanNotFoundException("El plan no está disponible actualmente");
        }

        return PlanResponse.fromEntity(plan);
    }

    /**
     * Obtiene un plan por tipo (GRATUITO, BASICO, PREMIUM)
     */
    public PlanResponse obtenerPlanPorTipo(TipoPlan tipo) {
        log.info("Obteniendo plan por tipo: {}", tipo);
        
        Plan plan = planRepository.findByTipoAndDeletedFalse(tipo)
                .orElseThrow(() -> new PlanNotFoundException("Plan no encontrado con tipo: " + tipo));

        if (!plan.getActivo()) {
            throw new PlanNotFoundException("El plan no está disponible actualmente");
        }

        return PlanResponse.fromEntity(plan);
    }
}
