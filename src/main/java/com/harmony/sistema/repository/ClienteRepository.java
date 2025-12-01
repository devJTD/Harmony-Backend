package com.harmony.sistema.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.User;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Busca un cliente por la entidad User asociada.
    Optional<Cliente> findByUser(User user);

    // Busca un cliente por su campo 'correo'.
    Optional<Cliente> findByCorreo(String correo);

    // Busca un cliente por el email del User asociado.
    @Query("SELECT c FROM Cliente c JOIN c.user u WHERE u.email = :email")
    Optional<Cliente> findByUserEmail(@Param("email") String email);

    // Busca un cliente por el ID de la entidad User asociada.
    Optional<Cliente> findByUserId(Long userId);
}