package apex.code.clipperBarberShop.auth.domain.port.out;

import apex.code.clipperBarberShop.Entities.Usuario;
import java.util.Optional;

/**
 * Puerto de salida para acceso a datos de usuarios en el contexto de autenticación
 */
public interface AuthUserRepositoryPort {
    /**
     * Busca un usuario por email
     * @param email Email del usuario
     * @return Usuario si existe
     */
    Optional<Usuario> findByEmail(String email);
}