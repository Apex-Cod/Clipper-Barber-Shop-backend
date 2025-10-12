package apex.code.clipperBarberShop.register.domain.port.out;

import java.util.Optional;

import apex.code.clipperBarberShop.Entities.Usuario;

public interface UsuarioRepositoryPort {
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(String id);
    Optional<Usuario> findByEmail(String email);
}
