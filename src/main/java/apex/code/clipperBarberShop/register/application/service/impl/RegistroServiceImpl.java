package apex.code.clipperBarberShop.register.application.service.impl;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.register.application.dto.ClienteRequest;
import apex.code.clipperBarberShop.register.application.dto.EmpleadoRequest;
import apex.code.clipperBarberShop.register.application.dto.RegistroRequest;
import apex.code.clipperBarberShop.register.application.service.RegistroService;
import apex.code.clipperBarberShop.register.domain.port.out.EmpresaRepositoryPort;
import apex.code.clipperBarberShop.register.domain.port.out.UsuarioRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistroServiceImpl implements RegistroService {

    private final EmpresaRepositoryPort empresaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void registrarEmpresaConAdmin(RegistroRequest request) {
        // Sanitizar datos: eliminar espacios en blanco al inicio y final
        String empresaNombre = request.getEmpresaNombre().trim();
        String empresaEmail = request.getEmpresaEmail().trim().toLowerCase();
        String adminName = request.getAdminName().trim();
        String adminLastName = request.getAdminLastName().trim();
        String adminEmail = request.getAdminEmail().trim().toLowerCase();
        
        // Verificar que el email no esté ya registrado
        if (usuarioRepository.findByEmail(adminEmail).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        
        Empresa empresa = Empresa.builder()
                .nombre(empresaNombre)
                .email(empresaEmail)
                .build();

        empresa = empresaRepository.save(empresa);

        Usuario admin = Usuario.builder()
                .id(UUID.randomUUID().toString())
                .empresa(empresa)
                .name(adminName)
                .lastName(adminLastName)
                .email(adminEmail)
                .password(passwordEncoder.encode(request.getAdminPassword()))
                .role("OWNER")
                .build();

        usuarioRepository.save(admin);
    }

    @Override
    @Transactional
    public void registrarEmpleado(EmpleadoRequest request) {
        // Sanitizar datos
        String name = request.getName().trim();
        String lastName = request.getLastName().trim();
        String email = request.getEmail().trim().toLowerCase();
        
        // Verificar que el email no esté ya registrado
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        
        Long empresaId = null;
        try{
            empresaId = Long.parseLong(request.getEmpresaId().trim());
        }catch(Exception e){
            throw new IllegalArgumentException("empresaId inválido");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        Usuario empleado = Usuario.builder()
                .id(UUID.randomUUID().toString())
                .empresa(empresa)
                .name(name)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role("EMPLOYEE")
                .build();

        usuarioRepository.save(empleado);
    }

    @Override
    @Transactional
    public void registrarCliente(ClienteRequest request) {
        // Sanitizar datos
        String name = request.getName().trim();
        String lastName = request.getLastName().trim();
        String email = request.getEmail().trim().toLowerCase();
        
        // Verificar que el email no esté ya registrado
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        
        Usuario cliente = Usuario.builder()
                .id(UUID.randomUUID().toString())
                .empresa(null)
                .name(name)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role("CLIENT")
                .build();

        usuarioRepository.save(cliente);
    }

    @Override
    @Transactional
    public void registrarEmpleadoByAdmin(EmpleadoRequest request, String adminUserId) {
        // Sanitizar datos
        String name = request.getName().trim();
        String lastName = request.getLastName().trim();
        String email = request.getEmail().trim().toLowerCase();
        
        // Verificar que el email no esté ya registrado
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        
        // find admin user
        Usuario admin = usuarioRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin no encontrado"));

        if(!"OWNER".equals(admin.getRole())){
            throw new IllegalArgumentException("El usuario no tiene permisos para crear empleados");
        }

        Empresa empresa = admin.getEmpresa();
        if(empresa == null) throw new IllegalArgumentException("Admin no asociado a ninguna empresa");

        Usuario empleado = Usuario.builder()
                .id(UUID.randomUUID().toString())
                .empresa(empresa)
                .name(name)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role("EMPLOYEE")
                .build();

        usuarioRepository.save(empleado);
    }
}
