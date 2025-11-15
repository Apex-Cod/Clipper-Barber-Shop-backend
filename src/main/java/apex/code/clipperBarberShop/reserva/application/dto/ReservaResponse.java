package apex.code.clipperBarberShop.reserva.application.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaResponse {
    private Long id;
    private Long empresaId;
    private String empresaNombre;
    private Long serviceId;
    private String serviceName;
    private String clientId;
    private String clientName;
    private String clientImageUrl;
    private String employeeId;
    private String employeeName;
    private String employeeImageUrl;
    private LocalDateTime reservationDate;
    private Integer duracionMinutos;
    private Double finalPrice;
    private String status;
    private LocalDateTime createdAt;
}
