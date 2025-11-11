package com.harmony.sistema.repository;

import com.harmony.sistema.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    // Busca un rol por su nombre (ej: "ROLE_ADMIN", "ROLE_CLIENTE").
    Optional<Role> findByName(String name);
}