package apex.code.clipperBarberShop.reporte.domain.port.in;

import apex.code.clipperBarberShop.reporte.application.dto.*;

import java.time.LocalDate;

/**
 * Puerto de entrada para casos de uso de reportes
 * Solo disponible para OWNERS
 */
public interface ReporteUseCase {
    
    /**
     * Obtiene el reporte de ingresos por período
     * 
     * @param empresaId ID de la empresa
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @param ownerId ID del owner que solicita el reporte
     * @return Reporte de ingresos
     */
    ReporteIngresosResponse obtenerReporteIngresos(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin, 
            String ownerId
    );
    
    /**
     * Obtiene el reporte de servicios más solicitados
     * 
     * @param empresaId ID de la empresa
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @param ownerId ID del owner que solicita el reporte
     * @return Reporte de servicios
     */
    ReporteServiciosResponse obtenerReporteServicios(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin, 
            String ownerId
    );
    
    /**
     * Obtiene el reporte de rendimiento de empleados
     * 
     * @param empresaId ID de la empresa
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @param ownerId ID del owner que solicita el reporte
     * @return Reporte de empleados
     */
    ReporteEmpleadosResponse obtenerReporteEmpleados(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin, 
            String ownerId
    );
    
    /**
     * Obtiene el reporte de clientes frecuentes
     * 
     * @param empresaId ID de la empresa
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @param ownerId ID del owner que solicita el reporte
     * @return Reporte de clientes
     */
    ReporteClientesResponse obtenerReporteClientes(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin, 
            String ownerId
    );
    
    /**
     * Obtiene un reporte consolidado con todas las métricas
     * 
     * @param empresaId ID de la empresa
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @param ownerId ID del owner que solicita el reporte
     * @return Reporte consolidado
     */
    ReporteConsolidadoResponse obtenerReporteConsolidado(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin, 
            String ownerId
    );
}
