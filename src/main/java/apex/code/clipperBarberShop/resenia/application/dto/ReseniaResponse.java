package apex.code.clipperBarberShop.resenia.application.dto;

import apex.code.clipperBarberShop.Entities.Resenia;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReseniaResponse {
    private Long id;
    private Long reservaId;
    private Long empresaId;
    private String empresaNombre;
    private String clienteId;
    private String clienteNombre;
    private String empleadoId;
    private String empleadoNombre;
    private Long servicioId;
    private String servicioNombre;
    private Integer calificacionServicio;
    private Integer calificacionEmpleado;
    private String comentario;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reservationDate;
    
    /**
     * Convierte una entidad Resenia a ReseniaResponse
     */
    public static ReseniaResponse fromDomain(Resenia resenia) {
        return ReseniaResponse.builder()
                .id(resenia.getId())
                .reservaId(resenia.getReserva().getId())
                .empresaId(resenia.getEmpresa().getId())
                .empresaNombre(resenia.getEmpresa().getNombre())
                .clienteId(resenia.getClienteId())
                .clienteNombre(null) // Se puede cargar después si es necesario
                .empleadoId(resenia.getEmpleadoId())
                .empleadoNombre(null) // Se puede cargar después si es necesario
                .servicioId(resenia.getReserva().getService() != null ? resenia.getReserva().getService().getId() : null)
                .servicioNombre(resenia.getReserva().getService() != null ? resenia.getReserva().getService().getName() : null)
                .calificacionServicio(resenia.getCalificacionServicio())
                .calificacionEmpleado(resenia.getCalificacionEmpleado())
                .comentario(resenia.getComentario())
                .fecha(resenia.getFecha())
                .reservationDate(resenia.getReserva().getReservationDate())
                .build();
    }
}
