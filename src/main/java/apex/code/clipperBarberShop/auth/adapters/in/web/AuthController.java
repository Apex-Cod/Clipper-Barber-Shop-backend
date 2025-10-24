package apex.code.clipperBarberShop.auth.adapters.in.web;

import apex.code.clipperBarberShop.auth.application.dto.AuthRequest;
import apex.code.clipperBarberShop.auth.application.dto.AuthResponse;
import apex.code.clipperBarberShop.auth.domain.ForgotPasswordRequest;
import apex.code.clipperBarberShop.auth.domain.ForgotPasswordUseCase;
import apex.code.clipperBarberShop.auth.domain.port.in.AuthUseCase;
import apex.code.clipperBarberShop.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de autenticación
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request){
        AuthResponse resp = authUseCase.authenticateUser(request);
        return ResponseEntity.ok(ApiResponse.success("Autenticación exitosa", resp));
    }
    
    /**
     * Endpoint para recuperación de contraseña olvidada
     * Genera una contraseña temporal y la envía por email
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String message = forgotPasswordUseCase.resetPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }
}
