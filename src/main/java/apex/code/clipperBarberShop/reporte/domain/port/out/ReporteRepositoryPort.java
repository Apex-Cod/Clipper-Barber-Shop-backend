package apex.code.clipperBarberShop.reporte.domain.port.out;

import apex.code.clipperBarberShop.Entities.Reserva;

import java.time.LocalDate;
import java.util.List;

/**
 * Puerto de salida para operaciones de reportes
 */
public interface ReporteRepositoryPort {
    
    /**
     * Obtiene todas las reservas de una empresa en un período de tiempo
     * 
     * @param empresaId ID de la empresa
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de reservas
     */
    List<Reserva> findReservasByEmpresaAndPeriodo(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin
    );
    
    /**
     * Obtiene todas las reservas completadas de una empresa en un período
     * 
     * @param empresaId ID de la empresa
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de reservas completadas
     */
    List<Reserva> findReservasCompletadasByEmpresaAndPeriodo(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin
    );
    
    /**
     * Cuenta el número de clientes únicos que hicieron reservas en un período
     * 
     * @param empresaId ID de la empresa
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Número de clientes únicos
     */
    Long countClientesUnicosByEmpresaAndPeriodo(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin
    );
    
    /**
     * Encuentra clientes nuevos (primera reserva en el período)
     * 
     * @param empresaId ID de la empresa
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de IDs de clientes nuevos
     */
    List<String> findClientesNuevosByPeriodo(
            Long empresaId, 
            LocalDate fechaInicio, 
            LocalDate fechaFin
    );
}
