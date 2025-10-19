package apex.code.clipperBarberShop.user.domain.port.out;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para acceso a datos de usuarios
 */
public interface UserRepositoryPort {
    
    Optional<Usuario> findById(String id);
    
    Optional<Usuario> findByEmail(String email);
    
    List<Usuario> findByDeletedFalse();
    
    List<Usuario> findByDeletedTrue();
    
    List<Usuario> findByEmpresaAndDeletedFalse(Empresa empresa);
    
    List<Usuario> findByEmpresaAndDeletedTrue(Empresa empresa);
    
    List<Usuario> findByActivoTrueAndDeletedFalse();
    
    Usuario save(Usuario usuario);
    
    boolean existsByEmailAndDeletedFalse(String email);
    
    boolean existsByEmailAndIdNotAndDeletedFalse(String email, String id);
}
