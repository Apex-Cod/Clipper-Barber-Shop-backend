package apex.code.clipperBarberShop.servicio.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Servicio;
import apex.code.clipperBarberShop.Entities.enums.PublicoObjetivo;
import apex.code.clipperBarberShop.servicio.domain.port.out.ServicioRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para crear servicios por defecto al registrar una empresa
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServicioDefaultService {

    private final ServicioRepositoryPort servicioRepository;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.storage.bucket.servicios:Barber-Services}")
    private String servicesBucket;

    /**
     * Crea servicios por defecto según el público objetivo de la empresa
     * 
     * @param empresa La empresa recién creada
     */
    @Transactional
    public void crearServiciosPorDefecto(Empresa empresa) {
        if (empresa == null || empresa.getPublicoObjetivo() == null) {
            log.warn("No se pueden crear servicios por defecto: empresa o público objetivo nulo");
            return;
        }

        List<Servicio> serviciosDefault = new ArrayList<>();
        PublicoObjetivo publicoObjetivo = empresa.getPublicoObjetivo();

        log.info("Creando servicios por defecto para empresa {} con público objetivo: {}", 
                 empresa.getNombre(), publicoObjetivo);

        switch (publicoObjetivo) {
            case HOMBRES:
                serviciosDefault = crearServiciosHombres(empresa);
                break;
            case MUJERES:
                serviciosDefault = crearServiciosMujeres(empresa);
                break;
            case UNISEX:
                serviciosDefault = crearServiciosUnisex(empresa);
                break;
            case NIÑOS:
            case NIÑAS:
                serviciosDefault = crearServiciosNinos(empresa);
                break;
            default:
                log.warn("Público objetivo no reconocido: {}", publicoObjetivo);
                break;
        }

        // Guardar todos los servicios
        serviciosDefault.forEach(servicioRepository::save);
        
        log.info("Se crearon {} servicios por defecto para la empresa {}", 
                 serviciosDefault.size(), empresa.getNombre());
    }

    /**
     * Servicios por defecto para HOMBRES
     */
    private List<Servicio> crearServiciosHombres(Empresa empresa) {
        List<Servicio> servicios = new ArrayList<>();

        servicios.add(Servicio.builder()
                .empresa(empresa)
                .name("Corte Masculino")
                .description("Corte de cabello moderno para hombre, incluye lavado y secado")
                .price(15.00)
                .duration(30)
                .publicoObjetivo(PublicoObjetivo.HOMBRES)
                .imageUrl(buildImageUrl("default-hombres-corte.jpg"))
                .build());

        servicios.add(Servicio.builder()
                .empresa(empresa)
                .name("Barba y Bigote")
                .description("Diseño y arreglo profesional de barba completo")
                .price(12.00)
                .duration(25)
                .publicoObjetivo(PublicoObjetivo.HOMBRES)
                .imageUrl(buildImageUrl("default-hombres-barba.jpg"))
                .build());

        return servicios;
    }

    /**
     * Servicios por defecto para MUJERES
     */
    private List<Servicio> crearServiciosMujeres(Empresa empresa) {
        List<Servicio> servicios = new ArrayList<>();

        servicios.add(Servicio.builder()
                .empresa(empresa)
                .name("Corte Femenino")
                .description("Corte de cabello para dama con lavado, secado y peinado incluido")
                .price(25.00)
                .duration(45)
                .publicoObjetivo(PublicoObjetivo.MUJERES)
                .imageUrl(buildImageUrl("default-mujeres-corte.jpg"))
                .build());

        servicios.add(Servicio.builder()
                .empresa(empresa)
                .name("Peinado")
                .description("Peinado profesional para eventos o uso diario")
                .price(35.00)
                .duration(60)
                .publicoObjetivo(PublicoObjetivo.MUJERES)
                .imageUrl(buildImageUrl("default-mujeres-peinado.jpg"))
                .build());

        return servicios;
    }

    /**
     * Servicios por defecto para UNISEX
     */
    private List<Servicio> crearServiciosUnisex(Empresa empresa) {
        List<Servicio> servicios = new ArrayList<>();

        servicios.add(Servicio.builder()
                .empresa(empresa)
                .name("Corte Clásico")
                .description("Corte de cabello tradicional para cualquier persona, incluye lavado")
                .price(15.00)
                .duration(30)
                .publicoObjetivo(PublicoObjetivo.UNISEX)
                .imageUrl(buildImageUrl("default-unisex-corte.jpg"))
                .build());

        servicios.add(Servicio.builder()
                .empresa(empresa)
                .name("Corte + Barba")
                .description("Corte de cabello con arreglo de barba completo")
                .price(25.00)
                .duration(45)
                .publicoObjetivo(PublicoObjetivo.UNISEX)
                .imageUrl(buildImageUrl("default-unisex-corte-barba.jpg"))
                .build());

        return servicios;
    }

    /**
     * Servicios por defecto para NIÑOS/NIÑAS
     */
    private List<Servicio> crearServiciosNinos(Empresa empresa) {
        List<Servicio> servicios = new ArrayList<>();

        servicios.add(Servicio.builder()
                .empresa(empresa)
                .name("Corte Infantil")
                .description("Corte de cabello para niños y niñas en ambiente amigable")
                .price(10.00)
                .duration(20)
                .publicoObjetivo(empresa.getPublicoObjetivo())
                .imageUrl(buildImageUrl("default-ninos-corte.jpg"))
                .build());

        servicios.add(Servicio.builder()
                .empresa(empresa)
                .name("Primer Corte")
                .description("Primer corte especial para bebés, incluye certificado de recuerdo")
                .price(15.00)
                .duration(25)
                .publicoObjetivo(empresa.getPublicoObjetivo())
                .imageUrl(buildImageUrl("default-ninos-primer-corte.jpg"))
                .build());

        return servicios;
    }

    /**
     * Construye la URL completa de la imagen en Supabase Storage
     */
    private String buildImageUrl(String fileName) {
        return String.format("%s/storage/v1/object/public/%s/defaults/%s", 
                           supabaseUrl, servicesBucket, fileName);
    }
}
