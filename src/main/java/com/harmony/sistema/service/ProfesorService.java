package com.harmony.sistema.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.harmony.sistema.dto.ProfesorEdicionDTO;
import com.harmony.sistema.dto.ProfesorRegistroDTO;
import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.model.Role;
import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.ProfesorRepository;
import com.harmony.sistema.repository.RoleRepository;
import com.harmony.sistema.repository.UserRepository;

@Service
public class ProfesorService {

    // Constante para el rol
    private static final String ROLE_PROFESOR = "ROLE_PROFESOR";

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // Obtiene y retorna una lista de todos los profesores.
    public List<Profesor> listarProfesores() {
        System.out.println("[INFO] [PROFESOR] Listando todos los profesores.");
        return profesorRepository.findAll();
    }

    // Obtiene un profesor por su ID, lanzando una excepción si no se encuentra.
    public Profesor obtenerProfesorPorId(Long profesorId) {
        System.out.println("[INFO] [PROFESOR] Buscando profesor por ID: " + profesorId);
        return profesorRepository.findById(profesorId)
                .orElseThrow(() -> new RuntimeException("Profesor con ID " + profesorId + " no encontrado."));
    }

    // Registra un nuevo profesor, creando su entidad User asociada con el rol
    // 'ROLE_PROFESOR', una contraseña temporal, y envía un correo electrónico.
    public Profesor registrarProfesor(ProfesorRegistroDTO dto) {
        System.out.println("[INFO] [PROFESOR] Iniciando registro de nuevo profesor.");
        // 1. Busca el rol y genera la contraseña temporal.
        Role roleProfesor = roleRepository.findByName(ROLE_PROFESOR)
                .orElseThrow(
                        () -> new RuntimeException("Error: El rol PROFESOR no fue encontrado en la base de datos"));

        String passwordRandom = generadorRandomPassword();
        System.out.println("[INFO] [PROFESOR] Contraseña temporal generada.");

        // 2. Crea y guarda la entidad User con la contraseña codificada y el rol de
        // profesor.
        User user = User.builder()
                .email(dto.getCorreo())
                .password(passwordEncoder.encode(passwordRandom))
                .enabled(true)
                .roles(Set.of(roleProfesor))
                .build();
        userRepository.save(user);
        System.out.println("[SUCCESS] [PROFESOR] Entidad User creada y guardada para: " + dto.getCorreo());

        // 3. Crea y guarda la entidad Profesor con sus datos asociados al User.
        Profesor profesor = Profesor.builder()
                .nombreCompleto(dto.getNombreCompleto())
                .telefono(dto.getTelefono())
                .fotoUrl(dto.getFotoUrl())
                .informacion(dto.getInformacion())
                .user(user)
                .build();

        Profesor savedProfesor = profesorRepository.save(profesor);
        System.out.println("[SUCCESS] [PROFESOR] Entidad Profesor creada y guardada con ID: " + savedProfesor.getId());

        // 4. Envía un correo electrónico con las credenciales temporales.
        String asunto = "Bienvenido a Harmony - Tu cuenta ha sido creada";
        String cuerpo = "Hola " + dto.getNombreCompleto() + ",\n\n" +
                "Tu cuenta ha sido registrada correctamente.\n" +
                "Tu correo es: " + dto.getCorreo() + "\n" +
                "Tu contraseña temporal es: " + passwordRandom + "\n\n" +
                "Por favor cámbiala al iniciar sesión.\n\n" +
                "Saludos,\nEquipo Harmony";

        emailService.enviarCorreo(dto.getCorreo(), asunto, cuerpo);
        System.out
                .println("[SUCCESS] [PROFESOR] Correo de credenciales temporales enviado a: " + dto.getCorreo());

        return savedProfesor;
    }

    // Elimina de forma definitiva un profesor y su entidad User asociada.
    public void eliminarProfesor(Long profesorId) {
        System.out.println("[INFO] [PROFESOR] Iniciando eliminación de profesor con ID: " + profesorId);
        // 1. Busca el Profesor y lanza excepción si no existe.
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new RuntimeException("Profesor con ID " + profesorId + " no encontrado."));

        // 2. Obtiene el usuario asociado.
        User user = profesor.getUser();

        // 3. Elimina la entidad Profesor.
        profesorRepository.delete(profesor);
        System.out.println("[SUCCESS] [PROFESOR] Entidad Profesor eliminada.");

        // 4. Elimina la entidad User asociada, si existe.
        if (user != null) {
            userRepository.delete(user);
            System.out.println("[SUCCESS] [PROFESOR] Entidad User asociada eliminada.");
        } else {
            System.out.println("[WARN] [PROFESOR] El profesor no tenía una entidad User asociada.");
        }
        System.out.println("[INFO] [PROFESOR] Eliminación de profesor finalizada para ID: " + profesorId);
    }

    // Edita los datos de un profesor y actualiza el email del User asociado si es
    // necesario.
    public Profesor editarProfesor(ProfesorEdicionDTO dto) {
        System.out.println("[INFO] [PROFESOR] Iniciando edición de profesor con ID: " + dto.getId());
        // 1. Obtiene el Profesor existente y verifica que tenga un User asociado.
        Profesor profesor = profesorRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Profesor con ID " + dto.getId() + " no encontrado."));

        User user = profesor.getUser();
        if (user == null) {
            System.err
                    .println("[ERROR] [PROFESOR] El profesor no tiene un usuario asociado. No se puede editar.");
            throw new RuntimeException("El profesor no tiene un usuario asociado. No se puede editar.");
        }

        // 2. Actualiza el email del Usuario si ha cambiado y lo guarda.
        if (!user.getEmail().equals(dto.getCorreo())) {
            System.out.println("[INFO] [PROFESOR] El email ha cambiado. Actualizando User email.");
            user.setEmail(dto.getCorreo());
            userRepository.save(user);
        } else {
            System.out.println("[INFO] [PROFESOR] El email no ha cambiado.");
        }

        // 3. Actualiza los demás datos del Profesor.
        profesor.setNombreCompleto(dto.getNombreCompleto());
        profesor.setTelefono(dto.getTelefono());
        profesor.setFotoUrl(dto.getFotoUrl());
        profesor.setInformacion(dto.getInformacion());
        System.out.println("[INFO] [PROFESOR] Datos del Profesor actualizados.");

        // 4. Guarda los cambios del Profesor.
        Profesor updatedProfesor = profesorRepository.save(profesor);
        System.out.println(
                "[SUCCESS] [PROFESOR] Edición de profesor finalizada para ID: " + updatedProfesor.getId());
        return updatedProfesor;
    }

    // Genera una cadena de contraseña temporal única.
    public String generadorRandomPassword() {
        return "temporal-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Autowired
    private com.harmony.sistema.repository.HorarioRepository horarioRepository;

    @Autowired
    private com.harmony.sistema.repository.ClaseCanceladaRepository claseCanceladaRepository;

    public com.harmony.sistema.model.ClaseCancelada cancelarClase(Long horarioId,
            com.harmony.sistema.model.ClaseCancelada claseCancelada) {
        System.out.println("[INFO] [PROFESOR] Cancelando clase para horario ID: " + horarioId);

        com.harmony.sistema.model.Horario horario = horarioRepository.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        claseCancelada.setHorario(horario);
        com.harmony.sistema.model.ClaseCancelada saved = claseCanceladaRepository.save(claseCancelada);

        // Simulación de envío de correo al admin
        System.out.println("[INFO] [EMAIL] Enviando correo al ADMIN: El profesor " +
                horario.getProfesor().getNombreCompleto() + " ha cancelado una clase del taller " +
                horario.getTaller().getNombre() + " para la fecha " + claseCancelada.getFecha() +
                ". Motivo: " + claseCancelada.getMotivo());

        return saved;
    }
}
