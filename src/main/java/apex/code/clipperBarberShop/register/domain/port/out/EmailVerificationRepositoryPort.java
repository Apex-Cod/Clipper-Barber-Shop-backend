package apex.code.clipperBarberShop.register.domain.port.out;

import apex.code.clipperBarberShop.Entities.Usuario;

import java.util.Optional;

/**
 * Puerto para operaciones de verificación de email
 */
public interface EmailVerificationRepositoryPort {
    
    /**
     * Busca un usuario por su token de verificación
     * @param token Token de verificación
     * @return Usuario si existe
     */
    Optional<Usuario> findByVerificationToken(String token);
    
    /**
     * Guarda un usuario
     * @param usuario Usuario a guardar
     * @return Usuario guardado
     */
    Usuario save(Usuario usuario);
}
