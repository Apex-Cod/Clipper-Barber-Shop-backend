package apex.code.clipperBarberShop.empresa.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración para Supabase
 */
@Configuration
@ConfigurationProperties(prefix = "supabase")
@Getter
@Setter
public class SupabaseConfig {
    
    private String url;
    private String apiKey;
    private Storage storage = new Storage();
    
    @Getter
    @Setter
    public static class Storage {
        private Bucket bucket = new Bucket();
        
        @Getter
        @Setter
        public static class Bucket {
            private String empresas = "clipper-images";
            private String servicios = "Barber-Services";
        }
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
