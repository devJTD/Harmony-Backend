package com.harmony.sistema.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.UserRepository;

@Service
public class ContraseñaService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Actualiza la contraseña del usuario (sea Profesor o Cliente) buscando por su email en la tabla User, donde está almacenada la contraseña.
    @Transactional
    public boolean actualizarContrasena(String email, String nuevaContrasena) {
        System.out.println(" [CONTRASENA SERVICE] Iniciando actualización de contraseña para el email: " + email);
        
        // 1. Cifra la nueva contraseña.
        String contrasenaCodificada = passwordEncoder.encode(nuevaContrasena);
        System.out.println(" [CONTRASENA SERVICE] Nueva contraseña cifrada.");

        // 2. Busca al User por su email.
        Optional<User> userOptional = userRepository.findByEmail(email);
        System.out.println(" [CONTRASENA SERVICE] Búsqueda de User por email completada.");

        // 3. Si el usuario existe, actualiza su contraseña cifrada, guarda los cambios y retorna true. Si no existe, retorna false.
        return userOptional.map(user -> {
            System.out.println(" [CONTRASENA SERVICE] Usuario encontrado. Aplicando nueva contraseña.");
            user.setPassword(contrasenaCodificada);
            userRepository.save(user);
            System.out.println(" [CONTRASENA SERVICE SUCCESS] Contraseña actualizada y guardada exitosamente.");
            return true;
        }).orElseGet(() -> {
            System.out.println(" [CONTRASENA SERVICE FAILURE] Usuario con email: " + email + " no encontrado. No se actualizó la contraseña.");
            return false;
        });
    }
}