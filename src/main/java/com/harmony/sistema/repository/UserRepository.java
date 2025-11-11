package com.harmony.sistema.repository;

import com.harmony.sistema.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Busca un usuario por su dirección de correo electrónico (email).
    Optional<User> findByEmail(String email);

    // Consulta todos los usuarios que tienen el rol 'PROFESOR'.
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'PROFESOR'")
    List<User> findProfesores();
}