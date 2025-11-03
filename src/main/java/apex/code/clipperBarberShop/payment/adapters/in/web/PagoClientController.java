package apex.code.clipperBarberShop.payment.adapters.in.web;

import apex.code.clipperBarberShop.payment.application.dto.CrearPagoReservaRequest;
import apex.code.clipperBarberShop.payment.application.dto.PagoResponse;
import apex.code.clipperBarberShop.payment.application.dto.PaymentExecutionResponse;
import apex.code.clipperBarberShop.payment.application.service.PagoClientService;
import apex.code.clipperBarberShop.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Controlador REST para gestión de pagos por parte de los CLIENTES
 */
@RestController
@RequestMapping("/api/client/pagos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class PagoClientController {
    
    private final PagoClientService pagoClientService;
    
    /**
     * Crea un pago para una reserva
     * POST /api/client/pagos
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PagoResponse>> crearPago(
            @Valid @RequestBody CrearPagoReservaRequest request,
            Principal principal) {
        PagoResponse pago = pagoClientService.crearPagoReserva(request, principal.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pago creado exitosamente. Redirige al usuario a la URL de aprobación.", pago));
    }
    
    /**
     * Ejecuta un pago después de la aprobación de PayPal
     * POST /api/client/pagos/execute?paymentId=xxx&PayerID=xxx
     */
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<PaymentExecutionResponse>> ejecutarPago(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId,
            Principal principal) {
        PaymentExecutionResponse response = pagoClientService.ejecutarPago(paymentId, payerId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Pago completado exitosamente", response));
    }
    
    /**
     * Cancela un pago pendiente
     * DELETE /api/client/pagos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelarPago(
            @PathVariable Long id,
            Principal principal) {
        pagoClientService.cancelarPago(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Pago cancelado exitosamente", null));
    }
    
    /**
     * Obtiene los detalles de un pago
     * GET /api/client/pagos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PagoResponse>> obtenerPago(
            @PathVariable Long id,
            Principal principal) {
        PagoResponse pago = pagoClientService.obtenerPago(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Pago obtenido", pago));
    }
    
    /**
     * Lista todos los pagos del cliente autenticado
     * GET /api/client/pagos
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listarMisPagos(Principal principal) {
        List<PagoResponse> pagos = pagoClientService.listarMisPagos(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Pagos obtenidos", pagos));
    }
    
    /**
     * Obtiene el pago de una reserva específica
     * GET /api/client/pagos/reserva/{reservaId}
     */
    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<ApiResponse<PagoResponse>> obtenerPagoPorReserva(
            @PathVariable Long reservaId,
            Principal principal) {
        PagoResponse pago = pagoClientService.obtenerPagoPorReserva(reservaId, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Pago obtenido", pago));
    }
}
