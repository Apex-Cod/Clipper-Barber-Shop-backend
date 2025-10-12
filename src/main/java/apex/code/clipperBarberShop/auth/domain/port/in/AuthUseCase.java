package apex.code.clipperBarberShop.auth.domain.port.in;

import apex.code.clipperBarberShop.auth.application.dto.AuthRequest;
import apex.code.clipperBarberShop.auth.application.dto.AuthResponse;

/**
 * Puerto de entrada para casos de uso de autenticación
 */
public interface AuthUseCase {
    /**
     * Autentica un usuario con email y contraseña
     * @param request Datos de autenticación
     * @return Respuesta con token JWT
     */
    AuthResponse authenticateUser(AuthRequest request);
}