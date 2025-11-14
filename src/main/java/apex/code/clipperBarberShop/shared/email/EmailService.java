package apex.code.clipperBarberShop.shared.email;

import apex.code.clipperBarberShop.Entities.Reserva;

/**
 * Servicio para envío de correos electrónicos
 */
public interface EmailService {
    
    /**
     * Envía un email de verificación al usuario
     * @param toEmail Email del destinatario
     * @param userName Nombre del usuario
     * @param verificationToken Token de verificación
     */
    void sendVerificationEmail(String toEmail, String userName, String verificationToken);
    
    /**
     * Envía un email de bienvenida después de verificar la cuenta
     * @param toEmail Email del destinatario
     * @param userName Nombre del usuario
     */
    void sendWelcomeEmail(String toEmail, String userName);
    
    /**
     * Envía un email con contraseña temporal para recuperación de cuenta
     * @param toEmail Email del destinatario
     * @param userName Nombre del usuario
     * @param temporaryPassword Contraseña temporal generada
     */
    void sendTemporaryPasswordEmail(String toEmail, String userName, String temporaryPassword);
    
    /**
     * Envía un email de confirmación de reserva al cliente
     * @param reserva Datos de la reserva
     * @param clientName Nombre del cliente
     * @param clientEmail Email del cliente
     * @param employeeName Nombre del empleado
     * @param serviceName Nombre del servicio
     * @param empresaName Nombre de la empresa
     * @param empresaDireccion Dirección de la empresa
     */
    void sendReservaConfirmacionEmail(Reserva reserva, String clientName, String clientEmail, 
                                      String employeeName, String serviceName, 
                                      String empresaName, String empresaDireccion);
    
    /**
     * Envía un email recordatorio 15 minutos antes de la reserva
     * @param reserva Datos de la reserva
     * @param clientName Nombre del cliente
     * @param clientEmail Email del cliente
     * @param employeeName Nombre del empleado
     * @param serviceName Nombre del servicio
     * @param empresaName Nombre de la empresa
     * @param empresaDireccion Dirección de la empresa
     */
    void sendReservaRecordatorioEmail(Reserva reserva, String clientName, String clientEmail,
                                      String employeeName, String serviceName,
                                      String empresaName, String empresaDireccion);
    
    /**
     * Envía un email cuando se completa una reserva
     * @param reserva Datos de la reserva
     * @param clientName Nombre del cliente
     * @param clientEmail Email del cliente
     * @param employeeName Nombre del empleado
     * @param serviceName Nombre del servicio
     * @param empresaName Nombre de la empresa
     */
    void sendReservaCompletadaEmail(Reserva reserva, String clientName, String clientEmail,
                                    String employeeName, String serviceName, String empresaName);
    
    /**
     * Envía un email cuando se cancela una reserva
     * @param reserva Datos de la reserva
     * @param clientName Nombre del cliente
     * @param clientEmail Email del cliente
     * @param serviceName Nombre del servicio
     * @param empresaName Nombre de la empresa
     * @param motivo Motivo de la cancelación (opcional)
     */
    void sendReservaCanceladaEmail(Reserva reserva, String clientName, String clientEmail,
                                   String serviceName, String empresaName, String motivo);
    
    /**
     * Envía un email cuando se reprograma una reserva
     * @param reserva Datos de la reserva con nueva fecha
     * @param clientName Nombre del cliente
     * @param clientEmail Email del cliente
     * @param employeeName Nombre del empleado
     * @param serviceName Nombre del servicio
     * @param empresaName Nombre de la empresa
     * @param fechaAnterior Fecha anterior de la reserva
     */
    void sendReservaReprogramadaEmail(Reserva reserva, String clientName, String clientEmail,
                                      String employeeName, String serviceName, 
                                      String empresaName, String fechaAnterior);
}
