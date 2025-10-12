package apex.code.clipperBarberShop.register.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import apex.code.clipperBarberShop.Entities.Usuario;

import java.util.Optional;

public interface SpringDataUsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);
}
