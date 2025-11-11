package com.harmony.sistema.controller;

import org.springframework.http.ResponseEntity;
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
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Procesa la solicitud de registro, creando un nuevo usuario y generando un token de autenticación.
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        System.out.println(" [API] Solicitud POST a /api/auth/register. Intentando registrar usuario: " + request.getEmail());
        // 1. Llama al servicio de autenticación para registrar al usuario y obtener la respuesta.
        AuthResponse response = authService.register(request);
        System.out.println(" [API SUCCESS] Registro exitoso para usuario: " + request.getEmail());
        return ResponseEntity.ok(response);
    }

    // Procesa la solicitud de inicio de sesión, autenticando al usuario y generando un token JWT.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        System.out.println(" [API] Solicitud POST a /api/auth/login. Intentando autenticar usuario: " + request.getEmail());
        // 1. Llama al servicio de autenticación para loguear al usuario y obtener la respuesta.
        AuthResponse response = authService.login(request);
        System.out.println(" [API SUCCESS] Login exitoso para usuario: " + request.getEmail() + ". JWT generado.");
        return ResponseEntity.ok(response);
    }
}