package com.harmony.sistema.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmony.sistema.dto.ClienteRegistroDTO;
import com.harmony.sistema.dto.CredencialesDTO;
import com.harmony.sistema.dto.InscripcionFormDTO;
import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Role;
import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.ClienteRepository;
import com.harmony.sistema.repository.RoleRepository;
import com.harmony.sistema.repository.UserRepository;

/**
 * Servicio especializado para el registro y activación de clientes.
 * Responsabilidad única: Coordinar el proceso completo de registro de un nuevo
 * cliente.
 */
@Service
public class ServicioRegistroCliente {

    private static final String ROLE_CLIENTE = "ROLE_CLIENTE";

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ServicioPassword servicioPassword;

    @Autowired
    private ServicioNotificacion servicioNotificacion;

    /**
     * Registra y activa un nuevo cliente con su usuario asociado.
     * 
     * @param dto Datos del formulario de inscripción
     * @return Credenciales generadas (email y contraseña temporal)
     */
    @Transactional
    public CredencialesDTO registrarYActivarCliente(InscripcionFormDTO dto) {
        System.out.println("🔵 [SERVICIO REGISTRO CLIENTE] Iniciando registro para: " + dto.getEmail());

        // 1. Validar que no exista un usuario activo con este email
        Optional<User> userOpt = userRepository.findByEmail(dto.getEmail());
        if (userOpt.isPresent() && userOpt.get().isEnabled()) {
            System.out.println("❌ [SERVICIO REGISTRO CLIENTE ERROR] El correo ya tiene una cuenta activa.");
            throw new RuntimeException(
                    "El correo ya tiene una cuenta activa. Por favor, revisa el listado de clientes.");
        }

        // 2. Obtener o crear cliente
        Cliente cliente = obtenerOCrearCliente(dto);

        // 3. Generar contraseña temporal
        String password = servicioPassword.generarPasswordTemporal();
        String encodedPassword = passwordEncoder.encode(password);
        System.out.println("🔐 [SERVICIO REGISTRO CLIENTE] Contraseña encriptada generada.");

        // 4. Obtener rol de cliente
        Role roleCliente = roleRepository.findByName(ROLE_CLIENTE)
                .orElseThrow(() -> new RuntimeException("Error: El rol CLIENTE no fue encontrado."));
        System.out.println("✅ [SERVICIO REGISTRO CLIENTE] Rol 'ROLE_CLIENTE' obtenido.");

        // 5. Crear o actualizar usuario
        User newUser = userOpt.orElseGet(() -> User.builder().build());
        newUser.setEmail(dto.getEmail());
        newUser.setPassword(encodedPassword);
        newUser.setEnabled(true);
        newUser.setRoles(Set.of(roleCliente));

        userRepository.save(newUser);
        System.out.println("✅ [SERVICIO REGISTRO CLIENTE] User persistido con ID: " + newUser.getId());

        // 6. Asociar usuario al cliente
        cliente.setUser(newUser);
        clienteRepository.save(cliente);
        System.out.println("✅ [SERVICIO REGISTRO CLIENTE] Cliente asociado al User.");

        // 7. Enviar credenciales por correo
        servicioNotificacion.enviarCredenciales(dto.getEmail(), dto.getNombre(), password);

        System.out.println("✅ [SERVICIO REGISTRO CLIENTE SUCCESS] Proceso de registro completado.");
        return new CredencialesDTO(dto.getEmail(), password);
    }

    /**
     * Obtiene un cliente existente o crea uno nuevo si no existe.
     */
    private Cliente obtenerOCrearCliente(InscripcionFormDTO dto) {
        Optional<Cliente> clienteOpt = clienteService.encontrarClientePorCorreo(dto.getEmail());

        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            cliente.setNombreCompleto(dto.getNombre());
            cliente.setTelefono(dto.getTelefono());
            clienteRepository.save(cliente);
            System.out.println(
                    "✅ [SERVICIO REGISTRO CLIENTE] Cliente existente actualizado (ID: " + cliente.getId() + ").");
            return cliente;
        } else {
            ClienteRegistroDTO registroDTO = new ClienteRegistroDTO(
                    dto.getNombre(),
                    dto.getEmail(),
                    dto.getTelefono());

            Cliente nuevoCliente = clienteService.crearClienteTemporal(registroDTO);
            System.out.println(
                    "✅ [SERVICIO REGISTRO CLIENTE] Nuevo cliente temporal creado (ID: " + nuevoCliente.getId() + ").");
            return nuevoCliente;
        }
    }
}
