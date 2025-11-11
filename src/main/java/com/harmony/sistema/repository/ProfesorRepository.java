package com.harmony.sistema.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.model.User;

@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, Long> {
    // Busca un profesor por la entidad User asociada.
    Optional<Profesor> findByUser(User user);

    // Busca un profesor por su nombre completo.
    Optional<Profesor> findByNombreCompleto(String nombreCompleto);
}