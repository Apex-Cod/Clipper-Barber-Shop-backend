package apex.code.clipperBarberShop.Entities.enums;

/**
 * Estado de una suscripción de plan
 */
public enum EstadoSuscripcion {
    /**
     * Suscripción activa y pagada
     */
    ACTIVA,
    
    /**
     * Suscripción que ha llegado a su fecha de fin
     */
    EXPIRADA,
    
    /**
     * Suscripción cancelada manualmente por el usuario
     */
    CANCELADA,
    
    /**
     * Suscripción creada pero aún no pagada
     */
    PENDIENTE_PAGO
}
