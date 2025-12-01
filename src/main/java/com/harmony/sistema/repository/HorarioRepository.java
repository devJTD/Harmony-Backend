package com.harmony.sistema.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.model.Taller;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {

        // Busca horarios de clases asociados al Profesor cuyo email de usuario coincide
        // con el parámetro.
        @Query("SELECT h FROM Horario h LEFT JOIN FETCH h.profesor WHERE h.profesor.user.email = :email")
        List<Horario> findByProfesorUserEmail(@Param("email") String email);

        // Busca horarios de clases a los que el Cliente (identificado por su email de
        // usuario) está inscrito.
        @Query("SELECT DISTINCT h FROM Horario h " +
                        "LEFT JOIN FETCH h.profesor " +
                        "LEFT JOIN FETCH h.taller " +
                        "JOIN h.inscripciones i " +
                        "WHERE i.cliente.user.email = :email")
        List<Horario> findByInscripcionesClienteUserEmail(@Param("email") String email);

        // Busca horarios filtrando por el ID del taller, una fecha de inicio posterior
        // a la fecha dada y que tengan vacantes disponibles.
        @Query("SELECT h FROM Horario h " +
                        "LEFT JOIN FETCH h.profesor " +
                        "WHERE h.taller.id = :tallerId " +
                        "AND h.fechaInicio > :fecha " +
                        "AND h.vacantesDisponibles > :vacantes")
        List<Horario> findByTallerIdAndFechaInicioAfterAndVacantesDisponiblesGreaterThan(
                        @Param("tallerId") Long tallerId,
                        @Param("fecha") LocalDate fecha,
                        @Param("vacantes") Integer vacantes);

        // Busca y devuelve todos los horarios asociados a un Taller específico mediante
        // su ID.
        @Query("SELECT h FROM Horario h LEFT JOIN FETCH h.profesor WHERE h.taller.id = :tallerId")
        List<Horario> findByTallerId(@Param("tallerId") Long tallerId);

        // Busca un horario que coincida exactamente con los objetos Taller, Profesor,
        // los días y el rango de horas especificados.
        Optional<Horario> findByTallerAndProfesorAndDiasDeClaseAndHoraInicioAndHoraFin(
                        Taller taller,
                        Profesor profesor,
                        String diasDeClase,
                        LocalTime horaInicio,
                        LocalTime horaFin);

        // Busca horarios que no estén marcados como finalizados y cuya fecha de fin sea
        // anterior a la fecha actual.
        List<Horario> findByFinalizadoFalseAndFechaFinBefore(LocalDate fecha);
}