package apex.code.clipperBarberShop.auth.application.service;

import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.auth.application.dto.AuthRequest;
import apex.code.clipperBarberShop.auth.application.dto.AuthResponse;
import apex.code.clipperBarberShop.auth.domain.port.in.AuthUseCase;
import apex.code.clipperBarberShop.auth.domain.port.out.AuthUserRepositoryPort;
import apex.code.clipperBarberShop.auth.domain.port.out.PasswordEncoderPort;
import apex.code.clipperBarberShop.auth.domain.port.out.TokenProviderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implementación del caso de uso de autenticación
 */
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final AuthUserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;

    @Override
    public AuthResponse authenticateUser(AuthRequest request) {
        Usuario user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
        return AuthResponse.builder().token(token).build();
    }
}