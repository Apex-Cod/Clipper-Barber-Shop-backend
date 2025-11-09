package apex.code.clipperBarberShop.recomendacion.adapters.in.web;

import apex.code.clipperBarberShop.Entities.enums.PublicoObjetivo;
import apex.code.clipperBarberShop.recomendacion.application.dto.EmpresaRecomendadaDTO;
import apex.code.clipperBarberShop.recomendacion.application.dto.ServicioRecomendadoDTO;
import apex.code.clipperBarberShop.recomendacion.application.service.RecomendacionService;
import apex.code.clipperBarberShop.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Controlador REST para recomendaciones personalizadas para clientes
 */
@RestController
@RequestMapping("/api/client/recomendaciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class RecomendacionClientController {

    private final RecomendacionService recomendacionService;

    /**
     * Obtiene servicios recomendados basados en el historial del cliente
     * 
     * @param publicoObjetivo Filtro opcional: HOMBRES, MUJERES, UNISEX, NIÑOS, NIÑAS
     * @param limit Cantidad máxima de resultados (por defecto 10)
     */
    @GetMapping("/servicios")
    public ResponseEntity<ApiResponse<List<ServicioRecomendadoDTO>>> obtenerServiciosRecomendados(
            @RequestParam(required = false) PublicoObjetivo publicoObjetivo,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            Principal principal) {
        
        List<ServicioRecomendadoDTO> servicios = recomendacionService.recomendarServicios(
                principal.getName(), 
                publicoObjetivo, 
                limit);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Servicios recomendados obtenidos exitosamente", 
                servicios));
    }

    /**
     * Obtiene empresas recomendadas basadas en el historial del cliente
     * 
     * @param publicoObjetivo Filtro opcional: HOMBRES, MUJERES, UNISEX, NIÑOS, NIÑAS
     * @param limit Cantidad máxima de resultados (por defecto 10)
     */
    @GetMapping("/empresas")
    public ResponseEntity<ApiResponse<List<EmpresaRecomendadaDTO>>> obtenerEmpresasRecomendadas(
            @RequestParam(required = false) PublicoObjetivo publicoObjetivo,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            Principal principal) {
        
        List<EmpresaRecomendadaDTO> empresas = recomendacionService.recomendarEmpresas(
                principal.getName(), 
                publicoObjetivo, 
                limit);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Empresas recomendadas obtenidas exitosamente", 
                empresas));
    }

    /**
     * Obtiene los mejores servicios filtrados por género/público objetivo
     * No requiere historial del cliente
     * 
     * @param publicoObjetivo HOMBRES, MUJERES, UNISEX, NIÑOS, NIÑAS
     * @param limit Cantidad máxima de resultados (por defecto 10)
     */
    @GetMapping("/servicios/por-genero")
    public ResponseEntity<ApiResponse<List<ServicioRecomendadoDTO>>> obtenerMejoresServiciosPorGenero(
            @RequestParam PublicoObjetivo publicoObjetivo,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        
        List<ServicioRecomendadoDTO> servicios = recomendacionService
                .obtenerMejoresServiciosPorGenero(publicoObjetivo, limit);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Mejores servicios para " + publicoObjetivo.getDescripcion(), 
                servicios));
    }
}
