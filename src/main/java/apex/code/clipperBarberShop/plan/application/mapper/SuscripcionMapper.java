package apex.code.clipperBarberShop.plan.application.mapper;

import apex.code.clipperBarberShop.Entities.Suscripcion;
import apex.code.clipperBarberShop.plan.application.dto.SuscripcionResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Suscripcion y DTOs
 */
@Component
public class SuscripcionMapper {
    
    public SuscripcionResponse toResponse(Suscripcion suscripcion) {
        return SuscripcionResponse.builder()
            .id(suscripcion.getId())
            .empresaId(suscripcion.getEmpresa().getId())
            .empresaNombre(suscripcion.getEmpresa().getNombre())
            .planId(suscripcion.getPlan().getId())
            .planNombre(suscripcion.getPlan().getNombre())
            .tipoPlan(suscripcion.getPlan().getTipo().name())
            .fechaInicio(suscripcion.getFechaInicio())
            .fechaFin(suscripcion.getFechaFin())
            .estado(suscripcion.getEstado())
            .montoPagado(suscripcion.getMontoPagado())
            .fechaCreacion(suscripcion.getFechaCreacion())
            .fechaActivacion(suscripcion.getFechaActivacion())
            .autoRenovar(suscripcion.getAutoRenovar())
            .diasRestantes(suscripcion.getDiasRestantes())
            .estaActiva(suscripcion.estaActiva())
            .notas(suscripcion.getNotas())
            .build();
    }
}
