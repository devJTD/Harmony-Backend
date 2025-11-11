package com.harmony.sistema.service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.Optional; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmony.sistema.dto.CredencialesDTO;
import com.harmony.sistema.dto.InscripcionFormDTO;
import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Inscripcion;
import com.harmony.sistema.model.Role;
import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.ClienteRepository;
import com.harmony.sistema.repository.HorarioRepository;
import com.harmony.sistema.repository.InscripcionRepository;
import com.harmony.sistema.repository.RoleRepository;
import com.harmony.sistema.repository.UserRepository;
import com.harmony.sistema.dto.DatosPersonalesFormDTO;
import com.harmony.sistema.dto.ClienteRegistroDTO;
import com.harmony.sistema.model.Cliente;

@Service
public class InscripcionService {

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
    private InscripcionRepository inscripcionRepository;
    @Autowired
    private HorarioRepository horarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;

    // --- MÉTODO PARA EL PASO 1: CREAR/OBTENER CLIENTE TEMPORAL (SIN USER) ---
    @Transactional
    public Cliente guardarOObtenerClienteTemporal(DatosPersonalesFormDTO datos) {

        System.out.println(
                " [INSCRIPCION SERVICE] Procesando datos personales para email: " + datos.getEmail());

        // 1. VERIFICACIÓN CRÍTICA: Buscar si el email ya existe en la tabla User
        Optional<User> userOpt = userRepository.findByEmail(datos.getEmail());
        if (userOpt.isPresent()) {
            User userExistente = userOpt.get();
            
            // 1a. Si el User existe Y está HABILITADO (ya pagó/confirmó), bloquear la reinscripción.
            if (userExistente.isEnabled()) {
                System.out.println(" [INSCRIPCION SERVICE BLOQUEO] El correo ya tiene una cuenta activa (enabled=true).");
                throw new RuntimeException("Ya tienes una cuenta activa con este correo. Por favor, inicia sesión para continuar.");
            }
            
            // 1b. Si existe pero NO está HABILITADO (enabled=false), podría ser un proceso incompleto.
            // Continuamos devolviendo el Cliente asociado, permitiendo que complete el pago
            // y active su cuenta si es necesario.
            Cliente clienteExistente = clienteService.encontrarClientePorEmail(datos.getEmail());
            System.out.println(" [INSCRIPCION SERVICE] Cliente ya existe con User INACTIVO (ID: " + clienteExistente.getId()
                    + "). Devolviendo existente para completar proceso.");
            return clienteExistente;
        }

        // 2. Verificar si el email ya existe en la tabla Cliente (cliente temporal)
        Optional<Cliente> clienteTemporalOpt = clienteService.encontrarClientePorCorreo(datos.getEmail());
        if (clienteTemporalOpt.isPresent()) {
             Cliente clienteTemporal = clienteTemporalOpt.get();
             // Actualizar datos del cliente temporal si es necesario
             clienteTemporal.setNombreCompleto(datos.getNombre());
             clienteTemporal.setTelefono(datos.getTelefono());
             clienteRepository.save(clienteTemporal);
             System.out.println(" [INSCRIPCION SERVICE] Cliente temporal ya existe (ID: " + clienteTemporal.getId()
                     + "). Devolviendo existente y actualizado.");
             return clienteTemporal;
        }

        // 3. Crear el DTO para el registro
        ClienteRegistroDTO registroDTO = new ClienteRegistroDTO(
                datos.getNombre(),
                datos.getEmail(),
                datos.getTelefono());

        // 4. Llama al método modificado que **SOLO crea el Cliente** (sin User/Email)
        Cliente clienteRecienCreado = clienteService.crearClienteTemporal(registroDTO);
        
        System.out.println(" [INSCRIPCION SERVICE] Cliente recién registrado como TEMPORAL (ID: "
                + clienteRecienCreado.getId() + ").");
        return clienteRecienCreado;
    }

    // ---------------------------------------------
    // MÉTODO PARA EL PASO 3: CREAR USUARIO, ASOCIAR A CLIENTE, INSCRIBIR Y ENVIAR CORREO
    // ---------------------------------------------
    /**
     * Procesa la inscripción completa: crea User (con contraseña temporal cifrada),
     * asocia al Cliente existente y genera las Inscripciones.
     * Retorna CredencialesDTO con el correo y la contraseña temporal (raw).
     */
    @Transactional
    public CredencialesDTO procesarInscripcionCompleta(InscripcionFormDTO dto,
            Map<Long, Long> horariosSeleccionados) {
        System.out.println(" [INSCRIPCION SERVICE] Iniciando proceso de inscripción completa para: "
                + dto.getEmail());

        // 1. Validar que NO exista un User asociado a este email. 
        // Esta validación debe ser redundante si el Paso 1 es correcto.
        Optional<User> userOpt = userRepository.findByEmail(dto.getEmail());
        if (userOpt.isPresent()) {
            User existingUser = userOpt.get();
            // Si el usuario ya existe y está enabled (lo que falló el Paso 1), lanzamos error.
            if (existingUser.isEnabled()) {
                 throw new RuntimeException("Error fatal de flujo: El correo ya tiene una cuenta activa. Por favor, inicie sesión.");
            }
            // Si no está enabled, usamos el User existente y solo actualizamos la contraseña
            // y habilitamos. Esto cubre el caso de un cliente que no confirmó su cuenta inicialmente.
            System.out.println(" [INSCRIPCION SERVICE] User encontrado pero INACTIVO. Se reutilizará y habilitará.");

        }
        
        // 2. Encontrar el Cliente existente (que debió ser creado en el paso 1)
        Cliente clienteExistente = clienteService.encontrarClientePorCorreo(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("No se encontró un cliente temporal con el correo: " + dto.getEmail()));
                
        // 3. Generar la contraseña temporal y cifrarla.
        String rawPassword = userService.generadorRandomPassword();
        System.out.println(" [INSCRIPCION SERVICE] Contraseña Temporal (Raw) generada: [REDACTED]");  
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println(" [INSCRIPCION SERVICE] Contraseña Encriptada (Encoded) generada.");

        // 4. Buscar el rol 'ROLE_CLIENTE'.
        Role roleCliente = roleRepository.findByName(ROLE_CLIENTE)
                .orElseThrow(() -> new RuntimeException("Error: El rol CLIENTE no fue encontrado."));
        System.out.println(" [INSCRIPCION SERVICE] Rol 'ROLE_CLIENTE' obtenido.");
        
        User newUser = userOpt.orElseGet(() -> User.builder().build()); // Crea nuevo User o usa el existente inactivo

        // 5. Configurar o actualizar el User.
        newUser.setEmail(dto.getEmail());
        newUser.setPassword(encodedPassword);
        newUser.setEnabled(true); // Se habilita al completar el pago
        newUser.setRoles(Set.of(roleCliente));

        userRepository.save(newUser);
        System.out.println(" [INSCRIPCION SERVICE] User persistido/actualizado con email: " + newUser.getEmail());

        // 6. Asocia el nuevo User al Cliente existente y actualiza sus datos si cambiaron.
        clienteExistente.setUser(newUser);
        clienteExistente.setNombreCompleto(dto.getNombre()); 
        clienteExistente.setTelefono(dto.getTelefono());      
        clienteRepository.save(clienteExistente);
        System.out.println(
                " [INSCRIPCION SERVICE] Cliente existente (ID: " + clienteExistente.getId() + ") asociado al nuevo User.");

        // 7. Itera sobre los horarios seleccionados, crea el registro de Inscripción
        LocalDate fechaActual = LocalDate.now();
        System.out.println(" [INSCRIPCION SERVICE] Procesando " + horariosSeleccionados.size()
                + " inscripciones.");

        horariosSeleccionados.forEach((tallerId, horarioId) -> {
            Horario horario = horarioRepository.findById(horarioId)
                    .orElseThrow(() -> new RuntimeException(
                                "Horario con ID " + horarioId + " no encontrado."));

            Inscripcion inscripcion = new Inscripcion();
            inscripcion.setCliente(clienteExistente); 
            inscripcion.setHorario(horario);
            inscripcion.setFechaInscripcion(fechaActual);
            inscripcion.setPagado(true);

            inscripcionRepository.save(inscripcion);
            System.out.println(" [INSCRIPCION SERVICE] Inscripción creada para Horario ID: " + horarioId);
        });

        // 8. Envía un correo electrónico al nuevo cliente con su contraseña temporal.
        try {
            String asunto = "¡Bienvenido a Harmony! Tus Credenciales de Acceso";
            String cuerpo = "Hola " + dto.getNombre() + ",\n\n" +
                            "¡Tu inscripción ha sido confirmada y tu cuenta ha sido creada con éxito! \n\n" +
                            "Tus credenciales de acceso son:\n" +
                            "Usuario (Correo Electrónico): " + dto.getEmail() + "\n" +
                            "Contraseña Temporal: " + rawPassword + "\n\n" +
                            "Por tu seguridad, te recomendamos encarecidamente cambiar tu contraseña inmediatamente después de iniciar sesión.\n\n" +
                            "¡Te esperamos en clase!\n" +
                            "Saludos cordiales,\n" +
                            "El equipo de Harmony";

            emailService.enviarCorreo(dto.getEmail(), asunto, cuerpo);
            System.out.println(" [INSCRIPCION SERVICE] Correo de bienvenida enviado a: " + dto.getEmail());
        } catch (Exception e) {
            System.out.println(" [INSCRIPCION SERVICE ERROR] Error al enviar el correo de bienvenida a "
                    + dto.getEmail() + ": "
                    + e.getMessage());
        }

        System.out.println(
                " [INSCRIPCION SERVICE SUCCESS] Proceso de inscripción completa finalizado.");

        // 9. RETORNA el DTO con el correo y la contraseña temporal (raw)
        return new CredencialesDTO(dto.getEmail(), rawPassword);
    }
}