package apex.code.clipperBarberShop.promocion.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Promocion;
import apex.code.clipperBarberShop.Entities.Servicio;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.promocion.application.dto.ActualizarPromocionRequest;
import apex.code.clipperBarberShop.promocion.application.dto.PromocionRequest;
import apex.code.clipperBarberShop.promocion.application.dto.PromocionResponse;
import apex.code.clipperBarberShop.promocion.domain.exception.PromocionAccessDeniedException;
import apex.code.clipperBarberShop.promocion.domain.exception.PromocionNotFoundException;
import apex.code.clipperBarberShop.promocion.domain.port.out.PromocionRepositoryPort;
import apex.code.clipperBarberShop.servicio.domain.port.out.ServicioRepositoryPort;
import apex.code.clipperBarberShop.user.domain.port.out.UserRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de promociones para OWNER
 * Gestiona el CRUD completo de promociones
 */
@Service
@RequiredArgsConstructor
public class PromocionOwnerService {
    
    private final PromocionRepositoryPort promocionRepository;
    private final UserRepositoryPort userRepository;
    private final ServicioRepositoryPort servicioRepository;
    
    /**
     * Obtiene la empresa del usuario OWNER autenticado
     */
    private Empresa obtenerEmpresaDelUsuario(String userId) {
        Usuario usuario = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (!"OWNER".equals(usuario.getRole())) {
            throw new IllegalArgumentException("Solo los usuarios OWNER pueden gestionar promociones");
        }
        
        if (usuario.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario OWNER no tiene una empresa asociada");
        }
        
        return usuario.getEmpresa();
    }
    
    /**
     * Verifica que la promoción pertenezca a la empresa del usuario
     */
    private void verificarAccesoPromocion(Promocion promocion, Long empresaId) {
        if (!promocion.getEmpresa().getId().equals(empresaId)) {
            throw new PromocionAccessDeniedException();
        }
    }
    
    /**
     * Crea una nueva promoción
     */
    @Transactional
    public PromocionResponse crearPromocion(String userId, PromocionRequest request) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        
        // Validar servicio si se especifica
        Servicio servicio = null;
        if (request.getServicioId() != null) {
            servicio = servicioRepository.findByIdAndDeletedFalse(request.getServicioId())
                    .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
            
            // Verificar que el servicio pertenezca a la empresa
            if (!servicio.getEmpresa().getId().equals(empresa.getId())) {
                throw new IllegalArgumentException("El servicio no pertenece a tu empresa");
            }
        }
        
        // Crear promoción
        Promocion promocion = Promocion.builder()
                .empresa(empresa)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .tipoDescuento(request.getTipoDescuento())
                .valorDescuento(request.getValorDescuento())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .limiteUsos(request.getLimiteUsos())
                .montoMinimo(request.getMontoMinimo())
                .servicio(servicio)
                .build();
        
        promocion = promocionRepository.save(promocion);
        
        return mapToResponse(promocion);
    }
    
    /**
     * Actualiza una promoción existente
     */
    @Transactional
    public PromocionResponse actualizarPromocion(String userId, Long promocionId, ActualizarPromocionRequest request) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        
        Promocion promocion = promocionRepository.findByIdAndDeletedFalse(promocionId)
                .orElseThrow(() -> new PromocionNotFoundException(promocionId));
        
        verificarAccesoPromocion(promocion, empresa.getId());
        
        // Actualizar campos si se proporcionan
        if (request.getNombre() != null) {
            promocion.setNombre(request.getNombre());
        }
        
        if (request.getDescripcion() != null) {
            promocion.setDescripcion(request.getDescripcion());
        }
        
        if (request.getTipoDescuento() != null) {
            promocion.setTipoDescuento(request.getTipoDescuento());
        }
        
        if (request.getValorDescuento() != null) {
            promocion.setValorDescuento(request.getValorDescuento());
        }
        
        if (request.getFechaInicio() != null) {
            promocion.setFechaInicio(request.getFechaInicio());
        }
        
        if (request.getFechaFin() != null) {
            promocion.setFechaFin(request.getFechaFin());
        }
        
        if (request.getActiva() != null) {
            promocion.setActiva(request.getActiva());
        }
        
        if (request.getLimiteUsos() != null) {
            promocion.setLimiteUsos(request.getLimiteUsos());
        }
        
        if (request.getMontoMinimo() != null) {
            promocion.setMontoMinimo(request.getMontoMinimo());
        }
        
        if (request.getServicioId() != null) {
            Servicio servicio = servicioRepository.findByIdAndDeletedFalse(request.getServicioId())
                    .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
            
            if (!servicio.getEmpresa().getId().equals(empresa.getId())) {
                throw new IllegalArgumentException("El servicio no pertenece a tu empresa");
            }
            
            promocion.setServicio(servicio);
        }
        
        promocion = promocionRepository.save(promocion);
        
        return mapToResponse(promocion);
    }
    
    /**
     * Obtiene una promoción por ID
     */
    public PromocionResponse obtenerPromocion(String userId, Long promocionId) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        
        Promocion promocion = promocionRepository.findByIdAndDeletedFalse(promocionId)
                .orElseThrow(() -> new PromocionNotFoundException(promocionId));
        
        verificarAccesoPromocion(promocion, empresa.getId());
        
        return mapToResponse(promocion);
    }
    
    /**
     * Lista todas las promociones de la empresa
     */
    public List<PromocionResponse> listarPromociones(String userId) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        
        List<Promocion> promociones = promocionRepository.findByEmpresaIdAndDeletedFalse(empresa.getId());
        
        return promociones.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lista promociones con paginación
     */
    public Page<PromocionResponse> listarPromocionesPaginadas(String userId, Pageable pageable) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        
        Page<Promocion> promociones = promocionRepository.findByEmpresaIdAndDeletedFalse(empresa.getId(), pageable);
        
        return promociones.map(this::mapToResponse);
    }
    
    /**
     * Lista solo promociones activas
     */
    public List<PromocionResponse> listarPromocionesActivas(String userId) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        
        List<Promocion> promociones = promocionRepository.findByEmpresaIdAndActivaTrueAndDeletedFalse(empresa.getId());
        
        return promociones.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Activa o desactiva una promoción
     */
    @Transactional
    public PromocionResponse cambiarEstadoPromocion(String userId, Long promocionId, Boolean activa) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        
        Promocion promocion = promocionRepository.findByIdAndDeletedFalse(promocionId)
                .orElseThrow(() -> new PromocionNotFoundException(promocionId));
        
        verificarAccesoPromocion(promocion, empresa.getId());
        
        promocion.setActiva(activa);
        promocion = promocionRepository.save(promocion);
        
        return mapToResponse(promocion);
    }
    
    /**
     * Elimina (soft delete) una promoción
     */
    @Transactional
    public void eliminarPromocion(String userId, Long promocionId) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        
        Promocion promocion = promocionRepository.findByIdAndDeletedFalse(promocionId)
                .orElseThrow(() -> new PromocionNotFoundException(promocionId));
        
        verificarAccesoPromocion(promocion, empresa.getId());
        
        promocion.setDeleted(true);
        promocion.setDeletedAt(LocalDateTime.now());
        promocion.setDeletedBy(userId);
        
        promocionRepository.save(promocion);
    }
    
    /**
     * Mapea entidad a DTO de respuesta
     */
    private PromocionResponse mapToResponse(Promocion promocion) {
        return PromocionResponse.builder()
                .id(promocion.getId())
                .empresaId(promocion.getEmpresa().getId())
                .empresaNombre(promocion.getEmpresa().getNombre())
                .nombre(promocion.getNombre())
                .descripcion(promocion.getDescripcion())
                .tipoDescuento(promocion.getTipoDescuento())
                .valorDescuento(promocion.getValorDescuento())
                .fechaInicio(promocion.getFechaInicio())
                .fechaFin(promocion.getFechaFin())
                .activa(promocion.getActiva())
                .limiteUsos(promocion.getLimiteUsos())
                .usosActuales(promocion.getUsosActuales())
                .montoMinimo(promocion.getMontoMinimo())
                .servicioId(promocion.getServicio() != null ? promocion.getServicio().getId() : null)
                .servicioNombre(promocion.getServicio() != null ? promocion.getServicio().getName() : null)
                .vigente(promocion.isVigente())
                .build();
    }
}
