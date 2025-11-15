package apex.code.clipperBarberShop.reporte.application.dto;

import lombok.*;

import java.time.LocalDate;

/**
 * DTO de respuesta para reporte consolidado
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteConsolidadoResponse {
    private Long empresaId;
    private String empresaNombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    
    // Métricas generales
    private Double ingresoTotal;
    private Integer totalReservas;
    private Integer reservasCompletadas;
    private Integer reservasCanceladas;
    private Double tasaCompletado; // Porcentaje de reservas completadas
    private Double tasaCancelacion; // Porcentaje de reservas canceladas
    
    // Métricas de servicios
    private Integer totalServicios;
    private String servicioMasPopular;
    private Integer reservasServicioMasPopular;
    
    // Métricas de empleados
    private Integer totalEmpleados;
    private Integer empleadosActivos;
    private String mejorEmpleado; // El que más ingresos generó
    private Double ingresosMejorEmpleado;
    
    // Métricas de clientes
    private Integer totalClientes;
    private Integer clientesNuevos;
    private Integer clientesRecurrentes;
    private Double promedioReservasPorCliente;
    
    // Promedios
    private Double ingresoPromedioPorDia;
    private Double ingresoPromedioPorReserva;
    private Double duracionPromedioPorReserva;
}
