package apex.code.clipperBarberShop.security;

import apex.code.clipperBarberShop.shared.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String message = "No autenticado";
        String uri = request.getRequestURI();
        
        // Mensaje más específico basado en el tipo de error
        if (authException.getMessage().contains("JWT")) {
            message = "Token JWT inválido o expirado";
        } else if (uri.contains("/api/")) {
            message = "Se requiere autenticación para acceder a este recurso";
        }
        
        ApiResponse<Object> body = ApiResponse.error(message, null);
        response.getWriter().write(mapper.writeValueAsString(body));
    }
}
