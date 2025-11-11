package com.harmony.sistema.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.ClienteRepository;
import com.harmony.sistema.repository.ProfesorRepository;
import com.harmony.sistema.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    // Inyección de repositorios adicionales necesarios para buscar el rol y el nombre
    @Autowired
    private ClienteRepository clienteRepository; 
    @Autowired
    private ProfesorRepository profesorRepository; 

    // 1. Retorna una lista de todos los usuarios que tienen el rol de Profesor.
    public List<User> listarProfesor() {
        System.out.println(" [USER SERVICE] Listando todos los usuarios con rol de Profesor.");
        return userRepository.findProfesores();
    }

    // 1. Genera una cadena de contraseña temporal única con el prefijo "temporal-".
    public String generadorRandomPassword() {
        String random = UUID.randomUUID().toString().substring(0, 8);
        System.out.println(" [USER SERVICE] Contraseña temporal generada.");
        return "temporal-" + random;
    }
    
    // Obtiene el rol principal del usuario basado en su email.
    public String getRoleByUserEmail(String userEmail) {
        System.out.println(" [USER SERVICE] Buscando rol para el email: " + userEmail);
        // 1. Busca el usuario por email.
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        
        // 2. Si el usuario existe y tiene roles, retorna el nombre del primer rol encontrado.
        if (userOpt.isPresent() && !userOpt.get().getRoles().isEmpty()) {
            String roleName = userOpt.get().getRoles().iterator().next().getName();
            System.out.println(" [USER SERVICE] Rol encontrado: " + roleName);
            return roleName;
        }
        // 3. Si no existe o no tiene roles, retorna null.
        System.out.println(" [USER SERVICE] Usuario no encontrado o sin roles.");
        return null; 
    }

    // Obtiene el nombre completo de la entidad (Cliente o Profesor) asociada al User.
    public String getNombreCompletoByUserEmail(String userEmail) {
        System.out.println(" [USER SERVICE] Buscando nombre completo para el email: " + userEmail);
        // 1. Busca el usuario por email y verifica su existencia.
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            System.out.println(" [USER SERVICE] Usuario no encontrado.");
            return "Usuario Desconocido";
        }

        User user = userOpt.get();
        
        // 2. Intenta buscar la entidad Cliente asociada. Si la encuentra, retorna el nombre.
        Optional<Cliente> clienteOpt = clienteRepository.findByUser(user);
        if (clienteOpt.isPresent()) {
            String nombre = clienteOpt.get().getNombreCompleto();
            System.out.println(" [USER SERVICE] Nombre de Cliente encontrado: " + nombre);
            return nombre;
        }

        // 3. Intenta buscar la entidad Profesor asociada. Si la encuentra, retorna el nombre.
        Optional<Profesor> profesorOpt = profesorRepository.findByUser(user);
        if (profesorOpt.isPresent()) {
            String nombre = profesorOpt.get().getNombreCompleto();
            System.out.println(" [USER SERVICE] Nombre de Profesor encontrado: " + nombre);
            return nombre;
        }

        // 4. Si el User existe pero no es Cliente ni Profesor, retorna una etiqueta genérica.
        System.out.println(" [USER SERVICE] Usuario encontrado, pero sin entidad Cliente/Profesor asociada.");
        return "Usuario del Sistema";
    }

    
}