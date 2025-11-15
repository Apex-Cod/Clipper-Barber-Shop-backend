package apex.code.clipperBarberShop.reporte.application.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para reporte de clientes
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteClientesResponse {
    private Long empresaId;
    private String empresaNombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer totalClientes;
    private Integer clientesNuevos;
    private Integer clientesRecurrentes;
    private List<ClienteEstadistica> topClientes;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClienteEstadistica {
        private String clienteId;
        private String clienteNombre;
        private String clienteEmail;
        private String profileImageUrl;
        private Integer cantidadReservas;
        private Double totalGastado;
        private LocalDateTime ultimaReserva;
        private String servicioFavorito;
    }
}
