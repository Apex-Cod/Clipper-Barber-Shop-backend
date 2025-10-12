package apex.code.clipperBarberShop.adapters.in.web;

import apex.code.clipperBarberShop.shared.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Object>> publicEndpoint(){
        return ResponseEntity.ok(ApiResponse.success("Public endpoint OK", null));
    }

    @GetMapping("/protected")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> protectedEndpoint(){
        return ResponseEntity.ok(ApiResponse.success("Protected endpoint OK", null));
    }

    @GetMapping("/client-only")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<Object>> clientOnlyEndpoint(){
        return ResponseEntity.ok(ApiResponse.success("¡Hola! Endpoint exclusivo para clientes", "CLIENT_ACCESS"));
    }
}
