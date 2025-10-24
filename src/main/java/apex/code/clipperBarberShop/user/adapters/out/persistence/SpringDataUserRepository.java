package apex.code.clipperBarberShop.user.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para Usuario
 */
@Repository
public interface SpringDataUserRepository extends JpaRepository<Usuario, String> {
    
    Optional<Usuario> findByEmail(String email);
    
    List<Usuario> findByDeletedFalse();
    
    List<Usuario> findByDeletedTrue();
    
    List<Usuario> findByEmpresaAndDeletedFalse(Empresa empresa);
    
    List<Usuario> findByEmpresaAndDeletedTrue(Empresa empresa);
    
    List<Usuario> findByActivoTrueAndDeletedFalse();
    
    boolean existsByEmailAndDeletedFalse(String email);
    
    boolean existsByEmailAndIdNotAndDeletedFalse(String email, String id);
}
