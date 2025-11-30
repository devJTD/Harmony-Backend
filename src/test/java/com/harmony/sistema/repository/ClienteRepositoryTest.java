package com.harmony.sistema.repository;

import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Role;
import com.harmony.sistema.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class ClienteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void testFindByCorreo() {
        // Crear y guardar un cliente
        Cliente cliente = Cliente.builder()
                .nombreCompleto("Test Cliente")
                .correo("test@test.com")
                .telefono("123456789")
                .build();
        entityManager.persist(cliente);
        entityManager.flush();

        // Ejecutar el método
        Optional<Cliente> resultado = clienteRepository.findByCorreo("test@test.com");

        // Verificar que funciona
        assertTrue(resultado.isPresent());
        assertEquals("Test Cliente", resultado.get().getNombreCompleto());
    }

    @Test
    void testFindByUserEmail() {
        // Crear y guardar un role
        Role role = Role.builder()
                .name("ROLE_CLIENTE")
                .build();
        entityManager.persist(role);

        // Crear y guardar un user
        User user = User.builder()
                .email("user@test.com")
                .password("password")
                .roles(new HashSet<>())
                .build();
        user.getRoles().add(role);
        entityManager.persist(user);

        // Crear y guardar un cliente asociado
        Cliente cliente = Cliente.builder()
                .nombreCompleto("Cliente con User")
                .correo("user@test.com")
                .telefono("987654321")
                .user(user)
                .build();
        entityManager.persist(cliente);
        entityManager.flush();

        // Ejecutar el método
        Optional<Cliente> resultado = clienteRepository.findByUserEmail("user@test.com");

        // Verificar que funciona
        assertTrue(resultado.isPresent());
        assertEquals("Cliente con User", resultado.get().getNombreCompleto());
    }
}
