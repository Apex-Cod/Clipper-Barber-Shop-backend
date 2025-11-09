package apex.code.clipperBarberShop.plan.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Plan;
import apex.code.clipperBarberShop.Entities.Suscripcion;
import apex.code.clipperBarberShop.plan.domain.exception.PlanLimitExceededException;
import apex.code.clipperBarberShop.plan.domain.exception.PlanNotFoundException;
import apex.code.clipperBarberShop.plan.domain.exception.SuscripcionException;
import apex.code.clipperBarberShop.plan.domain.port.out.PlanRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para validar los límites del plan de una empresa.
 * Este servicio se debe invocar antes de crear usuarios, reservas, servicios o promociones.
 * IMPORTANTE: Verifica primero que la empresa tenga una suscripción activa y pagada.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PlanLimitService {

    private final PlanRepositoryPort planRepository;
    private final SuscripcionService suscripcionService;

    /**
     * Valida si la empresa puede agregar un nuevo usuario según su plan.
     * PRIMERO verifica que tenga una suscripción activa y pagada.
     * @param empresa La empresa que intenta agregar un usuario
     * @param cantidadActualUsuarios Cantidad actual de usuarios de la empresa
     * @throws SuscripcionException si no tiene suscripción activa
     * @throws PlanLimitExceededException si se excede el límite
     */
    public void validarLimiteUsuarios(Empresa empresa, int cantidadActualUsuarios) {
        // PRIMERO: Verificar que la empresa tiene una suscripción activa
        verificarSuscripcionActiva(empresa);
        
        Plan plan = obtenerPlanDeEmpresa(empresa);
        
        if (plan.tieneUsuariosIlimitados()) {
            log.debug("Plan {} permite usuarios ilimitados", plan.getNombre());
            return;
        }

        Integer limite = plan.getLimiteUsuarios();
        if (cantidadActualUsuarios >= limite) {
            log.warn("Límite de usuarios alcanzado para empresa {}: {}/{}", 
                     empresa.getId(), cantidadActualUsuarios, limite);
            throw new PlanLimitExceededException(
                "usuarios", 
                cantidadActualUsuarios, 
                limite
            );
        }

        log.debug("Validación de usuarios OK: {}/{}", cantidadActualUsuarios, limite);
    }

    /**
     * Valida si la empresa puede crear una nueva reserva según su plan.
     * PRIMERO verifica que tenga una suscripción activa y pagada.
     * @param empresa La empresa que intenta crear una reserva
     * @param cantidadReservasEsteMes Cantidad de reservas creadas en el mes actual
     * @throws SuscripcionException si no tiene suscripción activa
     * @throws PlanLimitExceededException si se excede el límite
     */
    public void validarLimiteReservas(Empresa empresa, int cantidadReservasEsteMes) {
        // PRIMERO: Verificar que la empresa tiene una suscripción activa
        verificarSuscripcionActiva(empresa);
        
        Plan plan = obtenerPlanDeEmpresa(empresa);
        
        if (plan.tieneReservasIlimitadas()) {
            log.debug("Plan {} permite reservas ilimitadas", plan.getNombre());
            return;
        }

        Integer limite = plan.getLimiteReservasMes();
        if (cantidadReservasEsteMes >= limite) {
            log.warn("Límite de reservas alcanzado para empresa {}: {}/{}", 
                     empresa.getId(), cantidadReservasEsteMes, limite);
            throw new PlanLimitExceededException(
                "reservas mensuales", 
                cantidadReservasEsteMes, 
                limite
            );
        }

        log.debug("Validación de reservas OK: {}/{}", cantidadReservasEsteMes, limite);
    }

    /**
     * Valida si la empresa puede crear un nuevo servicio según su plan.
     * PRIMERO verifica que tenga una suscripción activa y pagada.
     * @param empresa La empresa que intenta crear un servicio
     * @param cantidadActualServicios Cantidad actual de servicios de la empresa
     * @throws SuscripcionException si no tiene suscripción activa
     * @throws PlanLimitExceededException si se excede el límite
     */
    public void validarLimiteServicios(Empresa empresa, int cantidadActualServicios) {
        // PRIMERO: Verificar que la empresa tiene una suscripción activa
        verificarSuscripcionActiva(empresa);
        
        Plan plan = obtenerPlanDeEmpresa(empresa);
        
        if (plan.tieneServiciosIlimitados()) {
            log.debug("Plan {} permite servicios ilimitados", plan.getNombre());
            return;
        }

        Integer limite = plan.getLimiteServicios();
        if (cantidadActualServicios >= limite) {
            log.warn("Límite de servicios alcanzado para empresa {}: {}/{}", 
                     empresa.getId(), cantidadActualServicios, limite);
            throw new PlanLimitExceededException(
                "servicios", 
                cantidadActualServicios, 
                limite
            );
        }

        log.debug("Validación de servicios OK: {}/{}", cantidadActualServicios, limite);
    }

    /**
     * Valida si la empresa puede crear una nueva promoción según su plan.
     * PRIMERO verifica que tenga una suscripción activa y pagada.
     * @param empresa La empresa que intenta crear una promoción
     * @param cantidadActualPromociones Cantidad actual de promociones activas de la empresa
     * @throws SuscripcionException si no tiene suscripción activa
     * @throws PlanLimitExceededException si se excede el límite
     */
    public void validarLimitePromociones(Empresa empresa, int cantidadActualPromociones) {
        // PRIMERO: Verificar que la empresa tiene una suscripción activa
        verificarSuscripcionActiva(empresa);
        
        Plan plan = obtenerPlanDeEmpresa(empresa);
        
        if (plan.tienePromocionesIlimitadas()) {
            log.debug("Plan {} permite promociones ilimitadas", plan.getNombre());
            return;
        }

        Integer limite = plan.getLimitePromociones();
        if (cantidadActualPromociones >= limite) {
            log.warn("Límite de promociones alcanzado para empresa {}: {}/{}", 
                     empresa.getId(), cantidadActualPromociones, limite);
            throw new PlanLimitExceededException(
                "promociones", 
                cantidadActualPromociones, 
                limite
            );
        }

        log.debug("Validación de promociones OK: {}/{}", cantidadActualPromociones, limite);
    }
    
    /**
     * Verifica que la empresa tenga una suscripción activa y pagada.
     * Lanza excepción si no tiene suscripción o si expiró.
     */
    private void verificarSuscripcionActiva(Empresa empresa) {
        // Obtiene la suscripción activa (lanza excepción si no existe o expiró)
        suscripcionService.obtenerSuscripcionActiva(empresa.getId());
    }

    /**
     * Obtiene información sobre los límites actuales de una empresa
     */
    public LimitesInfo obtenerLimitesInfo(Empresa empresa) {
        Plan plan = obtenerPlanDeEmpresa(empresa);
        
        return LimitesInfo.builder()
                .nombrePlan(plan.getNombre())
                .tipoPlan(plan.getTipo())
                .limiteUsuarios(plan.getLimiteUsuarios())
                .limiteReservasMes(plan.getLimiteReservasMes())
                .limiteServicios(plan.getLimiteServicios())
                .limitePromociones(plan.getLimitePromociones())
                .usuariosIlimitados(plan.tieneUsuariosIlimitados())
                .reservasIlimitadas(plan.tieneReservasIlimitadas())
                .serviciosIlimitados(plan.tieneServiciosIlimitados())
                .promocionesIlimitadas(plan.tienePromocionesIlimitadas())
                .build();
    }

    /**
     * Método auxiliar para obtener el plan de una empresa
     */
    private Plan obtenerPlanDeEmpresa(Empresa empresa) {
        if (empresa.getPlan() == null) {
            throw new PlanNotFoundException("La empresa no tiene un plan asignado");
        }

        return planRepository.findByIdAndDeletedFalse(empresa.getPlan().getId())
                .orElseThrow(() -> new PlanNotFoundException(
                    "Plan no encontrado para empresa ID: " + empresa.getId()
                ));
    }

    /**
     * DTO con información de los límites del plan
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LimitesInfo {
        private String nombrePlan;
        private apex.code.clipperBarberShop.Entities.enums.TipoPlan tipoPlan;
        private Integer limiteUsuarios;
        private Integer limiteReservasMes;
        private Integer limiteServicios;
        private Integer limitePromociones;
        private boolean usuariosIlimitados;
        private boolean reservasIlimitadas;
        private boolean serviciosIlimitados;
        private boolean promocionesIlimitadas;
    }
}
