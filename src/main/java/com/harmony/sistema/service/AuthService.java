package com.harmony.sistema.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.harmony.sistema.dto.AuthResponse;
import com.harmony.sistema.dto.LoginRequest;
import com.harmony.sistema.dto.RegisterRequest;
import com.harmony.sistema.model.Role;
import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.RoleRepository;
import com.harmony.sistema.repository.UserRepository;
import com.harmony.sistema.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // Procesa la solicitud de registro de un nuevo usuario con el rol por defecto 'ROLE_CLIENTE', guarda el usuario, y genera un JWT.
    public AuthResponse register(RegisterRequest request) {
        System.out.println(" [AUTH SERVICE] Iniciando registro para el email: " + request.getEmail());
        
        // 1. Busca el rol por defecto.
        Optional<Role> userRole = roleRepository.findByName("ROLE_CLIENTE"); 
        
        if (userRole.isEmpty()) {
            System.out.println(" [AUTH SERVICE ERROR] El rol por defecto 'ROLE_CLIENTE' no se encontró.");
            throw new RuntimeException("El rol por defecto 'ROLE_CLIENTE' no existe.");
        }
        System.out.println(" [AUTH SERVICE] Rol 'ROLE_CLIENTE' encontrado.");

        // 2. Construye el objeto User, codificando la contraseña y asignando el rol.
        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(Collections.singleton(userRole.get())) 
                .build();
        System.out.println(" [AUTH SERVICE] Objeto User construido y contraseña codificada.");
        
        // 3. Guarda el nuevo usuario en la base de datos.
        userRepository.save(user);
        System.out.println(" [AUTH SERVICE] Usuario guardado en la base de datos.");

        // 4. Genera el token JWT.
        var jwtToken = jwtService.generateToken(user);
        System.out.println(" [AUTH SERVICE] Token JWT generado exitosamente.");
        
        // 5. Retorna la respuesta de autenticación con el token.
        return AuthResponse.builder().token(jwtToken).build();
    }

    // Autentica al usuario usando las credenciales, recupera la entidad User y genera un token JWT.
    public AuthResponse login(LoginRequest request) {
        System.out.println(" [AUTH SERVICE] Iniciando login para el email: " + request.getEmail());
        
        // 1. Autentica las credenciales del usuario.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), 
                        request.getPassword()
                )
        );
        System.out.println(" [AUTH SERVICE] Autenticación de credenciales exitosa.");

        // 2. Recupera la entidad User (UserDetails).
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        System.out.println(" [AUTH SERVICE] Entidad User recuperada de la base de datos.");
        
        // 3. Genera el token JWT.
        var jwtToken = jwtService.generateToken(user);

        // 4. Imprime un log de la generación del token.
        System.out.println("---------------------------------------------------------------------------------------");
        System.out.println("| JWT GENERADO: Usuario [" + request.getEmail() + "] logueado exitosamente.");
        System.out.println("| TOKEN: " + jwtToken.substring(0, 50) + "...");
        System.out.println("---------------------------------------------------------------------------------------");
        
        // 5. Retorna la respuesta de autenticación con el token.
        return AuthResponse.builder().token(jwtToken).build();
    }
}
