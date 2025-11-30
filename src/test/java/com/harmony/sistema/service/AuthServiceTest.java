package com.harmony.sistema.service;

import com.harmony.sistema.dto.LoginRequest;
import com.harmony.sistema.dto.RegisterRequest;
import com.harmony.sistema.dto.AuthResponse;
import com.harmony.sistema.model.Role;
import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.ClienteRepository;
import com.harmony.sistema.repository.ProfesorRepository;
import com.harmony.sistema.repository.RoleRepository;
import com.harmony.sistema.repository.UserRepository;
import com.harmony.sistema.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProfesorRepository profesorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Test
    void testRegister() {
        // Crear datos de prueba
        RegisterRequest request = RegisterRequest.builder()
                .email("nuevo@test.com")
                .password("password123")
                .build();

        Role roleCliente = Role.builder()
                .id(1L)
                .name("ROLE_CLIENTE")
                .build();

        Set<Role> roles = new HashSet<>();
        roles.add(roleCliente);

        User userEsperado = User.builder()
                .id(1L)
                .email("nuevo@test.com")
                .password("encodedPassword")
                .roles(roles)
                .build();

        // Configurar mocks
        when(userRepository.findByEmail("nuevo@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_CLIENTE")).thenReturn(Optional.of(roleCliente));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userEsperado);
        when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt-token");

        // Ejecutar el método
        AuthResponse resultado = authService.register(request);

        // Verificar que funciona
        assertNotNull(resultado);
        assertEquals("fake-jwt-token", resultado.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLogin() {
        // Crear datos de prueba
        LoginRequest request = LoginRequest.builder()
                .email("test@test.com")
                .password("password123")
                .build();

        Role roleCliente = Role.builder()
                .id(1L)
                .name("ROLE_CLIENTE")
                .build();

        Set<Role> roles = new HashSet<>();
        roles.add(roleCliente);

        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("encodedPassword")
                .roles(roles)
                .build();

        // Configurar mocks
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");
        when(clienteRepository.findByUser(user)).thenReturn(Optional.empty());
        when(profesorRepository.findByUser(user)).thenReturn(Optional.empty());

        // Ejecutar el método
        AuthResponse resultado = authService.login(request);

        // Verificar que funciona
        assertNotNull(resultado);
        assertEquals("fake-jwt-token", resultado.getToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
