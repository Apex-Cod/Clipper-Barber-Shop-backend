package apex.code.clipperBarberShop.reserva.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuesta del dashboard del empleado
 * Contiene estadísticas y resumen de reservas del día
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardEmpleadoResponse {
    
    private String fecha;
    private Integer totalReservas;
    private Integer reservasPendientes;
    private Integer reservasConfirmadas;
    private Integer reservasCompletadas;
    private Integer reservasCanceladas;
    private List<ReservaResponse> proximasReservas;
}
