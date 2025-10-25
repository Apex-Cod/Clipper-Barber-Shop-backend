package apex.code.clipperBarberShop.register.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.register.domain.port.out.UsuarioRepositoryPort;
import apex.code.clipperBarberShop.register.domain.port.out.EmailVerificationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort, EmailVerificationRepositoryPort {

    private final SpringDataUsuarioRepository repo;

    @Override
    public Usuario save(Usuario usuario) {
        return repo.save(usuario);
    }

    @Override
    public Optional<Usuario> findById(String id) {
        return repo.findById(id);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return repo.findByEmail(email);
    }
    
    @Override
    public Optional<Usuario> findByVerificationToken(String token) {
        return repo.findByVerificationToken(token);
    }
}
