package apex.code.clipperBarberShop.empresa.domain.port.out;

import org.springframework.web.multipart.MultipartFile;

/**
 * Puerto de salida para operaciones de almacenamiento de archivos en Supabase
 */
public interface StoragePort {
    
    /**
     * Sube un archivo a Supabase Storage
     * 
     * @param bucket Nombre del bucket en Supabase
     * @param path Ruta donde se guardará el archivo (ej: "empresas/123/logo.jpg")
     * @param file Archivo a subir
     * @return URL pública del archivo subido
     */
    String uploadFile(String bucket, String path, MultipartFile file);
    
    /**
     * Elimina un archivo de Supabase Storage
     * 
     * @param bucket Nombre del bucket en Supabase
     * @param path Ruta del archivo a eliminar
     */
    void deleteFile(String bucket, String path);
    
    /**
     * Obtiene la URL pública de un archivo
     * 
     * @param bucket Nombre del bucket en Supabase
     * @param path Ruta del archivo
     * @return URL pública del archivo
     */
    String getPublicUrl(String bucket, String path);
}
