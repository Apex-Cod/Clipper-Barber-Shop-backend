package apex.code.clipperBarberShop.plan.application.service;

import apex.code.clipperBarberShop.Entities.Plan;
import apex.code.clipperBarberShop.plan.application.dto.ActualizarPlanRequest;
import apex.code.clipperBarberShop.plan.application.dto.PlanRequest;
import apex.code.clipperBarberShop.plan.application.dto.PlanResponse;
import apex.code.clipperBarberShop.plan.domain.exception.PlanAlreadyExistsException;
import apex.code.clipperBarberShop.plan.domain.exception.PlanNotFoundException;
import apex.code.clipperBarberShop.plan.domain.port.out.PlanRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlanOwnerService {

    private final PlanRepositoryPort planRepository;

    /**
     * Crea un nuevo plan (solo ADMIN)
     */
    public PlanResponse crearPlan(PlanRequest request) {
        log.info("Creando nuevo plan: {}", request.getNombre());

        // Validar que no exista un plan con el mismo tipo
        if (planRepository.existsByTipo(request.getTipo())) {
            throw new PlanAlreadyExistsException(
                "Ya existe un plan con el tipo: " + request.getTipo()
            );
        }

        Plan plan = Plan.builder()
                .tipo(request.getTipo())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .duracionMeses(request.getDuracionMeses())
                .limiteUsuarios(request.getLimiteUsuarios())
                .limiteReservasMes(request.getLimiteReservasMes())
                .limiteServicios(request.getLimiteServicios())
                .limitePromociones(request.getLimitePromociones())
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();

        Plan planGuardado = planRepository.save(plan);
        log.info("Plan creado exitosamente con ID: {}", planGuardado.getId());

        return PlanResponse.fromEntity(planGuardado);
    }

    /**
     * Obtiene un plan por ID
     */
    @Transactional(readOnly = true)
    public PlanResponse obtenerPlan(Long id) {
        Plan plan = planRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new PlanNotFoundException("Plan no encontrado con ID: " + id));
        
        return PlanResponse.fromEntity(plan);
    }

    /**
     * Lista todos los planes (incluidos los inactivos)
     */
    @Transactional(readOnly = true)
    public List<PlanResponse> listarTodosLosPlanes() {
        return planRepository.findAllByDeletedFalse()
                .stream()
                .map(PlanResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lista planes con paginación
     */
    @Transactional(readOnly = true)
    public Page<PlanResponse> listarPlanesPaginados(Pageable pageable) {
        return planRepository.findAllByDeletedFalse(pageable)
                .map(PlanResponse::fromEntity);
    }

    /**
     * Actualiza un plan existente
     */
    public PlanResponse actualizarPlan(Long id, ActualizarPlanRequest request) {
        log.info("Actualizando plan ID: {}", id);

        Plan plan = planRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new PlanNotFoundException("Plan no encontrado con ID: " + id));

        // Actualizar solo los campos que no son null
        if (request.getNombre() != null) {
            plan.setNombre(request.getNombre());
        }
        if (request.getDescripcion() != null) {
            plan.setDescripcion(request.getDescripcion());
        }
        if (request.getPrecio() != null) {
            plan.setPrecio(request.getPrecio());
        }
        if (request.getDuracionMeses() != null) {
            plan.setDuracionMeses(request.getDuracionMeses());
        }
        if (request.getLimiteUsuarios() != null) {
            plan.setLimiteUsuarios(request.getLimiteUsuarios());
        }
        if (request.getLimiteReservasMes() != null) {
            plan.setLimiteReservasMes(request.getLimiteReservasMes());
        }
        if (request.getLimiteServicios() != null) {
            plan.setLimiteServicios(request.getLimiteServicios());
        }
        if (request.getLimitePromociones() != null) {
            plan.setLimitePromociones(request.getLimitePromociones());
        }
        if (request.getActivo() != null) {
            plan.setActivo(request.getActivo());
        }

        Plan planActualizado = planRepository.save(plan);
        log.info("Plan actualizado exitosamente: {}", planActualizado.getId());

        return PlanResponse.fromEntity(planActualizado);
    }

    /**
     * Desactiva un plan (soft delete lógico)
     */
    public void eliminarPlan(Long id) {
        log.info("Eliminando plan ID: {}", id);

        Plan plan = planRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new PlanNotFoundException("Plan no encontrado con ID: " + id));

        plan.setDeleted(true);
        planRepository.save(plan);
        
        log.info("Plan eliminado exitosamente: {}", id);
    }

    /**
     * Activa o desactiva un plan
     */
    public PlanResponse cambiarEstadoPlan(Long id, boolean activo) {
        log.info("Cambiando estado del plan ID {} a: {}", id, activo);

        Plan plan = planRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new PlanNotFoundException("Plan no encontrado con ID: " + id));

        plan.setActivo(activo);
        Plan planActualizado = planRepository.save(plan);

        log.info("Estado del plan cambiado exitosamente");
        return PlanResponse.fromEntity(planActualizado);
    }
}
