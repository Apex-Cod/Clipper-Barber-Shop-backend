package apex.code.clipperBarberShop.auth.adapters.out;

import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.auth.domain.port.out.AuthUserRepositoryPort;
import apex.code.clipperBarberShop.registro.domain.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Adaptador para operaciones de repositorio de usuarios en el contexto de autenticación
 */
@Component
@RequiredArgsConstructor
public class AuthUserRepositoryAdapter implements AuthUserRepositoryPort {

    private final UsuarioRepositoryPort usuarioRepository;

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}