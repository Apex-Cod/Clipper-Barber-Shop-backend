package apex.code.clipperBarberShop.register.adapters.in.web;

import apex.code.clipperBarberShop.register.application.dto.ClienteRequest;
import apex.code.clipperBarberShop.register.application.dto.EmpleadoRequest;
import apex.code.clipperBarberShop.register.application.dto.RegistroRequest;
import apex.code.clipperBarberShop.register.application.service.RegistroService;
import apex.code.clipperBarberShop.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/registro")
@RequiredArgsConstructor
public class RegistroController {

    private final RegistroService registroService;

    @PostMapping("/empresa")
    public ResponseEntity<ApiResponse<Object>> registrarEmpresaConAdmin(@RequestBody RegistroRequest request){
        registroService.registrarEmpresaConAdmin(request);
        return ResponseEntity.ok(ApiResponse.success("Empresa y admin registrados", null));
    }

    @PostMapping("/empleado")
    public ResponseEntity<ApiResponse<Object>> registrarEmpleado(@RequestBody EmpleadoRequest request, Principal principal){
        // principal name contains the authenticated user id (we configured UserDetails to use user id as username)
        registroService.registrarEmpleadoByAdmin(request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Empleado registrado", null));
    }

    @PostMapping("/cliente")
    public ResponseEntity<ApiResponse<Object>> registrarCliente(@RequestBody ClienteRequest request){
        registroService.registrarCliente(request);
        return ResponseEntity.ok(ApiResponse.success("Cliente registrado", null));
    }
}
