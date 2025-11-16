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

    // Configura la cadena de filtros de seguridad HTTP, definiendo políticas de
    // acceso, autenticación y sesión.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println(" [CONFIG] Inicializando Bean: SecurityFilterChain (Configuración de Seguridad HTTP)");

        http
                // 1. Deshabilita la protección CSRF.
                .csrf(csrf -> csrf.disable())

                // 2. Define las reglas de autorización para las rutas.
                .authorizeHttpRequests(authorize -> authorize
                        // Permite acceso público a rutas estáticas y páginas informativas.
                        .requestMatchers(PUBLIC_ROUTES).permitAll()

                        // 🚀 NUEVA REGLA: Permite acceso público al endpoint de talleres activos
                        .requestMatchers("/api/**").permitAll() // ⬅️ AÑADE ESTA LÍNEA

                        // Restringe el acceso a /admin/ a usuarios con el rol ADMIN.
                        .requestMatchers("/admin/**").permitAll()
                        // Restringe el acceso a /horario y /cambiar-clave a CLIENTE o PROFESOR.
                        .requestMatchers("/horario", "/cambiar-clave").hasAnyRole("CLIENTE", "PROFESOR")

                        // Requiere autenticación para cualquier otra solicitud no mapeada previamente.
                        .anyRequest().authenticated())

                // 3. Establece el proveedor de autenticación personalizado.
                .authenticationProvider(authenticationProvider)
                // 4. Agrega el filtro JWT antes del filtro de autenticación de
                // usuario/contraseña.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // 5. Configura el manejo del formulario de inicio de sesión.
                .formLogin(form -> form
                        .loginPage("/login") // Define la URL de la página de login.
                        .successHandler(mySuccessHandler()) // Usa un manejador de éxito para redirigir por rol.
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll() // Permite el acceso a la página de login para todos.
                )

                // 6. Configura el manejo del cierre de sesión.
                .logout(logout -> logout
                        .permitAll() // Permite el acceso a la ruta de logout para todos.
                )

                // 7. Configura el manejo de sesiones.
                .sessionManagement(session -> session
                        // Previene ataques de fijación de sesión creando una nueva sesión.
                        .sessionFixation(sessioFixation -> sessioFixation.newSession())
                        // Configura la creación de sesiones solo si es necesaria.
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        // Redirige si la sesión es inválida.
                        .invalidSessionUrl("/login")
                        // Limita el número de sesiones concurrentes a 1 por usuario.
                        .maximumSessions(1)
                        // Redirige si la sesión expira.
                        .expiredUrl("/login"));

        // 8. Construye y retorna la cadena de filtros de seguridad.
        return http.build();
    }

    // Crea un bean para manejar la redirección del usuario inmediatamente después
    // de un inicio de sesión exitoso, basado en su rol.
    @Bean
    public AuthenticationSuccessHandler mySuccessHandler() {
        System.out
                .println(" [CONFIG] Inicializando Bean: AuthenticationSuccessHandler (Manejo de redirección por Rol)");
        return (request, response, authentication) -> {
            // 1. Obtiene los roles del usuario autenticado.
            var roles = authentication.getAuthorities();

            // 2. Redirige al panel de administración si el rol es ADMIN.
            if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
                System.out.println(" [AUTH] Login exitoso. Redirigiendo a /admin/clientes.");
                response.sendRedirect("/admin/clientes");
                // 3. Redirige al horario si el rol es CLIENTE.
            } else if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_CLIENTE"))) {
                System.out.println(" [AUTH] Login exitoso. Redirigiendo a /horario (Cliente).");
                response.sendRedirect("/horario");
                // 4. Redirige al horario si el rol es PROFESOR.
            } else if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_PROFESOR"))) {
                System.out.println(" [AUTH] Login exitoso. Redirigiendo a /horario (Profesor).");
                response.sendRedirect("/horario");
                // 5. Si no se encuentra un rol esperado, redirige a la página de login.
            } else {
                System.out.println(" [AUTH] Login exitoso. Rol no reconocido. Redirigiendo a /login.");
                response.sendRedirect("/login");
            }
        };
    }
}
