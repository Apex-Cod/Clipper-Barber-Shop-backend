package apex.code.clipperBarberShop.payment.adapters.in.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controlador para callbacks de PayPal (success y cancel)
 * Estos endpoints son públicos ya que PayPal los llama directamente
 */
@Controller
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackController {
    
    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    /**
     * Callback de éxito de PayPal
     * PayPal redirige aquí después de que el usuario aprueba el pago
     * GET /api/payments/success?paymentId=xxx&token=xxx&PayerID=xxx&reservaId=xxx
     */
    @GetMapping("/success")
    public RedirectView paymentSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId,
            @RequestParam(value = "reservaId", required = false) Long reservaId) {
        
        log.info("Payment success callback: paymentId={}, payerId={}, reservaId={}", 
                paymentId, payerId, reservaId);
        
        // Redirigir al frontend con los parámetros necesarios para ejecutar el pago
        String redirectUrl = String.format("%s/payment/success?paymentId=%s&PayerID=%s&reservaId=%s",
                frontendUrl, paymentId, payerId, reservaId != null ? reservaId : "");
        
        return new RedirectView(redirectUrl);
    }
    
    /**
     * Callback de cancelación de PayPal
     * PayPal redirige aquí si el usuario cancela el pago
     * GET /api/payments/cancel?token=xxx&reservaId=xxx
     */
    @GetMapping("/cancel")
    public RedirectView paymentCancel(
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "reservaId", required = false) Long reservaId) {
        
        log.info("Payment cancel callback: token={}, reservaId={}", token, reservaId);
        
        // Redirigir al frontend informando de la cancelación
        String redirectUrl = String.format("%s/payment/cancel?reservaId=%s",
                frontendUrl, reservaId != null ? reservaId : "");
        
        return new RedirectView(redirectUrl);
    }
}
