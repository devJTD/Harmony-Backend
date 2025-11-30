package com.harmony.sistema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.harmony.sistema.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    private static final String[] PUBLIC_ROUTES = {
            "/", "/acerca", "/profesores", "/inscripcion", "/talleres",
            "/blog", "/pago", "/contacto/**", "/confirmacion", "/css/**", "/js/**", "/images/**",
    };

    // Configura la cadena de filtros de seguridad HTTP
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println(" [CONFIG] Inicializando Bean: SecurityFilterChain (Configuración de Seguridad HTTP)");

        http
                // Deshabilita CSRF y configura autorización
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_ROUTES).permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/admin/**").permitAll()
                        .requestMatchers("/horario", "/cambiar-clave").hasAnyRole("CLIENTE", "PROFESOR")
                        .anyRequest().authenticated())

                // Configura proveedor y filtro JWT
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Configura login y logout
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(mySuccessHandler())
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll())
                .logout(logout -> logout.permitAll())

                // Configura gestión de sesiones
                .sessionManagement(session -> session
                        .sessionFixation(sessioFixation -> sessioFixation.newSession())
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/login")
                        .maximumSessions(1)
                        .expiredUrl("/login"));

        return http.build();
    }

    // Maneja redirección post-login según rol
    @Bean
    public AuthenticationSuccessHandler mySuccessHandler() {
        System.out
                .println(" [CONFIG] Inicializando Bean: AuthenticationSuccessHandler (Manejo de redirección por Rol)");
        return (request, response, authentication) -> {
            var roles = authentication.getAuthorities();

            // Redirige según el rol detectado
            if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
                System.out.println(" [AUTH] Login exitoso. Redirigiendo a /admin/clientes.");
                response.sendRedirect("/admin/clientes");
            } else if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_CLIENTE"))) {
                System.out.println(" [AUTH] Login exitoso. Redirigiendo a /horario (Cliente).");
                response.sendRedirect("/horario");
            } else if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_PROFESOR"))) {
                System.out.println(" [AUTH] Login exitoso. Redirigiendo a /horario (Profesor).");
                response.sendRedirect("/horario");
            } else {
                System.out.println(" [AUTH] Login exitoso. Rol no reconocido. Redirigiendo a /login.");
                response.sendRedirect("/login");
            }
        };
    }
}
