package apex.code.clipperBarberShop.shared.util;

/**
 * Utilidades para manipulación de cadenas de texto
 */
public class StringUtils {
    
    /**
     * Sanitiza un string eliminando espacios en blanco al inicio y final
     * @param value El valor a sanitizar
     * @return El valor sanitizado o null si el input es null
     */
    public static String sanitize(String value) {
        return value != null ? value.trim() : null;
    }
    
    /**
     * Sanitiza un email eliminando espacios y convirtiendo a minúsculas
     * @param email El email a sanitizar
     * @return El email sanitizado o null si el input es null
     */
    public static String sanitizeEmail(String email) {
        return email != null ? email.trim().toLowerCase() : null;
    }
    
    /**
     * Normaliza espacios múltiples a un solo espacio
     * @param value El valor a normalizar
     * @return El valor normalizado o null si el input es null
     */
    public static String normalizeSpaces(String value) {
        return value != null ? value.trim().replaceAll("\\s+", " ") : null;
    }
    
    /**
     * Valida que un string no esté vacío después de hacer trim
     * @param value El valor a validar
     * @return true si el valor no es null y no está vacío después de trim
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
