package apex.code.clipperBarberShop.register.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.register.application.dto.EmpresaResponse;
import apex.code.clipperBarberShop.register.domain.port.out.EmpresaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmpresaClientService {

    private final EmpresaRepositoryPort empresaRepository;

    /**
     * Lista todas las empresas activas (no borradas)
     * @return Lista de empresas
     */
    @Transactional(readOnly = true)
    public List<EmpresaResponse> listarEmpresas() {
        List<Empresa> empresas = empresaRepository.findAllByDeletedFalse();
        return empresas.stream()
                .map(EmpresaResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lista todas las empresas activas (no borradas) con paginación
     * @param pageable Configuración de paginación
     * @return Página de empresas
     */
    @Transactional(readOnly = true)
    public Page<EmpresaResponse> listarEmpresasPaginadas(Pageable pageable) {
        Page<Empresa> empresas = empresaRepository.findAllByDeletedFalse(pageable);
        return empresas.map(EmpresaResponse::fromEntity);
    }

    /**
     * Obtiene una empresa por ID (solo si no está borrada)
     * @param id ID de la empresa
     * @return Empresa
     */
    @Transactional(readOnly = true)
    public EmpresaResponse obtenerEmpresaPorId(Long id) {
        Empresa empresa = empresaRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada con ID: " + id));
        return EmpresaResponse.fromEntity(empresa);
    }
}
