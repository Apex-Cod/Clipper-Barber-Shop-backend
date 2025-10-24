package apex.code.clipperBarberShop.shared.util;

import java.security.SecureRandom;

/**
 * Generador de contraseñas temporales aleatorias
 */
public class PasswordGenerator {
    
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "@#$%&*";
    
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * Genera una contraseña temporal aleatoria
     * Formato: 8 caracteres con al menos 1 mayúscula, 1 minúscula, 1 número y 1 especial
     * 
     * @return Contraseña temporal generada
     */
    public static String generateTemporaryPassword() {
        return generateTemporaryPassword(10);
    }
    
    /**
     * Genera una contraseña temporal aleatoria con longitud específica
     * 
     * @param length Longitud de la contraseña (mínimo 8)
     * @return Contraseña temporal generada
     */
    public static String generateTemporaryPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("La longitud mínima de la contraseña debe ser 8");
        }
        
        StringBuilder password = new StringBuilder(length);
        
        // Garantizar al menos un carácter de cada tipo
        password.append(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        
        // Completar el resto con caracteres aleatorios
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }
        
        // Mezclar los caracteres para que no sea predecible
        return shuffleString(password.toString());
    }
    
    /**
     * Mezcla los caracteres de un string de forma aleatoria
     */
    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }
}
