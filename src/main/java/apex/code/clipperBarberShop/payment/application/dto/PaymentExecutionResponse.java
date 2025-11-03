package apex.code.clipperBarberShop.payment.application.dto;

import lombok.*;

/**
 * Response para el resultado de ejecución de un pago
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentExecutionResponse {
    private Long pagoId;
    private String estado;
    private String mensaje;
    private String paypalSaleId;
}
