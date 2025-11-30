package com.harmony.sistema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.harmony.sistema.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    // Servicio para cargar detalles de usuario por email
    @Bean
    public UserDetailsService userDetailsService() {
        System.out.println(" [CONFIG] Inicializando Bean: UserDetailsService (Cargador de usuarios por Email)");
        // Busca usuario por email o lanza excepción
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    // Configura el proveedor de autenticación
    @SuppressWarnings("deprecation")
    @Bean
    public AuthenticationProvider authenticationProvider() {
        System.out.println(" [CONFIG] Inicializando Bean: AuthenticationProvider (DaoAuthenticationProvider)");
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Asigna servicio de usuarios y encriptador
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Gestor de autenticación global
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        System.out.println(" [CONFIG] Inicializando Bean: AuthenticationManager");
        // Retorna el AuthenticationManager
        return config.getAuthenticationManager();
    }

    // Codificador de contraseñas BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println(" [CONFIG] Inicializando Bean: PasswordEncoder (BCrypt)");
        return new BCryptPasswordEncoder();
    }
}