package com.harmony.sistema.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Procesa cada solicitud HTTP para extraer y validar el JWT del encabezado "Authorization",
    // y si es válido, autentica al usuario en el contexto de Spring Security.
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Verifica si el encabezado de autorización existe y tiene el prefijo "Bearer ". Si no, pasa al siguiente filtro.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extrae el token JWT y el email del usuario.
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);
        
        // 3. Procede solo si se extrajo un email y el usuario no está ya autenticado en el contexto de seguridad.
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 4. Carga los detalles del usuario desde la base de datos (UserDetails).
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. Valida si el token es válido para el usuario cargado.
            if (jwtService.isTokenValid(jwt, userDetails)) {
                
                // 6. Crea un token de autenticación para representar al usuario.
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // 7. Establece detalles de autenticación web para el token (incluye IP, sesión, etc.).
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 8. Establece el usuario como autenticado en el SecurityContext.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } 
        } 
        // 9. Pasa la solicitud al siguiente filtro en la cadena.
        filterChain.doFilter(request, response);
    }
}