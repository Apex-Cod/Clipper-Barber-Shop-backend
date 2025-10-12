package apex.code.clipperBarberShop.auth.adapters.out;

import apex.code.clipperBarberShop.auth.JwtTokenProvider;
import apex.code.clipperBarberShop.auth.domain.port.out.TokenProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptador para el proveedor de tokens JWT
 */
@Component
@RequiredArgsConstructor
public class TokenProviderAdapter implements TokenProviderPort {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public String generateToken(String userId, String email, String role) {
        return jwtTokenProvider.generateToken(userId, email, role);
    }
}