package apex.code.clipperBarberShop.payment.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Pago;
import apex.code.clipperBarberShop.Entities.Reserva;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.Entities.enums.EstadoPago;
import apex.code.clipperBarberShop.Entities.enums.MetodoPago;
import apex.code.clipperBarberShop.payment.application.dto.CrearPagoReservaRequest;
import apex.code.clipperBarberShop.payment.application.dto.PagoResponse;
import apex.code.clipperBarberShop.payment.application.dto.PaymentExecutionResponse;
import apex.code.clipperBarberShop.payment.config.PayPalConfig;
import apex.code.clipperBarberShop.payment.domain.exception.PagoAccessDeniedException;
import apex.code.clipperBarberShop.payment.domain.exception.PagoNotFoundException;
import apex.code.clipperBarberShop.payment.domain.exception.PagoYaExisteException;
import apex.code.clipperBarberShop.payment.domain.exception.PaymentProcessException;
import apex.code.clipperBarberShop.payment.domain.port.out.PagoRepositoryPort;
import apex.code.clipperBarberShop.register.domain.port.out.UsuarioRepositoryPort;
import apex.code.clipperBarberShop.reserva.domain.port.out.ReservaRepositoryPort;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para gestión de pagos (CLIENTE)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PagoClientService {
    
    private final PagoRepositoryPort pagoRepository;
    private final ReservaRepositoryPort reservaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final PayPalService payPalService;
    private final PayPalConfig payPalConfig;
    
    /**
     * Crea un pago para una reserva
     */
    @Transactional
    public PagoResponse crearPagoReserva(CrearPagoReservaRequest request, String clienteId) {
        log.info("Creando pago para reserva {} por cliente {}", request.getReservaId(), clienteId);
        
        // Validar que la reserva existe y pertenece al cliente
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(request.getReservaId())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        
        if (!reserva.getClientId().equals(clienteId)) {
            throw new PagoAccessDeniedException("No tienes permiso para pagar esta reserva");
        }
        
        // Validar que no exista ya un pago completado para esta reserva
        if (pagoRepository.existsByReservaIdAndEstadoAndDeletedFalse(request.getReservaId(), EstadoPago.COMPLETADO)) {
            throw new PagoYaExisteException(request.getReservaId());
        }
        
        // Validar que la reserva esté confirmada
        if (!"CONFIRMED".equalsIgnoreCase(reserva.getStatus())) {
            throw new PaymentProcessException("La reserva debe estar confirmada para poder pagar");
        }
        
        try {
            // Crear pago en PayPal
            String currency = request.getMoneda() != null ? request.getMoneda() : "USD";
            String description = request.getDescripcion() != null ? 
                    request.getDescripcion() : 
                    "Pago de reserva #" + reserva.getId();
            
            // URLs de retorno con el ID de la reserva
            String cancelUrl = payPalConfig.getCancelUrl() + "?reservaId=" + reserva.getId();
            String successUrl = payPalConfig.getSuccessUrl() + "?reservaId=" + reserva.getId();
            
            Payment paypalPayment = payPalService.createPayment(
                    request.getMonto(),
                    currency,
                    "paypal",
                    "sale",
                    description,
                    cancelUrl,
                    successUrl
            );
            
            // Guardar pago en la base de datos
            Pago pago = Pago.builder()
                    .reserva(reserva)
                    .empresa(reserva.getEmpresa())
                    .monto(BigDecimal.valueOf(request.getMonto()))
                    .moneda(currency)
                    .metodoPago(MetodoPago.PAYPAL)
                    .estado(EstadoPago.PENDIENTE)
                    .paypalPaymentId(paypalPayment.getId())
                    .descripcion(description)
                    .build();
            
            pago = pagoRepository.save(pago);
            
            // Obtener URL de aprobación
            String approvalUrl = payPalService.getApprovalUrl(paypalPayment);
            
            log.info("Pago creado exitosamente: ID={}, PayPal ID={}", pago.getId(), paypalPayment.getId());
            return PagoResponse.fromDomainWithApprovalUrl(pago, approvalUrl);
            
        } catch (PayPalRESTException e) {
            log.error("Error al crear pago en PayPal", e);
            throw new PaymentProcessException("Error al crear pago en PayPal: " + e.getMessage(), e);
        }
    }
    
    /**
     * Ejecuta un pago después de que el usuario lo apruebe en PayPal
     */
    @Transactional
    public PaymentExecutionResponse ejecutarPago(String paymentId, String payerId, String clienteId) {
        log.info("Ejecutando pago PayPal: {} por cliente {}", paymentId, clienteId);
        
        // Buscar el pago en la base de datos
        Pago pago = pagoRepository.findByPaypalPaymentId(paymentId)
                .orElseThrow(() -> new PagoNotFoundException(paymentId));
        
        // Validar que el pago pertenece a una reserva del cliente
        if (!pago.getReserva().getClientId().equals(clienteId)) {
            throw new PagoAccessDeniedException("No tienes permiso para ejecutar este pago");
        }
        
        // Validar que el pago esté pendiente
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new PaymentProcessException("El pago no está en estado pendiente");
        }
        
        try {
            // Ejecutar pago en PayPal
            Payment executedPayment = payPalService.executePayment(paymentId, payerId);
            
            // Verificar que el pago fue aprobado
            if (!"approved".equalsIgnoreCase(executedPayment.getState())) {
                pago.marcarComoFallido("Pago no aprobado por PayPal");
                pagoRepository.save(pago);
                throw new PaymentProcessException("El pago no fue aprobado por PayPal");
            }
            
            // Obtener Sale ID
            String saleId = payPalService.getSaleId(executedPayment);
            
            // Actualizar pago en la base de datos
            pago.marcarComoCompletado(payerId, saleId);
            pago = pagoRepository.save(pago);
            
            // Actualizar estado de la reserva a COMPLETED si es necesario
            // (Esto podría hacerse automáticamente o manualmente por el dueño)
            
            log.info("Pago ejecutado exitosamente: ID={}, Sale ID={}", pago.getId(), saleId);
            
            return PaymentExecutionResponse.builder()
                    .pagoId(pago.getId())
                    .estado("COMPLETADO")
                    .mensaje("Pago completado exitosamente")
                    .paypalSaleId(saleId)
                    .build();
            
        } catch (PayPalRESTException e) {
            log.error("Error al ejecutar pago en PayPal", e);
            pago.marcarComoFallido("Error al ejecutar pago: " + e.getMessage());
            pagoRepository.save(pago);
            throw new PaymentProcessException("Error al ejecutar pago en PayPal: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cancela un pago pendiente
     */
    @Transactional
    public void cancelarPago(Long pagoId, String clienteId) {
        log.info("Cancelando pago {} por cliente {}", pagoId, clienteId);
        
        Pago pago = pagoRepository.findByIdAndDeletedFalse(pagoId)
                .orElseThrow(() -> new PagoNotFoundException(pagoId));
        
        // Validar que el pago pertenece a una reserva del cliente
        if (!pago.getReserva().getClientId().equals(clienteId)) {
            throw new PagoAccessDeniedException("No tienes permiso para cancelar este pago");
        }
        
        // Solo se pueden cancelar pagos pendientes
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new PaymentProcessException("Solo se pueden cancelar pagos pendientes");
        }
        
        pago.marcarComoCancelado("Cancelado por el cliente");
        pagoRepository.save(pago);
        
        log.info("Pago cancelado exitosamente: {}", pagoId);
    }
    
    /**
     * Obtiene los detalles de un pago
     */
    public PagoResponse obtenerPago(Long pagoId, String clienteId) {
        Pago pago = pagoRepository.findByIdAndDeletedFalse(pagoId)
                .orElseThrow(() -> new PagoNotFoundException(pagoId));
        
        // Validar que el pago pertenece a una reserva del cliente
        if (!pago.getReserva().getClientId().equals(clienteId)) {
            throw new PagoAccessDeniedException("No tienes permiso para ver este pago");
        }
        
        return PagoResponse.fromDomain(pago);
    }
    
    /**
     * Lista todos los pagos del cliente
     */
    public List<PagoResponse> listarMisPagos(String clienteId) {
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        
        // Obtener todas las reservas del cliente y luego sus pagos
        List<Reserva> reservas = reservaRepository.findByClientIdAndDeletedFalse(clienteId);
        
        return reservas.stream()
                .map(Reserva::getId)
                .map(pagoRepository::findByReservaIdAndDeletedFalse)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(PagoResponse::fromDomain)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene el pago de una reserva específica
     */
    public PagoResponse obtenerPagoPorReserva(Long reservaId, String clienteId) {
        // Validar que la reserva pertenece al cliente
        Reserva reserva = reservaRepository.findByIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        
        if (!reserva.getClientId().equals(clienteId)) {
            throw new PagoAccessDeniedException("No tienes permiso para ver el pago de esta reserva");
        }
        
        Pago pago = pagoRepository.findByReservaIdAndDeletedFalse(reservaId)
                .orElseThrow(() -> new PagoNotFoundException("No se encontró pago para la reserva " + reservaId));
        
        return PagoResponse.fromDomain(pago);
    }
}
