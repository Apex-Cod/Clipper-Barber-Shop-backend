package apex.code.clipperBarberShop.shared;

import apex.code.clipperBarberShop.promocion.domain.exception.*;
import apex.code.clipperBarberShop.reserva.domain.exception.*;
import apex.code.clipperBarberShop.servicio.domain.exception.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArg(IllegalArgumentException ex){
        ApiResponse<Object> body = ApiResponse.error(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    
    // Excepciones de Servicio
    @ExceptionHandler(ServicioNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleServicioNotFound(ServicioNotFoundException ex){
        ApiResponse<Object> body = ApiResponse.error(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
    
    @ExceptionHandler(ServicioAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleServicioAccessDenied(ServicioAccessDeniedException ex){
        ApiResponse<Object> body = ApiResponse.error(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
    
    // Excepciones de Reserva
    @ExceptionHandler(ReservaNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleReservaNotFound(ReservaNotFoundException ex){
        ApiResponse<Object> body = ApiResponse.error(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
    
    @ExceptionHandler(ReservaAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleReservaAccessDenied(ReservaAccessDeniedException ex){
        ApiResponse<Object> body = ApiResponse.error(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
    
    @ExceptionHandler(ConflictoReservaException.class)
    public ResponseEntity<ApiResponse<Object>> handleConflictoReserva(ConflictoReservaException ex){
        ApiResponse<Object> body = ApiResponse.error(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    
    @ExceptionHandler(ReservaInvalidStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleReservaInvalidState(ReservaInvalidStateException ex){
        ApiResponse<Object> body = ApiResponse.error(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    
    // Excepciones de Promoción
    @ExceptionHandler(PromocionNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handlePromocionNotFound(PromocionNotFoundException ex){
        ApiResponse<Object> body = ApiResponse.error(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
    
    @ExceptionHandler(PromocionAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handlePromocionAccessDenied(PromocionAccessDeniedException ex){
        ApiResponse<Object> body = ApiResponse.error(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
    
    @ExceptionHandler(PromocionExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handlePromocionExpired(PromocionExpiredException ex){
        ApiResponse<Object> body = ApiResponse.error(ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    
    // Excepción de seguridad
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex){
        ApiResponse<Object> body = ApiResponse.error("Acceso denegado: " + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        String msg = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        
        ApiResponse<Object> body = ApiResponse.error("Error de validación: " + msg, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex){
        String msg = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining("; "));
        
        ApiResponse<Object> body = ApiResponse.error("Error de validación: " + msg, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAny(Exception ex){
        ApiResponse<Object> body = ApiResponse.error("Error interno del servidor: " + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
