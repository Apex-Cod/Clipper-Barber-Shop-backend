package apex.code.clipperBarberShop.servicio.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para información pública de empleados
 * Solo contiene datos no sensibles para clientes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoPublicoDTO {
    
    /**
     * ID único del empleado
     */
    private String id;
    
    /**
     * Nombre del empleado
     */
    private String nombre;
    
    /**
     * Apellido del empleado
     */
    private String apellido;
    
    /**
     * Estado activo del empleado
     */
    private boolean activo;
    
    /**
     * ID de la empresa a la que pertenece
     */
    private Long empresaId;
    
    // NOTA: Intencionalmente NO incluimos:
    // - email (información sensible)
    // - teléfono (información sensible)
    // - fechas de creación/modificación
    // - información de roles específicos
    // - datos de contacto personal
}
