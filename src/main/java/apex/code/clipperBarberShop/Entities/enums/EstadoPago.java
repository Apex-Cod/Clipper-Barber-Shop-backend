package apex.code.clipperBarberShop.Entities.enums;

/**
 * Estados posibles de un pago
 */
public enum EstadoPago {
    PENDIENTE,      // Pago creado pero no procesado
    PROCESANDO,     // Pago en proceso
    COMPLETADO,     // Pago exitoso
    FALLIDO,        // Pago rechazado o fallido
    CANCELADO,      // Pago cancelado por el usuario
    REEMBOLSADO,    // Pago reembolsado
    EXPIRADO        // Pago expirado sin completarse
}
