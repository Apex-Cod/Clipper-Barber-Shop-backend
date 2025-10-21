package apex.code.clipperBarberShop.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validador de contraseñas seguras
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");
    
    private int min;
    private int max;
    
    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        
        // Validar longitud
        if (password.length() < min || password.length() > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("La contraseña debe tener entre %d y %d caracteres", min, max)
            ).addConstraintViolation();
            return false;
        }
        
        // Validar mayúsculas
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "La contraseña debe contener al menos una letra mayúscula"
            ).addConstraintViolation();
            return false;
        }
        
        // Validar minúsculas
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "La contraseña debe contener al menos una letra minúscula"
            ).addConstraintViolation();
            return false;
        }
        
        // Validar números
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "La contraseña debe contener al menos un número"
            ).addConstraintViolation();
            return false;
        }
        
        // Validar caracteres especiales
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "La contraseña debe contener al menos un carácter especial (!@#$%^&*()_+-=[]{}|;:,.<>?)"
            ).addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
