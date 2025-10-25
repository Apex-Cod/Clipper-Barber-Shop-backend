package apex.code.clipperBarberShop.empresa.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.Entities.enums.PublicoObjetivo;
import apex.code.clipperBarberShop.empresa.adapters.out.storage.SupabaseStorageAdapter;
import apex.code.clipperBarberShop.empresa.application.dto.*;
import apex.code.clipperBarberShop.empresa.config.SupabaseConfig;
import apex.code.clipperBarberShop.empresa.domain.port.in.EmpresaConfigUseCase;
import apex.code.clipperBarberShop.empresa.domain.port.out.StoragePort;
import apex.code.clipperBarberShop.register.domain.port.out.EmpresaRepositoryPort;
import apex.code.clipperBarberShop.shared.util.StringUtils;
import apex.code.clipperBarberShop.user.domain.port.out.UserRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio de configuración de empresa
 */
@Service
@RequiredArgsConstructor
public class EmpresaConfigService implements EmpresaConfigUseCase {
    
    private final EmpresaRepositoryPort empresaRepository;
    private final UserRepositoryPort userRepository;
    private final StoragePort storagePort;
    private final SupabaseStorageAdapter supabaseAdapter;
    private final SupabaseConfig supabaseConfig;
    
    /**
     * Obtiene la empresa del usuario OWNER autenticado
     */
    private Empresa obtenerEmpresaDelUsuario(String userId) {
        Usuario usuario = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (!"OWNER".equals(usuario.getRole())) {
            throw new IllegalArgumentException("Solo los usuarios OWNER pueden gestionar la configuración de empresa");
        }
        
        if (usuario.getEmpresa() == null) {
            throw new IllegalArgumentException("El usuario OWNER no tiene una empresa asociada");
        }
        
        return usuario.getEmpresa();
    }
    
    @Override
    public EmpresaConfigResponse obtenerConfiguracion(String userId) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        return mapToResponse(empresa);
    }
    
    @Override
    @Transactional
    public EmpresaConfigResponse actualizarInformacionBasica(String userId, ActualizarInformacionBasicaRequest request) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        
        // Sanitizar y actualizar campos
        empresa.setNombre(StringUtils.sanitize(request.getNombre()));
        empresa.setDireccion(StringUtils.sanitize(request.getDireccion()));
        empresa.setTelefono(StringUtils.sanitize(request.getTelefono()));
        empresa.setEmail(StringUtils.sanitizeEmail(request.getEmail()));
        empresa.setDescripcion(StringUtils.sanitize(request.getDescripcion()));
        empresa.setSitioWeb(StringUtils.sanitize(request.getSitioWeb()));
        empresa.setRedesSociales(request.getRedesSociales());
        
        if (request.getPublicoObjetivo() != null) {
            empresa.setPublicoObjetivo(PublicoObjetivo.valueOf(request.getPublicoObjetivo()));
        }
        
        empresa = empresaRepository.save(empresa);
        return mapToResponse(empresa);
    }
    
    @Override
    @Transactional
    public EmpresaConfigResponse actualizarHorarios(String userId, ActualizarHorariosRequest request) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        
        empresa.setHorarioLunes(request.getHorarioLunes());
        empresa.setHorarioMartes(request.getHorarioMartes());
        empresa.setHorarioMiercoles(request.getHorarioMiercoles());
        empresa.setHorarioJueves(request.getHorarioJueves());
        empresa.setHorarioViernes(request.getHorarioViernes());
        empresa.setHorarioSabado(request.getHorarioSabado());
        empresa.setHorarioDomingo(request.getHorarioDomingo());
        
        empresa = empresaRepository.save(empresa);
        return mapToResponse(empresa);
    }
    
    @Override
    @Transactional
    public EmpresaConfigResponse actualizarUbicacion(String userId, ActualizarUbicacionRequest request) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        
        empresa.setLatitud(request.getLatitud());
        empresa.setLongitud(request.getLongitud());
        
        if (request.getDireccion() != null) {
            empresa.setDireccion(StringUtils.sanitize(request.getDireccion()));
        }
        
        empresa = empresaRepository.save(empresa);
        return mapToResponse(empresa);
    }
    
    @Override
    @Transactional
    public EmpresaConfigResponse subirLogo(String userId, MultipartFile archivo) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        
        // Validar archivo
        supabaseAdapter.validateImageFile(archivo);
        
        // Eliminar logo anterior si existe
        if (empresa.getLogoPath() != null) {
            try {
                storagePort.deleteFile(supabaseConfig.getStorage().getBucket().getEmpresas(), 
                    empresa.getLogoPath());
            } catch (Exception e) {
                // Log error pero continuar
            }
        }
        
        // Generar path único
        String fileName = supabaseAdapter.generateUniqueFileName(archivo.getOriginalFilename());
        String path = String.format("empresas/%d/logo/%s", empresa.getId(), fileName);
        
        // Subir archivo
        String url = storagePort.uploadFile(
            supabaseConfig.getStorage().getBucket().getEmpresas(), path, archivo);
        
        // Actualizar empresa
        empresa.setLogoUrl(url);
        empresa.setLogoPath(path);
        
        empresa = empresaRepository.save(empresa);
        return mapToResponse(empresa);
    }
    
    @Override
    @Transactional
    public EmpresaConfigResponse subirBanner(String userId, MultipartFile archivo) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        
        // Validar archivo
        supabaseAdapter.validateImageFile(archivo);
        
        // Eliminar banner anterior si existe
        if (empresa.getBannerPath() != null) {
            try {
                storagePort.deleteFile(supabaseConfig.getStorage().getBucket().getEmpresas(), 
                    empresa.getBannerPath());
            } catch (Exception e) {
                // Log error pero continuar
            }
        }
        
        // Generar path único
        String fileName = supabaseAdapter.generateUniqueFileName(archivo.getOriginalFilename());
        String path = String.format("empresas/%d/banner/%s", empresa.getId(), fileName);
        
        // Subir archivo
        String url = storagePort.uploadFile(
            supabaseConfig.getStorage().getBucket().getEmpresas(), path, archivo);
        
        // Actualizar empresa
        empresa.setBannerUrl(url);
        empresa.setBannerPath(path);
        
        empresa = empresaRepository.save(empresa);
        return mapToResponse(empresa);
    }
    
    @Override
    @Transactional
    public void eliminarLogo(String userId) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        
        if (empresa.getLogoPath() != null) {
            storagePort.deleteFile(supabaseConfig.getStorage().getBucket().getEmpresas(), 
                empresa.getLogoPath());
            empresa.setLogoUrl(null);
            empresa.setLogoPath(null);
            empresaRepository.save(empresa);
        }
    }
    
    @Override
    @Transactional
    public void eliminarBanner(String userId) {
        Empresa empresa = obtenerEmpresaDelUsuario(userId);
        
        if (empresa.getBannerPath() != null) {
            storagePort.deleteFile(supabaseConfig.getStorage().getBucket().getEmpresas(), 
                empresa.getBannerPath());
            empresa.setBannerUrl(null);
            empresa.setBannerPath(null);
            empresaRepository.save(empresa);
        }
    }
    
    /**
     * Mapea una entidad Empresa a EmpresaConfigResponse
     */
    private EmpresaConfigResponse mapToResponse(Empresa empresa) {
        return EmpresaConfigResponse.builder()
                .id(empresa.getId())
                .nombre(empresa.getNombre())
                .direccion(empresa.getDireccion())
                .telefono(empresa.getTelefono())
                .email(empresa.getEmail())
                .estado(empresa.getEstado())
                .publicoObjetivo(empresa.getPublicoObjetivo() != null ? empresa.getPublicoObjetivo().name() : null)
                .latitud(empresa.getLatitud())
                .longitud(empresa.getLongitud())
                .horarioLunes(empresa.getHorarioLunes())
                .horarioMartes(empresa.getHorarioMartes())
                .horarioMiercoles(empresa.getHorarioMiercoles())
                .horarioJueves(empresa.getHorarioJueves())
                .horarioViernes(empresa.getHorarioViernes())
                .horarioSabado(empresa.getHorarioSabado())
                .horarioDomingo(empresa.getHorarioDomingo())
                .logoUrl(empresa.getLogoUrl())
                .logoPath(empresa.getLogoPath())
                .bannerUrl(empresa.getBannerUrl())
                .bannerPath(empresa.getBannerPath())
                .descripcion(empresa.getDescripcion())
                .sitioWeb(empresa.getSitioWeb())
                .redesSociales(empresa.getRedesSociales())
                .createdAt(empresa.getCreatedAt())
                .build();
    }
}
