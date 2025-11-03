package apex.code.clipperBarberShop.payment.application.dto;

import apex.code.clipperBarberShop.Entities.Pago;
import apex.code.clipperBarberShop.Entities.enums.EstadoPago;
import apex.code.clipperBarberShop.Entities.enums.MetodoPago;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response con los datos de un pago
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoResponse {
    
    private Long id;
    private Long reservaId;
    private Long empresaId;
    private String empresaNombre;
    private BigDecimal monto;
    private String moneda;
    private MetodoPago metodoPago;
    private EstadoPago estado;
    private String paypalPaymentId;
    private String descripcion;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaCreacion;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaPago;
    
    private String notas;
    
    // URL de aprobación de PayPal (solo cuando el pago está pendiente)
    private String approvalUrl;
    
    /**
     * Convierte una entidad Pago a PagoResponse
     */
    public static PagoResponse fromDomain(Pago pago) {
        return PagoResponse.builder()
                .id(pago.getId())
                .reservaId(pago.getReserva() != null ? pago.getReserva().getId() : null)
                .empresaId(pago.getEmpresa().getId())
                .empresaNombre(pago.getEmpresa().getNombre())
                .monto(pago.getMonto())
                .moneda(pago.getMoneda())
                .metodoPago(pago.getMetodoPago())
                .estado(pago.getEstado())
                .paypalPaymentId(pago.getPaypalPaymentId())
                .descripcion(pago.getDescripcion())
                .fechaCreacion(pago.getFechaCreacion())
                .fechaPago(pago.getFechaPago())
                .notas(pago.getNotas())
                .build();
    }
    
    /**
     * Convierte una entidad Pago a PagoResponse con URL de aprobación
     */
    public static PagoResponse fromDomainWithApprovalUrl(Pago pago, String approvalUrl) {
        PagoResponse response = fromDomain(pago);
        response.setApprovalUrl(approvalUrl);
        return response;
    }
}
