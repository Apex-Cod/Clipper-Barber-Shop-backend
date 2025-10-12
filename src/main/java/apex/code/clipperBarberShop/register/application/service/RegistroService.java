package apex.code.clipperBarberShop.register.application.service;

import apex.code.clipperBarberShop.register.application.dto.ClienteRequest;
import apex.code.clipperBarberShop.register.application.dto.EmpleadoRequest;
import apex.code.clipperBarberShop.register.application.dto.RegistroRequest;

public interface RegistroService {
    void registrarEmpresaConAdmin(RegistroRequest request);
    void registrarEmpleado(EmpleadoRequest request);
    void registrarCliente(ClienteRequest request);
    // register an employee using the admin's user id (extracted from token)
    void registrarEmpleadoByAdmin(EmpleadoRequest request, String adminUserId);
}
