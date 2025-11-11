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

    // Crea un bean UserDetailsService para cargar los detalles del usuario por email desde la base de datos.
    @Bean
    public UserDetailsService userDetailsService() {
        System.out.println(" [CONFIG] Inicializando Bean: UserDetailsService (Cargador de usuarios por Email)");
        // 1. Retorna una implementación lambda que busca el User por email en el repositorio.
        return username -> userRepository.findByEmail(username)
                // 2. Lanza una excepción si el usuario no es encontrado.
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    // Crea un bean AuthenticationProvider para manejar la autenticación usando los detalles del usuario y el codificador de contraseñas.
    @SuppressWarnings("deprecation")
    @Bean
    public AuthenticationProvider authenticationProvider() {
        System.out.println(" [CONFIG] Inicializando Bean: AuthenticationProvider (DaoAuthenticationProvider)");
        // 1. Crea una instancia de DaoAuthenticationProvider.
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // 2. Establece el UserDetailsService.
        authProvider.setUserDetailsService(userDetailsService());
        // 3. Establece el PasswordEncoder.
        authProvider.setPasswordEncoder(passwordEncoder());
        // 4. Retorna el proveedor de autenticación configurado.
        return authProvider;
    }

    // Expone el AuthenticationManager de Spring Security.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        System.out.println(" [CONFIG] Inicializando Bean: AuthenticationManager");
        // 1. Obtiene el AuthenticationManager de la configuración de autenticación.
        return config.getAuthenticationManager();
    }

    // Crea un bean PasswordEncoder utilizando BCrypt.
    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println(" [CONFIG] Inicializando Bean: PasswordEncoder (BCrypt)");
        // 1. Retorna una instancia de BCryptPasswordEncoder para el cifrado de contraseñas.
        return new BCryptPasswordEncoder();
    }
}