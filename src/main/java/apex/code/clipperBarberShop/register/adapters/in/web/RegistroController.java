package apex.code.clipperBarberShop.register.adapters.in.web;

import apex.code.clipperBarberShop.register.application.dto.ClienteRequest;
import apex.code.clipperBarberShop.register.application.dto.EmpleadoRequest;
import apex.code.clipperBarberShop.register.application.dto.RegistroRequest;
import apex.code.clipperBarberShop.register.application.dto.ResendVerificationRequest;
import apex.code.clipperBarberShop.register.application.service.RegistroService;
import apex.code.clipperBarberShop.register.domain.port.in.VerifyEmailUseCase;
import apex.code.clipperBarberShop.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;

@RestController
@RequestMapping("/api/registro")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService registroService;
    private final VerifyEmailUseCase verifyEmailUseCase;

    @PostMapping("/empresa")
    public ResponseEntity<ApiResponse<Object>> registrarEmpresaConAdmin(@Valid @RequestBody RegistroRequest request){
        registroService.registrarEmpresaConAdmin(request);
        return ResponseEntity.ok(ApiResponse.success(
            "Empresa y admin registrados. Por favor, verifica tu email para activar tu cuenta.", null));
    }

    @PostMapping("/empleado")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Object>> registrarEmpleado(@Valid @RequestBody EmpleadoRequest request, Principal principal){
        // principal name contains the authenticated user id (we configured UserDetails to use user id as username)
        // Only users with OWNER role can register employees
        registroService.registrarEmpleadoByAdmin(request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(
            "Empleado registrado. Se ha enviado un email de verificación.", null));
    }

    @PostMapping("/cliente")
    public ResponseEntity<ApiResponse<Object>> registrarCliente(@Valid @RequestBody ClienteRequest request){
        registroService.registrarCliente(request);
        return ResponseEntity.ok(ApiResponse.success(
            "Cliente registrado. Por favor, verifica tu email para activar tu cuenta.", null));
    }
    
    /**
     * Endpoint para verificar el email mediante el token
     * Este endpoint se llama automáticamente cuando el usuario hace clic en el enlace del email
     * USO: Web (enlaces clickeables)
     */
    @GetMapping("/verify")
    public RedirectView verifyEmail(@RequestParam("token") String token) {
        try {
            verifyEmailUseCase.verifyEmail(token);
            // Redirigir al frontend con mensaje de éxito
            return new RedirectView("http://localhost:3000/verification/success");
        } catch (IllegalArgumentException e) {
            // Redirigir al frontend con mensaje de error
            return new RedirectView("http://localhost:3000/verification/error?message=" + e.getMessage());
        }
    }
    
    /**
     * Endpoint para verificar el email mediante código (App Móvil)
     * El usuario ingresa el código de 6 dígitos manualmente en la app
     * USO: Apps móviles
     */
    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<Object>> verifyEmailWithCode(
            @RequestParam("email") String email,
            @RequestParam("code") String code) {
        try {
            // Primero verificamos que el código corresponda al email
            verifyEmailUseCase.verifyEmail(code);
            return ResponseEntity.ok(ApiResponse.success("Email verificado exitosamente", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), null));
        }
    }
    
    /**
     * Endpoint para reenviar el email de verificación
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Object>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        verifyEmailUseCase.resendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
            "Email de verificación enviado. Por favor, revisa tu bandeja de entrada.", null));
    }
}
