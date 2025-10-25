package apex.code.clipperBarberShop.reserva.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Promocion;
import apex.code.clipperBarberShop.Entities.Reserva;
import apex.code.clipperBarberShop.Entities.Servicio;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.promocion.domain.exception.PromocionExpiredException;
import apex.code.clipperBarberShop.promocion.domain.exception.PromocionNotFoundException;
import apex.code.clipperBarberShop.promocion.domain.port.out.PromocionRepositoryPort;
import apex.code.clipperBarberShop.register.domain.port.out.EmpresaRepositoryPort;
import apex.code.clipperBarberShop.register.domain.port.out.UsuarioRepositoryPort;
import apex.code.clipperBarberShop.reserva.domain.exception.ConflictoReservaException;
import apex.code.clipperBarberShop.reserva.domain.port.out.ReservaRepositoryPort;
import apex.code.clipperBarberShop.servicio.domain.port.out.ServicioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Servicio de validación de reservas
 * Valida conflictos, horarios disponibles y restricciones de negocio
 */
@Service
@RequiredArgsConstructor
public class ReservaValidationService {
    
    private final ReservaRepositoryPort reservaRepository;
    private final ServicioRepositoryPort servicioRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final EmpresaRepositoryPort empresaRepository;
    private final PromocionRepositoryPort promocionRepository;
    
    /**
     * Valida que no existan conflictos para una nueva reserva
     */
    public void validarDisponibilidad(Long servicioId, String employeeId, LocalDateTime fechaInicio) {
        // Obtener el servicio para conocer la duración
        Servicio servicio = servicioRepository.findByIdAndDeletedFalse(servicioId)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        
        LocalDateTime fechaFin = fechaInicio.plusMinutes(servicio.getDuration());
        
        // Buscar reservas del empleado en ese rango de tiempo (excluyendo canceladas)
        List<Reserva> reservasConflictivas = reservaRepository
                .findByEmployeeIdAndReservationDateBetweenAndStatusNotAndDeletedFalse(
                        employeeId, 
                        fechaInicio.minusMinutes(1), 
                        fechaFin.plusMinutes(1), 
                        "CANCELLED");
        
        if (!reservasConflictivas.isEmpty()) {
            throw new ConflictoReservaException(
                    "El empleado ya tiene una reserva en ese horario. Por favor, elija otro horario.");
        }
    }
    
    /**
     * Valida que no existan conflictos para reprogramar una reserva
     * Excluye la reserva actual de la validación
     */
    public void validarDisponibilidadParaReprogramacion(
            Long reservaActualId, 
            Long servicioId, 
            String employeeId, 
            LocalDateTime fechaInicio) {
        
        // Obtener el servicio para conocer la duración
        Servicio servicio = servicioRepository.findByIdAndDeletedFalse(servicioId)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        
        LocalDateTime fechaFin = fechaInicio.plusMinutes(servicio.getDuration());
        
        // Buscar reservas del empleado en ese rango de tiempo (excluyendo canceladas y la actual)
        List<Reserva> reservasConflictivas = reservaRepository
                .findByEmployeeIdAndReservationDateBetweenAndStatusNotAndDeletedFalse(
                        employeeId, 
                        fechaInicio.minusMinutes(1), 
                        fechaFin.plusMinutes(1), 
                        "CANCELLED");
        
        // Filtrar la reserva actual
        reservasConflictivas = reservasConflictivas.stream()
                .filter(r -> !r.getId().equals(reservaActualId))
                .toList();
        
        if (!reservasConflictivas.isEmpty()) {
            throw new ConflictoReservaException(
                    "El empleado ya tiene una reserva en ese horario. Por favor, elija otro horario.");
        }
    }
    
    /**
     * Valida que la fecha de reserva esté en el futuro
     */
    public void validarFechaFutura(LocalDateTime fecha) {
        if (fecha.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de la reserva debe ser en el futuro");
        }
    }
    
    /**
     * Valida que el empleado pertenezca a la empresa
     */
    public void validarEmpleadoEmpresa(String employeeId, Long empresaId) {
        Usuario empleado = usuarioRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
        
        if (empleado.getEmpresa() == null || 
            !empleado.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El empleado no pertenece a esta empresa");
        }
        
        if (!"EMPLOYEE".equals(empleado.getRole()) && !"OWNER".equals(empleado.getRole())) {
            throw new IllegalArgumentException("El usuario seleccionado no es un empleado");
        }
        
        if (!empleado.getActivo() || empleado.getDeleted()) {
            throw new IllegalArgumentException("El empleado no está activo");
        }
    }
    
    /**
     * Valida horario comercial
     * Usa los horarios configurados de la empresa, si no están configurados usa horarios por defecto (8:00 - 20:00)
     */
    public void validarHorarioComercial(LocalDateTime fecha, Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
        DayOfWeek diaSemana = fecha.getDayOfWeek();
        String horarioEmpresa = obtenerHorarioPorDia(empresa, diaSemana);
        
        LocalTime hora = fecha.toLocalTime();
        
        // Si no hay horario configurado para ese día, usar horarios por defecto
        if (horarioEmpresa == null || horarioEmpresa.trim().isEmpty()) {
            validarHorarioPorDefecto(hora);
            return;
        }
        
        // Si el día está marcado como CERRADO
        if ("CERRADO".equalsIgnoreCase(horarioEmpresa.trim())) {
            throw new IllegalArgumentException("La empresa está cerrada el " + traducirDiaSemana(diaSemana));
        }
        
        // Validar horario configurado
        validarHorarioConfigurando(hora, horarioEmpresa, diaSemana);
    }
    
    /**
     * Obtiene el horario configurado para un día específico
     */
    private String obtenerHorarioPorDia(Empresa empresa, DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> empresa.getHorarioLunes();
            case TUESDAY -> empresa.getHorarioMartes();
            case WEDNESDAY -> empresa.getHorarioMiercoles();
            case THURSDAY -> empresa.getHorarioJueves();
            case FRIDAY -> empresa.getHorarioViernes();
            case SATURDAY -> empresa.getHorarioSabado();
            case SUNDAY -> empresa.getHorarioDomingo();
        };
    }
    
    /**
     * Valida usando horario por defecto (8:00 - 20:00)
     */
    private void validarHorarioPorDefecto(LocalTime hora) {
        LocalTime apertura = LocalTime.of(8, 0);
        LocalTime cierre = LocalTime.of(20, 0);
        
        if (hora.isBefore(apertura) || hora.isAfter(cierre)) {
            throw new IllegalArgumentException(
                    "La reserva debe estar dentro del horario comercial (8:00 - 20:00)");
        }
    }
    
    /**
     * Valida horario configurado por la empresa
     * Formato: "09:00-18:00" o "09:00-13:00,15:00-19:00" (con descanso)
     */
    private void validarHorarioConfigurando(LocalTime hora, String horario, DayOfWeek dia) {
        String[] rangos = horario.split(",");
        boolean dentroDeHorario = false;
        
        for (String rango : rangos) {
            String[] horas = rango.trim().split("-");
            if (horas.length != 2) {
                continue; // Formato inválido, saltar
            }
            
            try {
                LocalTime inicio = LocalTime.parse(horas[0].trim());
                LocalTime fin = LocalTime.parse(horas[1].trim());
                
                if (!hora.isBefore(inicio) && !hora.isAfter(fin)) {
                    dentroDeHorario = true;
                    break;
                }
            } catch (Exception e) {
                // Si hay error parseando, continuar con el siguiente rango
                continue;
            }
        }
        
        if (!dentroDeHorario) {
            throw new IllegalArgumentException(
                    "La reserva debe estar dentro del horario de atención del " + 
                    traducirDiaSemana(dia) + ": " + horario);
        }
    }
    
    /**
     * Traduce el día de la semana al español
     */
    private String traducirDiaSemana(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> "lunes";
            case TUESDAY -> "martes";
            case WEDNESDAY -> "miércoles";
            case THURSDAY -> "jueves";
            case FRIDAY -> "viernes";
            case SATURDAY -> "sábado";
            case SUNDAY -> "domingo";
        };
    }
    
    /**
     * Calcula el precio final considerando promociones
     */
    public Double calcularPrecioFinal(Servicio servicio, Long promocionId) {
        Double precioBase = servicio.getPrice();
        
        // Si no hay promoción, retornar precio base
        if (promocionId == null) {
            return precioBase;
        }
        
        // Buscar la promoción
        Promocion promocion = promocionRepository.findByIdAndDeletedFalse(promocionId)
                .orElseThrow(() -> new PromocionNotFoundException(promocionId));
        
        // Verificar que la promoción esté vigente
        if (!promocion.isVigente()) {
            throw new PromocionExpiredException(promocionId);
        }
        
        // Verificar que la promoción aplique al servicio
        if (!promocion.isAplicableAServicio(servicio.getId())) {
            throw new IllegalArgumentException(
                    "La promoción no aplica a este servicio. La promoción es específica para otro servicio.");
        }
        
        // Verificar que se cumpla el monto mínimo
        if (!promocion.isAplicable(precioBase)) {
            throw new IllegalArgumentException(
                    String.format("El monto del servicio ($%.2f) no cumple con el monto mínimo requerido ($%.2f) para aplicar la promoción",
                            precioBase, promocion.getMontoMinimo()));
        }
        
        // Calcular descuento
        Double descuento = promocion.calcularDescuento(precioBase);
        Double precioFinal = precioBase - descuento;
        
        // Asegurar que el precio final no sea negativo
        return Math.max(precioFinal, 0.0);
    }
    
    /**
     * Incrementa el contador de usos de una promoción después de completar una reserva
     */
    public void registrarUsoPromocion(Long promocionId) {
        if (promocionId == null) {
            return;
        }
        
        Promocion promocion = promocionRepository.findByIdAndDeletedFalse(promocionId)
                .orElseThrow(() -> new PromocionNotFoundException(promocionId));
        
        promocion.incrementarUsos();
        promocionRepository.save(promocion);
    }
}
