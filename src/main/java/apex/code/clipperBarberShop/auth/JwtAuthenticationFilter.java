package apex.code.clipperBarberShop.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<String> tokenOpt = getTokenFromRequest(request);
        
        if(tokenOpt.isPresent()){
            try{
                String token = tokenOpt.get();
                log.debug("Validando token JWT para la URI: {}", request.getRequestURI());
                
                DecodedJWT decoded = tokenProvider.validateToken(token);
                String userId = decoded.getSubject();
                String role = decoded.getClaim("role").asString();
                
                log.debug("Token válido para usuario: {} con rol: {}", userId, role);
                
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                
                log.debug("Autenticación establecida en SecurityContext para usuario: {}", userId);
                
            }catch(Exception e){
                log.error("Error validando token JWT: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            log.debug("No se encontró token JWT en la request para URI: {}", request.getRequestURI());
        }
        
        filterChain.doFilter(request, response);
    }

    private Optional<String> getTokenFromRequest(HttpServletRequest request){
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(header != null && header.startsWith("Bearer ")){
            return Optional.of(header.substring(7));
        }
        return Optional.empty();
    }
}
