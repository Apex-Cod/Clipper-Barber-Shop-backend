package apex.code.clipperBarberShop.reserva.adapters.out.persistence;

import apex.code.clipperBarberShop.Entities.Reserva;
import apex.code.clipperBarberShop.Entities.enums.ReservaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaReservaRepository extends JpaRepository<Reserva, Long> {
    
    Optional<Reserva> findByIdAndDeletedFalse(Long id);
    
    List<Reserva> findByClientIdAndDeletedFalse(String clientId);
    
    Page<Reserva> findByClientIdAndDeletedFalse(String clientId, Pageable pageable);
    
    List<Reserva> findByEmpresaIdAndDeletedFalse(Long empresaId);
    
    Page<Reserva> findByEmpresaIdAndDeletedFalse(Long empresaId, Pageable pageable);
    
    List<Reserva> findByEmployeeIdAndDeletedFalse(String employeeId);
    
    List<Reserva> findByClientIdAndStatusAndDeletedFalse(String clientId, String status);
    
    List<Reserva> findByEmpresaIdAndStatusAndDeletedFalse(Long empresaId, String status);
    
    @Query("SELECT r FROM Reserva r WHERE r.employeeId = :employeeId " +
           "AND r.reservationDate BETWEEN :start AND :end " +
           "AND r.status <> :status " +
           "AND r.deleted = false")
    List<Reserva> findByEmployeeIdAndReservationDateBetweenAndStatusNotAndDeletedFalse(
            @Param("employeeId") String employeeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") String status);
    
    @Query("SELECT r FROM Reserva r WHERE r.empresa.id = :empresaId " +
           "AND r.reservationDate BETWEEN :start AND :end " +
           "AND r.deleted = false")
    List<Reserva> findByEmpresaIdAndReservationDateBetweenAndDeletedFalse(
            @Param("empresaId") Long empresaId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    boolean existsByIdAndClientIdAndDeletedFalse(Long id, String clientId);
    
    boolean existsByIdAndEmpresaIdAndDeletedFalse(Long id, Long empresaId);
    
    // Buscar reservas por empresa, fecha y estados específicos
    @Query("SELECT r FROM Reserva r WHERE r.empresa.id = :empresaId " +
           "AND DATE(r.reservationDate) = :fecha " +
           "AND r.status IN :statuses " +
           "AND r.deleted = false")
    List<Reserva> findByEmpresaIdAndFechaAndStatusIn(
            @Param("empresaId") Long empresaId,
            @Param("fecha") LocalDate fecha,
            @Param("statuses") List<ReservaStatus> statuses);

    // Buscar reservas por empresa, fecha, empleado y estados específicos
    @Query("SELECT r FROM Reserva r WHERE r.empresa.id = :empresaId " +
           "AND DATE(r.reservationDate) = :fecha " +
           "AND r.employeeId = :empleadoId " +
           "AND r.status IN :statuses " +
           "AND r.deleted = false")
    List<Reserva> findByEmpresaIdAndFechaAndEmpleadoIdAndStatusIn(
            @Param("empresaId") Long empresaId,
            @Param("fecha") LocalDate fecha,
            @Param("empleadoId") String empleadoId,
            @Param("statuses") List<ReservaStatus> statuses);
    
    // Buscar reservas por empresa y rango de fechas con estados específicos
    @Query("SELECT r FROM Reserva r WHERE r.empresa.id = :empresaId " +
           "AND r.reservationDate BETWEEN :start AND :end " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND r.deleted = false")
    List<Reserva> findByEmpresaIdAndReservationDateBetweenAndStatusIn(
            @Param("empresaId") Long empresaId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statuses") List<String> statuses);

    // Buscar reservas por empresa, empleado y rango de fechas con estados específicos
    @Query("SELECT r FROM Reserva r WHERE r.empresa.id = :empresaId " +
           "AND r.employeeId = :employeeId " +
           "AND r.reservationDate BETWEEN :start AND :end " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND r.deleted = false")
    List<Reserva> findByEmpresaIdAndEmployeeIdAndReservationDateBetweenAndStatusIn(
            @Param("empresaId") Long empresaId,
            @Param("employeeId") String employeeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statuses") List<String> statuses);
}
