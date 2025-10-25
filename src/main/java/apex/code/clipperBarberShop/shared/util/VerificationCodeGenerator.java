package apex.code.clipperBarberShop.shared.util;

import java.security.SecureRandom;

/**
 * Generador de códigos de verificación seguros
 */
public class VerificationCodeGenerator {
    
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * Genera un código de verificación de 6 dígitos
     * @return Código de 6 dígitos como String (ej: "123456")
     */
    public static String generateCode() {
        int code = 100000 + random.nextInt(900000); // Entre 100000 y 999999
        return String.valueOf(code);
    }
    
    /**
     * Genera un código de verificación de N dígitos
     * @param length Longitud del código
     * @return Código de N dígitos como String
     */
    public static String generateCode(int length) {
        if (length < 4 || length > 8) {
            throw new IllegalArgumentException("La longitud debe estar entre 4 y 8 dígitos");
        }
        
        int min = (int) Math.pow(10, length - 1);
        int max = (int) Math.pow(10, length) - 1;
        int code = min + random.nextInt(max - min + 1);
        
        return String.valueOf(code);
    }
}
