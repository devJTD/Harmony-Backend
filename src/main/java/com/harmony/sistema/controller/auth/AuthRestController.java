package com.harmony.sistema.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harmony.sistema.dto.AuthResponse;
import com.harmony.sistema.dto.LoginRequest;
import com.harmony.sistema.dto.RegisterRequest;
import com.harmony.sistema.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthService authService;

    /**
     * Endpoint de registro de usuarios.
     * POST /api/auth/register
     */
    @PostMapping("/register")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        System.out.println("========================================");
        System.out.println("[AUTH REST] POST /api/auth/register");
        System.out.println("[AUTH REST] Email a registrar: " + request.getEmail());
        System.out.println("========================================");

        try {
            AuthResponse response = authService.register(request);

            System.out.println("[AUTH REST SUCCESS] Usuario registrado exitosamente");
            System.out.println("[AUTH REST] Token JWT generado y enviado");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("[AUTH REST ERROR] Error en registro: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Endpoint de login de usuarios.
     * POST /api/auth/login
     * Retorna el token JWT y la información del usuario con su rol.
     */
    @PostMapping("/login")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        System.out.println("========================================");
        System.out.println("[AUTH REST] POST /api/auth/login");
        System.out.println("[AUTH REST] Email de login: " + request.getEmail());
        System.out.println("========================================");

        try {
            // Llamar al servicio de autenticación
            AuthResponse response = authService.login(request);

            System.out.println("[AUTH REST SUCCESS] Login exitoso para: " + request.getEmail());
            System.out.println("[AUTH REST] Token JWT generado");
            System.out.println("[AUTH REST] Rol del usuario: " + response.getClass().getSimpleName());
            System.out.println("========================================");

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            System.err.println("[AUTH REST ERROR] Credenciales inválidas para: " + request.getEmail());
            System.err.println("[AUTH REST ERROR] Mensaje: " + e.getMessage());
            System.out.println("========================================");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (Exception e) {
            System.err.println("[AUTH REST ERROR] Error inesperado en login");
            System.err.println("[AUTH REST ERROR] Usuario: " + request.getEmail());
            System.err.println("[AUTH REST ERROR] Mensaje: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para verificar si el token es válido.
     * GET /api/auth/verify
     */
    @GetMapping("/verify")
    public ResponseEntity<Void> verifyToken() {
        System.out.println("[AUTH REST] GET /api/auth/verify - Token válido");
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint de logout (opcional, ya que JWT es stateless).
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        System.out.println("[AUTH REST] POST /api/auth/logout");
        System.out.println("[AUTH REST] Sesión cerrada (token invalidado en cliente)");
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint para solicitar recuperación de contraseña.
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<java.util.Map<String, String>> forgotPassword(
            @RequestBody java.util.Map<String, String> request) {
        String email = request.get("email");
        System.out.println("[AUTH REST] POST /api/auth/forgot-password - Email: " + email);

        try {
            authService.forgotPassword(email);
            return ResponseEntity
                    .ok(java.util.Map.of("message", "Si el correo existe, se ha enviado un código de recuperación."));
        } catch (Exception e) {
            // Por seguridad, no deberíamos decir si el usuario existe o no, pero para debug
            // lo dejamos así por ahora
            // O retornamos el mismo mensaje de éxito para evitar enumeración de usuarios
            return ResponseEntity
                    .ok(java.util.Map.of("message", "Si el correo existe, se ha enviado un código de recuperación."));
        }
    }

    /**
     * Endpoint para restablecer la contraseña.
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<java.util.Map<String, String>> resetPassword(
            @RequestBody java.util.Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        System.out.println("[AUTH REST] POST /api/auth/reset-password");

        try {
            authService.resetPassword(token, newPassword);
            return ResponseEntity.ok(java.util.Map.of("message", "Contraseña actualizada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}