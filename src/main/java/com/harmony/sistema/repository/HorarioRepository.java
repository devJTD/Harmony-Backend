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

        // ✅ CORREGIDO: Agregar FETCH JOIN para cargar el profesor
        @Query("SELECT h FROM Horario h LEFT JOIN FETCH h.profesor WHERE h.profesor.user.email = :email")
        List<Horario> findByProfesorUserEmail(@Param("email") String email);

        // ✅ CORREGIDO: Agregar FETCH JOIN para cargar el profesor y taller
        @Query("SELECT DISTINCT h FROM Horario h " +
                        "LEFT JOIN FETCH h.profesor " +
                        "LEFT JOIN FETCH h.taller " +
                        "JOIN h.inscripciones i " +
                        "WHERE i.cliente.user.email = :email")
        List<Horario> findByInscripcionesClienteUserEmail(@Param("email") String email);

        // ✅ CORREGIDO: Agregar FETCH JOIN para cargar el profesor
        @Query("SELECT h FROM Horario h " +
                        "LEFT JOIN FETCH h.profesor " +
                        "WHERE h.taller.id = :tallerId " +
                        "AND h.fechaInicio > :fecha " +
                        "AND h.vacantesDisponibles > :vacantes")
        List<Horario> findByTallerIdAndFechaInicioAfterAndVacantesDisponiblesGreaterThan(
                        @Param("tallerId") Long tallerId,
                        @Param("fecha") LocalDate fecha,
                        @Param("vacantes") Integer vacantes);

        // ✅ CORREGIDO: Agregar FETCH JOIN para cargar el profesor
        @Query("SELECT h FROM Horario h LEFT JOIN FETCH h.profesor WHERE h.taller.id = :tallerId")
        List<Horario> findByTallerId(@Param("tallerId") Long tallerId);

        Optional<Horario> findByTallerAndProfesorAndDiasDeClaseAndHoraInicioAndHoraFin(
                        Taller taller,
                        Profesor profesor,
                        String diasDeClase,
                        LocalTime horaInicio,
                        LocalTime horaFin);

        List<Horario> findByFinalizadoFalseAndFechaFinBefore(LocalDate fecha);
}
