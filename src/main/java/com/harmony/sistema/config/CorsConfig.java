package com.harmony.sistema.config; // ⬅️ Asegúrate de que el paquete exista y sea escaneado

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // ⬅️ ¡CRUCIAL! Marca la clase como configuración de Spring.
public class CorsConfig implements WebMvcConfigurer { // ⬅️ Interfaz necesaria para CORS global

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // **ESTA LÍNEA ES LA CLAVE:** Le dice a Spring que acepte peticiones desde Angular (4200)
        registry.addMapping("/**") // Aplica a todas las rutas de la API
                .allowedOrigins("http://localhost:4200") // ⬅️ ¡El origen de Angular!
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") 
                .allowedHeaders("*") 
                .allowCredentials(true); 
    }
}