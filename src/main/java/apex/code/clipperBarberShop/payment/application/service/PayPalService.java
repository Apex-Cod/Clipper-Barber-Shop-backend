package apex.code.clipperBarberShop.payment.application.service;

import apex.code.clipperBarberShop.payment.config.PayPalConfig;
import apex.code.clipperBarberShop.payment.domain.exception.PaymentProcessException;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para integración con PayPal SDK
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayPalService {
    
    private final APIContext apiContext;
    private final PayPalConfig payPalConfig;
    
    /**
     * Crea un pago en PayPal
     */
    public Payment createPayment(
            Double total,
            String currency,
            String method,
            String intent,
            String description,
            String cancelUrl,
            String successUrl
    ) throws PayPalRESTException {
        
        // Validar monto
        if (total == null || total <= 0) {
            throw new PaymentProcessException("El monto debe ser mayor a 0");
        }
        
        // Crear monto
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format("%.2f", total));
        
        // Crear transacción
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);
        
        // Configurar pagador
        Payer payer = new Payer();
        payer.setPaymentMethod(method);
        
        // Crear pago
        Payment payment = new Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);
        
        // URLs de redirección
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);
        
        try {
            log.info("Creando pago en PayPal: {} {} - {}", total, currency, description);
            Payment createdPayment = payment.create(apiContext);
            log.info("Pago creado exitosamente en PayPal: {}", createdPayment.getId());
            return createdPayment;
        } catch (PayPalRESTException e) {
            log.error("Error al crear pago en PayPal", e);
            throw new PaymentProcessException("Error al crear pago en PayPal: " + e.getMessage(), e);
        }
    }
    
    /**
     * Ejecuta un pago aprobado por el usuario
     */
    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        try {
            log.info("Ejecutando pago en PayPal: {} con Payer ID: {}", paymentId, payerId);
            
            Payment payment = new Payment();
            payment.setId(paymentId);
            
            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);
            
            Payment executedPayment = payment.execute(apiContext, paymentExecution);
            log.info("Pago ejecutado exitosamente: {}", executedPayment.getId());
            return executedPayment;
        } catch (PayPalRESTException e) {
            log.error("Error al ejecutar pago en PayPal", e);
            throw new PaymentProcessException("Error al ejecutar pago en PayPal: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene los detalles de un pago
     */
    public Payment getPaymentDetails(String paymentId) throws PayPalRESTException {
        try {
            log.info("Obteniendo detalles del pago: {}", paymentId);
            return Payment.get(apiContext, paymentId);
        } catch (PayPalRESTException e) {
            log.error("Error al obtener detalles del pago", e);
            throw new PaymentProcessException("Error al obtener detalles del pago: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extrae la URL de aprobación de PayPal del pago creado
     */
    public String getApprovalUrl(Payment payment) {
        return payment.getLinks().stream()
                .filter(link -> "approval_url".equals(link.getRel()))
                .findFirst()
                .map(Links::getHref)
                .orElseThrow(() -> new PaymentProcessException("No se encontró URL de aprobación en el pago de PayPal"));
    }
    
    /**
     * Extrae el Sale ID del pago ejecutado
     */
    public String getSaleId(Payment payment) {
        return payment.getTransactions().stream()
                .flatMap(transaction -> transaction.getRelatedResources().stream())
                .map(RelatedResources::getSale)
                .filter(sale -> sale != null)
                .map(Sale::getId)
                .findFirst()
                .orElse(null);
    }
}
