package apex.code.clipperBarberShop.payment.application.service;

import apex.code.clipperBarberShop.Entities.Pago;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.Entities.enums.EstadoPago;
import apex.code.clipperBarberShop.payment.application.dto.PagoResponse;
import apex.code.clipperBarberShop.payment.domain.exception.PagoAccessDeniedException;
import apex.code.clipperBarberShop.payment.domain.exception.PagoNotFoundException;
import apex.code.clipperBarberShop.payment.domain.port.out.PagoRepositoryPort;
import apex.code.clipperBarberShop.register.domain.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para gestión de pagos por parte del OWNER
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PagoOwnerService {
    
    private final PagoRepositoryPort pagoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    
    /**
     * Obtiene los detalles de un pago
     */
    public PagoResponse obtenerPago(Long pagoId, String ownerId) {
        Pago pago = pagoRepository.findByIdAndDeletedFalse(pagoId)
                .orElseThrow(() -> new PagoNotFoundException(pagoId));
        
        // Validar que el pago pertenece a la empresa del owner
        validarAccesoEmpresa(pago.getEmpresa().getId(), ownerId);
        
        return PagoResponse.fromDomain(pago);
    }
    
    /**
     * Lista todos los pagos de la empresa del owner
     */
    public List<PagoResponse> listarPagosPorEmpresa(String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no tiene una empresa asociada");
        }
        
        Long empresaId = owner.getEmpresa().getId();
        
        return pagoRepository.findByEmpresaIdAndDeletedFalse(empresaId).stream()
                .map(PagoResponse::fromDomain)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista pagos paginados de la empresa
     */
    public Page<PagoResponse> listarPagosPaginados(String ownerId, Pageable pageable) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no tiene una empresa asociada");
        }
        
        Long empresaId = owner.getEmpresa().getId();
        
        return pagoRepository.findByEmpresaIdAndDeletedFalse(empresaId, pageable)
                .map(PagoResponse::fromDomain);
    }
    
    /**
     * Lista pagos por estado
     */
    public List<PagoResponse> listarPagosPorEstado(String ownerId, EstadoPago estado) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no tiene una empresa asociada");
        }
        
        Long empresaId = owner.getEmpresa().getId();
        
        return pagoRepository.findByEmpresaIdAndDeletedFalse(empresaId).stream()
                .filter(pago -> pago.getEstado() == estado)
                .map(PagoResponse::fromDomain)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene estadísticas de pagos de la empresa
     */
    public Map<String, Object> obtenerEstadisticasPagos(String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner no encontrado"));
        
        if (owner.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario no tiene una empresa asociada");
        }
        
        Long empresaId = owner.getEmpresa().getId();
        
        // Obtener todos los pagos de la empresa
        List<Pago> pagos = pagoRepository.findByEmpresaIdAndDeletedFalse(empresaId);
        
        // Calcular estadísticas
        long totalPagos = pagos.size();
        long pagosCompletados = pagos.stream().filter(p -> p.getEstado() == EstadoPago.COMPLETADO).count();
        long pagosPendientes = pagos.stream().filter(p -> p.getEstado() == EstadoPago.PENDIENTE).count();
        long pagosFallidos = pagos.stream().filter(p -> p.getEstado() == EstadoPago.FALLIDO).count();
        long pagosCancelados = pagos.stream().filter(p -> p.getEstado() == EstadoPago.CANCELADO).count();
        
        double totalIngresos = pagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.COMPLETADO)
                .mapToDouble(p -> p.getMonto().doubleValue())
                .sum();
        
        double promedioMonto = pagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.COMPLETADO)
                .mapToDouble(p -> p.getMonto().doubleValue())
                .average()
                .orElse(0.0);
        
        return Map.of(
                "totalPagos", totalPagos,
                "pagosCompletados", pagosCompletados,
                "pagosPendientes", pagosPendientes,
                "pagosFallidos", pagosFallidos,
                "pagosCancelados", pagosCancelados,
                "totalIngresos", Math.round(totalIngresos * 100.0) / 100.0,
                "promedioMonto", Math.round(promedioMonto * 100.0) / 100.0
        );
    }
    
    /**
     * Elimina un pago (soft delete) - Solo pagos pendientes o fallidos
     */
    @Transactional
    public void eliminarPago(Long pagoId, String ownerId) {
        Pago pago = pagoRepository.findByIdAndDeletedFalse(pagoId)
                .orElseThrow(() -> new PagoNotFoundException(pagoId));
        
        // Validar que el pago pertenece a la empresa del owner
        validarAccesoEmpresa(pago.getEmpresa().getId(), ownerId);
        
        // Solo se pueden eliminar pagos pendientes, fallidos o cancelados
        if (pago.getEstado() == EstadoPago.COMPLETADO || pago.getEstado() == EstadoPago.PROCESANDO) {
            throw new IllegalArgumentException("No se puede eliminar un pago completado o en proceso");
        }
        
        pago.softDelete(ownerId);
        pagoRepository.save(pago);
        
        log.info("Pago {} eliminado por owner {}", pagoId, ownerId);
    }
    
    /**
     * Valida que el owner tiene acceso a la empresa
     */
    private void validarAccesoEmpresa(Long empresaId, String ownerId) {
        Usuario owner = usuarioRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner no encontrado"));
        
        if (owner.getEmpresa() == null || !owner.getEmpresa().getId().equals(empresaId)) {
            throw new PagoAccessDeniedException("No tienes permiso para acceder a los pagos de esta empresa");
        }
    }
}
