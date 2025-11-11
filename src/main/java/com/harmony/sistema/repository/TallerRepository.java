package com.harmony.sistema.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.harmony.sistema.model.Taller;

public interface TallerRepository extends JpaRepository<Taller, Long> {

    // Busca un taller por su nombre.
    Optional<Taller> findByNombre(String nombre);

    // Obtiene una lista de todos los talleres cuyo campo 'activo' es verdadero.
    List<Taller> findByActivoTrue();

}