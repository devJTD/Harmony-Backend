package com.harmony.sistema.service;

import java.util.List;
import java.util.UUID;
import java.util.Optional; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.harmony.sistema.dto.ClienteRegistroDTO;
import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.ClienteRepository;
import com.harmony.sistema.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; 

    @Autowired
    private EmailService emailService; 

    // --- MÉTODOS DE BÚSQUEDA ---
    
    // Verifica si un User existe con ese email
    public Optional<User> encontrarUserPorEmail(String email) { // MODIFICADO para devolver Optional
        return userRepository.findByEmail(email);
    }
    
    // Encuentra el Cliente asociado a un User
    public Cliente encontrarClientePorEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario con email " + email + " no encontrado."));
            
        return clienteRepository.findByUser(user)
            .orElseThrow(() -> new RuntimeException("Cliente asociado al User " + email + " no encontrado."));
    }
    
    // Encuentra un cliente por correo directamente en la tabla Cliente
    public Optional<Cliente> encontrarClientePorCorreo(String correo) {
        return clienteRepository.findByCorreo(correo); 
    }
    
    // Obtiene y retorna una lista de todos los clientes en la base de datos.
    public List<Cliente> listarClientes() {
        System.out.println(" [CLIENTE SERVICE] Iniciando listado de todos los clientes.");
        List<Cliente> clientes = clienteRepository.findAll();
        System.out.println(" [CLIENTE SERVICE] Listado de clientes encontrado: " + clientes.size() + " clientes.");
        return clientes;
    }

    // **MÉTODO MODIFICADO (ANTES registrarCliente): SOLO CREA CLIENTE, SIN USER NI CORREO**
    public Cliente crearClienteTemporal(ClienteRegistroDTO dto) {
        System.out.println(" [CLIENTE SERVICE] Iniciando creación de Cliente TEMPORAL.");

        // 1. Crea y guarda la entidad Cliente (sin User)
        Cliente cliente = Cliente.builder()
                .nombreCompleto(dto.getNombreCompleto())
                .correo(dto.getCorreo()) 
                .telefono(dto.getTelefono())
                .user(null) 
                .build();
        clienteRepository.save(cliente);
        System.out.println(" [CLIENTE SERVICE] Entidad Cliente TEMPORAL (" + cliente.getCorreo()
                + ") guardada. ID: " + cliente.getId());

        return cliente;
    }

    // Actualiza los datos de un cliente (nombre y teléfono) y, si el correo cambia,
    // actualiza el email del User, genera una nueva contraseña temporal y envía un
    // correo.
    public Cliente actualizarCliente(Long clienteId, String nuevoNombre, String nuevoCorreo, String nuevoTelefono,
            String originalCorreo) {
        System.out.println(" [CLIENTE SERVICE] Iniciando actualización para Cliente ID: " + clienteId);
        // 1. Busca el Cliente y su User asociado.
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + clienteId));

        User user = cliente.getUser();
        
        // 2. Actualiza los campos del Cliente.
        cliente.setNombreCompleto(nuevoNombre);
        cliente.setTelefono(nuevoTelefono);
        cliente.setCorreo(nuevoCorreo); // <--- Actualizar el correo en la entidad Cliente

        boolean emailCambiado = !originalCorreo.equalsIgnoreCase(nuevoCorreo);

        if (emailCambiado && user != null) {
            System.out.println(" [CLIENTE SERVICE] Correo electrónico cambiado. Actualizando credenciales.");

            // 3a. Valida que el nuevo correo no esté ya asignado a otro usuario.
            if (userRepository.findByEmail(nuevoCorreo).isPresent() &&
                    !userRepository.findByEmail(nuevoCorreo).get().getId().equals(user.getId())) {
                System.out.println(" [CLIENTE SERVICE ERROR] El nuevo correo " + nuevoCorreo + " ya existe.");
                throw new RuntimeException("El nuevo correo electrónico ya está registrado en el sistema.");
            }

            // 3b. Genera nueva contraseña, actualiza email y contraseña en User, y guarda.
            String nuevaPasswordRandom = generadorRandomPassword();
            System.out.println(" [CLIENTE SERVICE] Nueva contraseña temporal generada.");

            user.setEmail(nuevoCorreo);
            user.setPassword(passwordEncoder.encode(nuevaPasswordRandom));
            userRepository.save(user);
            System.out.println(" [CLIENTE SERVICE] User actualizado y guardado con nueva contraseña.");

            // 3c. Envía una notificación por correo con las nuevas credenciales temporales.
            String asunto = "IMPORTANTE: Cambio de Correo y Contraseña Temporal - Harmony";
            String cuerpo = "Hola " + nuevoNombre + ",\n\n" +
                    "Tu correo electrónico ha sido actualizado a **" + nuevoCorreo + "** por un administrador.\n" +
                    "Se ha generado una nueva contraseña temporal por seguridad.\n\n" +
                    "Tu **NUEVA** Contraseña Temporal es: " + nuevaPasswordRandom + "\n\n" +
                    "Por favor, úsala para iniciar sesión. Tu contraseña anterior ya no es válida.\n\n" +
                    "Saludos,\nEquipo Harmony";

            emailService.enviarCorreo(nuevoCorreo, asunto, cuerpo);
            System.out.println(" [CLIENTE SERVICE] Correo de notificación enviado al nuevo email.");

        } else if (!emailCambiado && user != null) {
            // 4. Si el email no cambió, solo guarda el User para mantener la sesión abierta
            userRepository.save(user);
            System.out.println(
                    " [CLIENTE SERVICE] Email no cambiado. Solo actualizando datos del cliente y guardando User.");
        }
        
        // 5. Guarda el Cliente actualizado.
        clienteRepository.save(cliente);
        System.out.println(" [CLIENTE SERVICE] Cliente ID " + clienteId + " actualizado exitosamente.");

        return cliente;
    }

    // Elimina de forma transaccional el registro de Cliente y su User asociado de
    // la base de datos.
    @Transactional
    public void eliminarCliente(Long clienteId) {
        System.out.println(" [CLIENTE SERVICE] Iniciando eliminación transaccional para Cliente ID: " + clienteId);
        // 1. Busca el Cliente y su User asociado.
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + clienteId));

        User user = cliente.getUser();
        if (user == null) {
            clienteRepository.delete(cliente);
            System.out.println(" [CLIENTE SERVICE] Cliente sin User asociado. Solo eliminando cliente.");
            throw new RuntimeException("El cliente fue eliminado, pero no tenía un usuario asociado (User).");
        }

        // 2. Elimina el Cliente (las inscripciones se eliminan en cascada si la
        // relación lo permite).
        clienteRepository.delete(cliente);
        System.out.println(" [CLIENTE SERVICE] Cliente ID " + clienteId + " eliminado.");

        // 3. Elimina el User asociado.
        userRepository.delete(user);
        System.out.println(" [CLIENTE SERVICE] User asociado eliminado. Eliminación completada.");
    }

    // Genera una cadena de contraseña temporal única.
    public String generadorRandomPassword() {
        String random = UUID.randomUUID().toString().substring(0, 8);
        return "temporal-" + random;
    }
}