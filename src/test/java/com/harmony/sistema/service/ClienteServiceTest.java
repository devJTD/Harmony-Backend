package com.harmony.sistema.service;

import com.harmony.sistema.dto.ClienteRegistroDTO;
import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.ClienteRepository;
import com.harmony.sistema.repository.UserRepository;
import com.harmony.sistema.repository.InscripcionRepository;
import com.harmony.sistema.repository.HorarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private HorarioRepository horarioRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void testEncontrarUserPorEmail() {
        // Crear un usuario de prueba
        User user = User.builder()
                .email("test@test.com")
                .password("password")
                .build();

        // Configurar el mock
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        // Ejecutar el método
        Optional<User> resultado = clienteService.encontrarUserPorEmail("test@test.com");

        // Verificar que funciona
        assertTrue(resultado.isPresent());
        assertEquals("test@test.com", resultado.get().getEmail());
    }

    @Test
    void testCrearClienteTemporal() {
        // Crear DTO de prueba
        ClienteRegistroDTO dto = new ClienteRegistroDTO();
        dto.setNombreCompleto("Juan Pérez");
        dto.setCorreo("juan@test.com");
        dto.setTelefono("123456789");

        // Crear cliente esperado
        Cliente clienteEsperado = Cliente.builder()
                .id(1L)
                .nombreCompleto("Juan Pérez")
                .correo("juan@test.com")
                .telefono("123456789")
                .build();

        // Configurar el mock
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteEsperado);

        // Ejecutar el método
        Cliente resultado = clienteService.crearClienteTemporal(dto);

        // Verificar que funciona
        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getNombreCompleto());
        assertEquals("juan@test.com", resultado.getCorreo());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void testGeneradorRandomPassword() {
        // Ejecutar el método
        String password = clienteService.generadorRandomPassword();

        // Verificar que funciona
        assertNotNull(password);
        assertTrue(password.startsWith("temporal-"));
        assertTrue(password.length() > 9);
    }
}
