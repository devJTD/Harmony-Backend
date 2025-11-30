package com.harmony.sistema.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmony.sistema.dto.AuthResponse;
import com.harmony.sistema.dto.LoginRequest;
import com.harmony.sistema.dto.RegisterRequest;
import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.model.Role;
import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.ClienteRepository;
import com.harmony.sistema.repository.ProfesorRepository;
import com.harmony.sistema.repository.RoleRepository;
import com.harmony.sistema.repository.UserRepository;
import com.harmony.sistema.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClienteRepository clienteRepository;
    private final ProfesorRepository profesorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    /**
     * Registra un nuevo usuario con el rol ROLE_CLIENTE por defecto.
     * Utilizado para registros públicos desde la web.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        System.out.println("[INFO] [AUTH] ========================================");
        System.out.println("[INFO] [AUTH] Iniciando registro para el email: " + request.getEmail());

        // 1. Verificar si el usuario ya existe
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            System.err.println("[ERROR] [AUTH] El email ya está registrado: " + request.getEmail());
            throw new RuntimeException("El email ya está registrado en el sistema");
        }

        // 2. Buscar el rol por defecto
        Optional<Role> userRole = roleRepository.findByName("ROLE_CLIENTE");

        if (userRole.isEmpty()) {
            System.err.println("[ERROR] [AUTH] El rol 'ROLE_CLIENTE' no existe en la BD");
            throw new RuntimeException("Error de configuración: Rol ROLE_CLIENTE no encontrado");
        }
        System.out.println("[INFO] [AUTH] Rol 'ROLE_CLIENTE' encontrado");

        // 3. Construir y guardar el objeto User
        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(Collections.singleton(userRole.get()))
                .build();

        System.out.println("[INFO] [AUTH] Usuario construido, guardando en BD...");
        userRepository.save(user);
        System.out.println("[SUCCESS] [AUTH] Usuario guardado exitosamente");

        // 4. Generar el token JWT
        var jwtToken = jwtService.generateToken(user);
        System.out.println("[SUCCESS] [AUTH] Token JWT generado");
        System.out.println("[INFO] [AUTH] ========================================");

        // 5. Retornar respuesta con token y rol
        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .role("ROLE_CLIENTE")
                .build();
    }

    /**
     * Autentica a un usuario y retorna el JWT con su información de rol y nombre.
     * Busca en las tablas Cliente y Profesor para obtener datos adicionales.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        System.out.println("[INFO] [AUTH] ========================================");
        System.out.println("[INFO] [AUTH] Iniciando login para: " + request.getEmail());

        try {
            // 1. Autenticar credenciales
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
            System.out.println("[SUCCESS] [AUTH] Credenciales autenticadas correctamente");

        } catch (BadCredentialsException e) {
            System.err.println("[ERROR] [AUTH] Credenciales inválidas para: " + request.getEmail());
            throw new BadCredentialsException("Email o contraseña incorrectos");
        }

        // 2. Recuperar la entidad User
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    System.err.println("[ERROR] [AUTH] Usuario no encontrado: " + request.getEmail());
                    return new RuntimeException("Usuario no encontrado");
                });

        System.out.println("[INFO] [AUTH] Usuario recuperado de BD");

        // 3. Determinar el rol del usuario
        String roleName = user.getRoles().isEmpty()
                ? "ROLE_CLIENTE"
                : user.getRoles().iterator().next().getName();

        System.out.println("[INFO] [AUTH] Rol detectado: " + roleName);

        // 4. Obtener el nombre completo y el ID según el rol
        String nombreCompleto = user.getEmail();
        Long userId = null;

        try {
            if ("ROLE_CLIENTE".equals(roleName)) {
                Optional<Cliente> clienteOpt = clienteRepository.findByUser(user);
                if (clienteOpt.isPresent()) {
                    nombreCompleto = clienteOpt.get().getNombreCompleto();
                    userId = clienteOpt.get().getId();
                    System.out.println("[INFO] [AUTH] Datos de Cliente encontrados. ID: " + userId);
                }
            } else if ("ROLE_PROFESOR".equals(roleName)) {
                Optional<Profesor> profesorOpt = profesorRepository.findByUser(user);
                if (profesorOpt.isPresent()) {
                    nombreCompleto = profesorOpt.get().getNombreCompleto();
                    userId = profesorOpt.get().getId();
                    System.out.println("[INFO] [AUTH] Datos de Profesor encontrados. ID: " + userId);
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] [AUTH] Error obteniendo datos adicionales: " + e.getMessage());
        }

        System.out.println("[INFO] [AUTH] Nombre completo: " + nombreCompleto);

        // 5. Generar el token JWT
        var jwtToken = jwtService.generateToken(user);

        System.out.println("[INFO] [AUTH] ========================================");
        System.out.println("[SUCCESS] [AUTH] JWT GENERADO PARA: " + request.getEmail());
        System.out.println("[INFO] [AUTH] ROL: " + roleName);
        System.out.println("[INFO] [AUTH] NOMBRE: " + nombreCompleto);
        System.out.println("[INFO] [AUTH] ID: " + userId);
        System.out.println(
                "[DEBUG] [AUTH] TOKEN (primeros 50 chars): " + jwtToken.substring(0, Math.min(50, jwtToken.length()))
                        + "...");
        System.out.println("[INFO] [AUTH] ========================================");

        // 6. Retornar respuesta completa
        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .role(roleName)
                .nombreCompleto(nombreCompleto)
                .id(userId)
                .build();
    }

    /**
     * Genera un token de recuperación de contraseña para el email dado.
     * Envia el token por correo.
     */
    @Transactional
    public void forgotPassword(String email) {
        System.out.println("[INFO] [AUTH] ========================================");
        System.out.println("[INFO] [AUTH] Solicitud de recuperación para: " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Generar código de 6 dígitos
        String code = String.format("%06d", new java.util.Random().nextInt(999999));

        // Establecer expiración (15 minutos)
        user.setResetToken(code);
        user.setResetTokenExpiry(java.time.LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        System.out.println("[INFO] [AUTH] Código generado: " + code);

        // Enviar correo
        String asunto = "Recuperación de Contraseña - Harmony";
        String cuerpo = "Hola,\n\nHas solicitado restablecer tu contraseña.\n" +
                "Tu código de verificación es: " + code + "\n\n" +
                "Este código expira en 15 minutos.\n\n" +
                "Si no solicitaste esto, ignora este mensaje.";

        emailService.enviarCorreo(email, asunto, cuerpo);

        System.out.println("[INFO] [AUTH] ========================================");
    }

    /**
     * Restablece la contraseña usando el token de recuperación.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        System.out.println("[INFO] [AUTH] ========================================");
        System.out.println("[INFO] [AUTH] Restableciendo contraseña con token: " + token);

        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);

        System.out.println("[SUCCESS] [AUTH] Contraseña actualizada exitosamente");
        System.out.println("[INFO] [AUTH] ========================================");
    }
}