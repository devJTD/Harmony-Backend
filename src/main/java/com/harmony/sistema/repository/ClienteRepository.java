package com.harmony.sistema.repository;

import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    // Busca un cliente por la entidad User asociada.
    Optional<Cliente> findByUser(User user);


    Optional<Cliente> findByCorreo(String correo);
}