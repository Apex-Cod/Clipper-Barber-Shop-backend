package apex.code.clipperBarberShop.user.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.user.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia para usuarios
 */
@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {
    
    private final SpringDataUserRepository repository;
    
    @Override
    public Optional<Usuario> findById(String id) {
        return repository.findById(id);
    }
    
    @Override
    public Optional<Usuario> findByEmail(String email) {
        return repository.findByEmail(email);
    }
    
    @Override
    public List<Usuario> findByDeletedFalse() {
        return repository.findByDeletedFalse();
    }
    
    @Override
    public List<Usuario> findByDeletedTrue() {
        return repository.findByDeletedTrue();
    }
    
    @Override
    public List<Usuario> findByEmpresaAndDeletedFalse(Empresa empresa) {
        return repository.findByEmpresaAndDeletedFalse(empresa);
    }
    
    @Override
    public List<Usuario> findByEmpresaAndDeletedTrue(Empresa empresa) {
        return repository.findByEmpresaAndDeletedTrue(empresa);
    }
    
    @Override
    public List<Usuario> findByActivoTrueAndDeletedFalse() {
        return repository.findByActivoTrueAndDeletedFalse();
    }
    
    @Override
    public Usuario save(Usuario usuario) {
        return repository.save(usuario);
    }
    
    @Override
    public boolean existsByEmailAndDeletedFalse(String email) {
        return repository.existsByEmailAndDeletedFalse(email);
    }
    
    @Override
    public boolean existsByEmailAndIdNotAndDeletedFalse(String email, String id) {
        return repository.existsByEmailAndIdNotAndDeletedFalse(email, id);
    }
}
