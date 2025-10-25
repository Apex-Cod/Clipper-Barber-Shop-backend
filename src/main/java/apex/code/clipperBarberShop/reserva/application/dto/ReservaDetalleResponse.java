package apex.code.clipperBarberShop.reserva.application.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaDetalleResponse {
    private Long id;
    private Long empresaId;
    private String empresaNombre;
    
    // Información del servicio
    private Long serviceId;
    private String serviceName;
    private String serviceDescription;
    private Double servicePrice;
    private Integer serviceDuration;
    
    // Información del cliente
    private String clientId;
    private String clientName;
    private String clientEmail;
    
    // Información del empleado
    private String employeeId;
    private String employeeName;
    private String employeeEmail;
    
    // Información de la reserva
    private LocalDateTime reservationDate;
    private Integer duracionMinutos;
    private Double finalPrice;
    private String status;
    private LocalDateTime createdAt;
    
    // Promoción aplicada
    private Long promocionId;
    private String promocionNombre;
    private Double descuento;
    
    // Auditoría de borrado
    private Boolean deleted;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
