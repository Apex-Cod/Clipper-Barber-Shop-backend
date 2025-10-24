package apex.code.clipperBarberShop.empresa.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.enums.PublicoObjetivo;
import apex.code.clipperBarberShop.empresa.adapters.out.storage.SupabaseStorageAdapter;
import apex.code.clipperBarberShop.empresa.application.dto.*;
import apex.code.clipperBarberShop.empresa.domain.port.in.EmpresaConfigUseCase;
import apex.code.clipperBarberShop.empresa.domain.port.out.StoragePort;
import apex.code.clipperBarberShop.register.domain.port.out.EmpresaRepositoryPort;
import apex.code.clipperBarberShop.shared.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio de configuración de empresa
 */
@Service
@RequiredArgsConstructor
public class EmpresaConfigService implements EmpresaConfigUseCase {
    
    private final EmpresaRepositoryPort empresaRepository;
    private final StoragePort storagePort;
    private final SupabaseStorageAdapter supabaseAdapter;
    
    @Value("${supabase.storage.bucket}")
    private String bucket;
    
    @Override
    public EmpresaConfigResponse obtenerConfiguracion(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
        return mapToResponse(empresa);
    }
    
    @Override
    @Transactional
    public EmpresaConfigResponse actualizarInformacionBasica(Long empresaId, ActualizarInformacionBasicaRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
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
    public EmpresaConfigResponse actualizarHorarios(Long empresaId, ActualizarHorariosRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
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
    public EmpresaConfigResponse actualizarUbicacion(Long empresaId, ActualizarUbicacionRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
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
    public EmpresaConfigResponse subirLogo(Long empresaId, MultipartFile archivo) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
        // Validar archivo
        supabaseAdapter.validateImageFile(archivo);
        
        // Eliminar logo anterior si existe
        if (empresa.getLogoPath() != null) {
            try {
                storagePort.deleteFile(bucket, empresa.getLogoPath());
            } catch (Exception e) {
                // Log error pero continuar
            }
        }
        
        // Generar path único
        String fileName = supabaseAdapter.generateUniqueFileName(archivo.getOriginalFilename());
        String path = String.format("empresas/%d/logo/%s", empresaId, fileName);
        
        // Subir archivo
        String url = storagePort.uploadFile(bucket, path, archivo);
        
        // Actualizar empresa
        empresa.setLogoUrl(url);
        empresa.setLogoPath(path);
        
        empresa = empresaRepository.save(empresa);
        return mapToResponse(empresa);
    }
    
    @Override
    @Transactional
    public EmpresaConfigResponse subirBanner(Long empresaId, MultipartFile archivo) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
        // Validar archivo
        supabaseAdapter.validateImageFile(archivo);
        
        // Eliminar banner anterior si existe
        if (empresa.getBannerPath() != null) {
            try {
                storagePort.deleteFile(bucket, empresa.getBannerPath());
            } catch (Exception e) {
                // Log error pero continuar
            }
        }
        
        // Generar path único
        String fileName = supabaseAdapter.generateUniqueFileName(archivo.getOriginalFilename());
        String path = String.format("empresas/%d/banner/%s", empresaId, fileName);
        
        // Subir archivo
        String url = storagePort.uploadFile(bucket, path, archivo);
        
        // Actualizar empresa
        empresa.setBannerUrl(url);
        empresa.setBannerPath(path);
        
        empresa = empresaRepository.save(empresa);
        return mapToResponse(empresa);
    }
    
    @Override
    @Transactional
    public void eliminarLogo(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
        if (empresa.getLogoPath() != null) {
            storagePort.deleteFile(bucket, empresa.getLogoPath());
            empresa.setLogoUrl(null);
            empresa.setLogoPath(null);
            empresaRepository.save(empresa);
        }
    }
    
    @Override
    @Transactional
    public void eliminarBanner(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
        if (empresa.getBannerPath() != null) {
            storagePort.deleteFile(bucket, empresa.getBannerPath());
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
