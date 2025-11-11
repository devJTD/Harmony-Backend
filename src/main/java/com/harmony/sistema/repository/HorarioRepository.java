package com.harmony.sistema.repository;

import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.model.Taller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HorarioRepository extends JpaRepository<Horario, Long> {

    // Busca un horario por taller, profesor, días, hora de inicio y hora de fin.
    Optional<Horario> findByTallerAndProfesorAndDiasDeClaseAndHoraInicioAndHoraFin(
            Taller taller, Profesor profesor, String dias, LocalTime inicio, LocalTime fin);

    // Obtiene todos los horarios impartidos por un Profesor, usando su email de usuario.
    @Query("SELECT h FROM Horario h JOIN h.profesor p JOIN p.user u WHERE u.email = :email")
    List<Horario> findByProfesorUserEmail(@Param("email") String email);

    // Obtiene todos los horarios a los que un Cliente está inscrito, usando su email de usuario.
    @Query("SELECT i.horario FROM Inscripcion i JOIN i.cliente c JOIN c.user u WHERE u.email = :email")
    List<Horario> findByInscripcionesClienteUserEmail(@Param("email") String email);

    // Obtiene los horarios de un taller que aún no han comenzado (fechaInicio > hoy) y tienen vacantes disponibles (vacantesDisponibles > 0).
    List<Horario> findByTallerIdAndFechaInicioAfterAndVacantesDisponiblesGreaterThan(
            Long tallerId, LocalDate hoy, int vacantes);

    // Obtiene TODOS los horarios de un taller (usado para saber si todos ya empezaron).
    List<Horario> findByTallerId(Long tallerId);

}