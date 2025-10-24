package apex.code.clipperBarberShop.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotación para validar contraseñas seguras.
 * Requisitos:
 * - Longitud entre 8 y 100 caracteres
 * - Al menos una letra mayúscula
 * - Al menos una letra minúscula
 * - Al menos un número
 * - Al menos un carácter especial (!@#$%^&*()_+-=[]{}|;:,.<>?)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
@Documented
public @interface ValidPassword {
    
    String message() default "La contraseña debe tener entre 8 y 100 caracteres, " +
            "incluir al menos una mayúscula, una minúscula, un número y un carácter especial";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    int min() default 8;
    
    int max() default 100;
}
