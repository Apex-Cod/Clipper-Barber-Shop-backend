package apex.code.clipperBarberShop.plan.adapters.in.web;

import apex.code.clipperBarberShop.Entities.enums.TipoPlan;
import apex.code.clipperBarberShop.plan.application.dto.PlanResponse;
import apex.code.clipperBarberShop.plan.application.service.PlanClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para consultar planes disponibles (OWNER solamente)
 * Los planes son para dueños de barberías que necesitan pagar para realizar acciones
 */
@RestController
@RequestMapping("/api/owner/planes")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('OWNER')")
public class PlanOwnerQueryController {

    private final PlanClientService planClientService;

    /**
     * GET /api/owner/planes - Listar todos los planes activos disponibles para suscripción
     */
    @GetMapping
    public ResponseEntity<List<PlanResponse>> listarPlanesActivos() {
        log.info("OWNER solicitando lista de planes activos");
        List<PlanResponse> planes = planClientService.listarPlanesActivos();
        return ResponseEntity.ok(planes);
    }

    /**
     * GET /api/owner/planes/{id} - Obtener un plan específico por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlanResponse> obtenerPlan(@PathVariable Long id) {
        log.info("OWNER solicitando plan ID: {}", id);
        PlanResponse response = planClientService.obtenerPlan(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/owner/planes/tipo/{tipo} - Obtener un plan por tipo (GRATUITO, BASICO, PREMIUM)
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<PlanResponse> obtenerPlanPorTipo(@PathVariable TipoPlan tipo) {
        log.info("OWNER solicitando plan por tipo: {}", tipo);
        PlanResponse response = planClientService.obtenerPlanPorTipo(tipo);
        return ResponseEntity.ok(response);
    }
}
