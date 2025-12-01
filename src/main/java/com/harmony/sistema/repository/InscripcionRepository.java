package com.harmony.sistema.repository;

import com.harmony.sistema.model.Inscripcion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    // Busca una inscripción específica por el ID del cliente y el ID del horario
    // asociado.
    Optional<Inscripcion> findByClienteIdAndHorarioId(Long clienteId, Long horarioId);

    // Busca y devuelve una lista de todas las inscripciones realizadas por un
    // cliente específico (por su ID).
    java.util.List<Inscripcion> findByClienteId(Long clienteId);
}