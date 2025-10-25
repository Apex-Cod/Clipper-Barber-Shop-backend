package apex.code.clipperBarberShop.empresa.adapters.out.storage;

import apex.code.clipperBarberShop.empresa.config.SupabaseConfig;
import apex.code.clipperBarberShop.empresa.domain.port.out.StoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Adaptador para Supabase Storage
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SupabaseStorageAdapter implements StoragePort {
    
    private final RestTemplate restTemplate;
    private final SupabaseConfig supabaseConfig;
    
    @Override
    public String uploadFile(String bucket, String path, MultipartFile file) {
        try {
            String url = String.format("%s/storage/v1/object/%s/%s", 
                supabaseConfig.getUrl(), bucket, path);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseConfig.getApiKey());
            headers.setContentType(MediaType.parseMediaType(file.getContentType()));
            
            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return getPublicUrl(bucket, path);
            } else {
                throw new RuntimeException("Error al subir archivo a Supabase: " + response.getBody());
            }
            
        } catch (IOException e) {
            log.error("Error al leer el archivo", e);
            throw new RuntimeException("Error al procesar el archivo", e);
        }
    }
    
    @Override
    public void deleteFile(String bucket, String path) {
        try {
            String url = String.format("%s/storage/v1/object/%s/%s", 
                supabaseConfig.getUrl(), bucket, path);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseConfig.getApiKey());
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            
        } catch (Exception e) {
            log.error("Error al eliminar archivo de Supabase", e);
            throw new RuntimeException("Error al eliminar archivo", e);
        }
    }
    
    @Override
    public String getPublicUrl(String bucket, String path) {
        return String.format("%s/storage/v1/object/public/%s/%s", 
            supabaseConfig.getUrl(), bucket, path);
    }
    
    @Override
    public String uploadServiceImage(String fileName, MultipartFile file) {
        validateImageFile(file);
        String servicesBucket = supabaseConfig.getServicesBucket();
        String path = "servicios/" + fileName;
        return uploadFile(servicesBucket, path, file);
    }
    
    /**
     * Genera un nombre único para el archivo
     */
    public String generateUniqueFileName(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        return UUID.randomUUID().toString() + extension;
    }
    
    /**
     * Valida que el archivo sea una imagen
     */
    public void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }
        
        // Validar tamaño (ej: max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("El archivo no debe superar los 5MB");
        }
    }
}
