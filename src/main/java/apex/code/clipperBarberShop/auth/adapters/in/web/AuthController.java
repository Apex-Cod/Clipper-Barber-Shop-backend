package apex.code.clipperBarberShop.auth.adapters.in.web;

import apex.code.clipperBarberShop.auth.application.dto.AuthRequest;
import apex.code.clipperBarberShop.auth.application.dto.AuthResponse;
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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request){
        AuthResponse resp = authUseCase.authenticateUser(request);
        return ResponseEntity.ok(ApiResponse.success("Autenticación exitosa", resp));
    }
}
