package apex.code.clipperBarberShop.plan.adapters.in.web;

import apex.code.clipperBarberShop.plan.application.dto.ActualizarPlanRequest;
import apex.code.clipperBarberShop.plan.application.dto.PlanRequest;
import apex.code.clipperBarberShop.plan.application.dto.PlanResponse;
import apex.code.clipperBarberShop.plan.application.service.PlanOwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de planes (solo ADMIN)
 */
@RestController
@RequestMapping("/api/admin/planes")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class PlanOwnerController {

    private final PlanOwnerService planOwnerService;

    /**
     * POST /api/admin/planes - Crear un nuevo plan
     */
    @PostMapping
    public ResponseEntity<PlanResponse> crearPlan(@Valid @RequestBody PlanRequest request) {
        log.info("Solicitud para crear plan: {}", request.getNombre());
        PlanResponse response = planOwnerService.crearPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/admin/planes/{id} - Obtener un plan por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlanResponse> obtenerPlan(@PathVariable Long id) {
        log.info("Solicitud para obtener plan ID: {}", id);
        PlanResponse response = planOwnerService.obtenerPlan(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/planes - Listar todos los planes
     */
    @GetMapping
    public ResponseEntity<List<PlanResponse>> listarPlanes() {
        log.info("Solicitud para listar todos los planes");
        List<PlanResponse> planes = planOwnerService.listarTodosLosPlanes();
        return ResponseEntity.ok(planes);
    }

    /**
     * GET /api/admin/planes/paginados - Listar planes con paginación
     */
    @GetMapping("/paginados")
    public ResponseEntity<Page<PlanResponse>> listarPlanesPaginados(
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Solicitud para listar planes paginados");
        Page<PlanResponse> planes = planOwnerService.listarPlanesPaginados(pageable);
        return ResponseEntity.ok(planes);
    }

    /**
     * PUT /api/admin/planes/{id} - Actualizar un plan
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlanResponse> actualizarPlan(
            @PathVariable Long id, 
            @Valid @RequestBody ActualizarPlanRequest request) {
        log.info("Solicitud para actualizar plan ID: {}", id);
        PlanResponse response = planOwnerService.actualizarPlan(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/admin/planes/{id}/estado - Activar o desactivar un plan
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PlanResponse> cambiarEstadoPlan(
            @PathVariable Long id,
            @RequestParam boolean activo) {
        log.info("Solicitud para cambiar estado del plan ID {} a: {}", id, activo);
        PlanResponse response = planOwnerService.cambiarEstadoPlan(id, activo);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/admin/planes/{id} - Eliminar un plan (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPlan(@PathVariable Long id) {
        log.info("Solicitud para eliminar plan ID: {}", id);
        planOwnerService.eliminarPlan(id);
        return ResponseEntity.noContent().build();
    }
}
