package apex.code.clipperBarberShop.plan.adapters.in.web;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Plan;
import apex.code.clipperBarberShop.Entities.Suscripcion;
import apex.code.clipperBarberShop.register.domain.port.out.EmpresaRepositoryPort;
import apex.code.clipperBarberShop.plan.application.dto.CrearSuscripcionRequest;
import apex.code.clipperBarberShop.plan.application.dto.SuscripcionResponse;
import apex.code.clipperBarberShop.plan.application.mapper.SuscripcionMapper;
import apex.code.clipperBarberShop.plan.application.service.SuscripcionService;
import apex.code.clipperBarberShop.plan.domain.port.out.PlanRepositoryPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para gestión de suscripciones (OWNER)
 * Los owners pueden consultar y gestionar sus propias suscripciones
 */
@RestController
@RequestMapping("/api/owner/suscripciones")
@RequiredArgsConstructor
public class SuscripcionOwnerController {
    
    private final SuscripcionService suscripcionService;
    private final SuscripcionMapper mapper;
    private final EmpresaRepositoryPort empresaRepository;
    private final PlanRepositoryPort planRepository;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SuscripcionResponse> crearSuscripcion(
            @Valid @RequestBody CrearSuscripcionRequest request) {
        
        Empresa empresa = empresaRepository.findById(request.getEmpresaId())
            .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
            
        Plan plan = planRepository.findById(request.getPlanId())
            .orElseThrow(() -> new RuntimeException("Plan no encontrado"));
        
        Suscripcion suscripcion = suscripcionService.crearSuscripcion(empresa, plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(suscripcion));
    }

    @GetMapping("/activa")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SuscripcionResponse> obtenerSuscripcionActiva(
            @RequestParam Long empresaId) {
        
        Suscripcion suscripcion = suscripcionService.obtenerSuscripcionActiva(empresaId);
        return ResponseEntity.ok(mapper.toResponse(suscripcion));
    }

    @GetMapping("/historial")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<SuscripcionResponse>> obtenerHistorial(
            @RequestParam Long empresaId) {
        
        List<Suscripcion> suscripciones = suscripcionService.obtenerHistorialSuscripciones(empresaId);
        List<SuscripcionResponse> response = suscripciones.stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SuscripcionService.SuscripcionInfo> obtenerInfo(
            @RequestParam Long empresaId) {
        
        SuscripcionService.SuscripcionInfo info = suscripcionService.obtenerInfoSuscripcion(empresaId);
        return ResponseEntity.ok(info);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SuscripcionResponse> cancelarSuscripcion(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        
        Suscripcion suscripcion = suscripcionService.cancelarSuscripcion(
            id, 
            motivo != null ? motivo : "Cancelada por el usuario"
        );
        
        return ResponseEntity.ok(mapper.toResponse(suscripcion));
    }

    @PostMapping("/{id}/renovar")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SuscripcionResponse> renovarSuscripcion(@PathVariable Long id) {
        Suscripcion nuevaSuscripcion = suscripcionService.renovarSuscripcion(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(nuevaSuscripcion));
    }
}
