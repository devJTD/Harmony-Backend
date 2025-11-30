package com.harmony.sistema.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.model.Role;
import com.harmony.sistema.model.Taller;
import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.HorarioRepository;
import com.harmony.sistema.repository.ProfesorRepository;
import com.harmony.sistema.repository.RoleRepository;
import com.harmony.sistema.repository.TallerRepository;
import com.harmony.sistema.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

        private static final String ROLE_ADMIN = "ROLE_ADMIN";
        private static final String ROLE_CLIENTE = "ROLE_CLIENTE";
        private static final String ROLE_PROFESOR = "ROLE_PROFESOR";
        private static final int VACANTES_DEFAULT = 10;

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final TallerRepository tallerRepository;
        private final ProfesorRepository profesorRepository;
        private final HorarioRepository horarioRepository;
        private final PasswordEncoder passwordEncoder;

        public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
                        TallerRepository tallerRepository, ProfesorRepository profesorRepository,
                        HorarioRepository horarioRepository, PasswordEncoder passwordEncoder) {
                this.userRepository = userRepository;
                this.roleRepository = roleRepository;
                this.tallerRepository = tallerRepository;
                this.profesorRepository = profesorRepository;
                this.horarioRepository = horarioRepository;
                this.passwordEncoder = passwordEncoder;
        }

        // Ejecuta la inicialización de datos al arrancar la aplicación
        @Override
        @Transactional
        public void run(String... args) throws Exception {
                System.out.println("--- INICIANDO CONFIGURACIÓN INICIAL DE HARMONY ---");

                // Inicializa roles y usuario administrador
                Role adminRole = initializeRole(ROLE_ADMIN);
                Role profesorRole = initializeRole(ROLE_PROFESOR);
                initializeRole(ROLE_CLIENTE);

                initializeAdminUser(adminRole);

                // 3. Inicializa los profesores.
                Profesor juanPerez = initializeProfesor("Juan Perez", "987654321", profesorRole,
                                "/images/profesorPiano.jpg",
                                "\"La música es el lenguaje universal que conecta corazones. Enseñar piano no es solo transmitir técnica, es despertar la pasión por crear melodías que expresan lo que las palabras no pueden. Cada alumno es único y mi misión es ayudarles a descubrir su propia voz musical.\"");

                Profesor pedroSanchez = initializeProfesor("Pedro Sanchez", "987654322", profesorRole,
                                "/images/profesorGuitarra.jpg",
                                "\"La guitarra fue mi refugio en momentos difíciles y quiero que mis alumnos experimenten ese mismo poder transformador. Me apasiona ver cómo cada estudiante encuentra su estilo propio y se emociona al tocar su primera canción completa. Enseñar es compartir esa magia.\"");

                Profesor sofiaLopez = initializeProfesor("Sofia Lopez", "987654323", profesorRole,
                                "/images/profesorViolin.jpg",
                                "\"El violín exige disciplina, pero también sensibilidad. Me fascina guiar a mis alumnos en ese balance perfecto entre técnica y emoción. Ver cómo dominan un pasaje difícil o logran expresar sentimientos a través del arco es lo que me motiva cada día a ser mejor profesora.\"");

                Profesor jeremyAgurto = initializeProfesor("Jeremy Agurto", "987654324", profesorRole,
                                "/images/profesorFlauta.jpg",
                                "\"La flauta tiene una dulzura especial que llega al alma. Me apasiona enseñar este instrumento porque permite expresar emociones con una delicadeza única. Ver a mis alumnos disfrutar del proceso y conquistar cada melodía es mi mayor satisfacción como profesor.\"");

                System.out.println("Profesores inicializados/verificados.");

                // 4. Inicializa los talleres.
                Taller piano = initializeTaller(
                                "Piano",
                                "Aprende las bases del piano desde cero en un curso diseñado para principiantes que desean familiarizarse con el instrumento y desarrollar coordinación, lectura musical y ejecución de melodías sencillas.",
                                12,
                                2,
                                "/images/tallerPiano.jpg",
                                "/images/piano.jpg",
                                "Postura y digitación, Lectura de notas y ritmos básicos, Acordes mayores y menores, Interpretación de canciones sencillas, Escalas principales y arpegios.",
                                new BigDecimal("500.00"));

                Taller guitarra = initializeTaller(
                                "Guitarra",
                                "Descubre los fundamentos de la guitarra acústica o eléctrica en un taller pensado para principiantes que buscan aprender acordes básicos, rasgueo, afinación y sus primeras canciones.",
                                16,
                                3,
                                "/images/tallerGuitarra.jpg",
                                "/images/guitarra.jpg",
                                "Afinación y postura, Acordes básicos (mayores/menores/séptima), Técnicas de rasgueo y ritmo, Lectura de tablaturas, Progresiones de acordes, Repertorio popular.",
                                new BigDecimal("450.00"));

                Taller violin = initializeTaller(
                                "Violín",
                                "Iníciate en el mundo del violín aprendiendo la postura correcta, el uso del arco, la afinación y la ejecución de escalas y melodías simples ideales para principiantes.",
                                20,
                                2,
                                "/images/tallerViolin.jpg",
                                "/images/violin.jpg",
                                "Nociones básicas y agarre del arco, Posición del violín y dedos, Producción de sonido, Primeras escalas y melodías, Ejercicios de ritmo y afinación.",
                                new BigDecimal("600.00"));

                Taller flauta = initializeTaller(
                                "Flauta",
                                "Comienza tu formación musical con la flauta aprendiendo respiración, digitación y lectura básica de partituras en un curso ideal para quienes tocan por primera vez.",
                                12,
                                3,
                                "/images/tallerFlauta.jpg",
                                "/images/flauta.jpg",
                                "Embocadura correcta y respiración, Digitaciones de notas, Lectura de partituras, Escalas mayores, Interpretación de piezas sencillas.",
                                new BigDecimal("350.00"));

                System.out.println("Talleres inicializados/verificados.");

                // 5. Inicializa los horarios para los talleres, algunos ya iniciados (minus) y
                // otros por iniciar (plus).
                LocalDate hoy = LocalDate.now();

                initializeHorario(piano, juanPerez, "Lunes, Miércoles", "16:00", "18:00", VACANTES_DEFAULT,
                                hoy.minusWeeks(2));
                initializeHorario(piano, juanPerez, "Martes, Jueves", "09:00", "11:00", VACANTES_DEFAULT,
                                hoy.plusDays(5));
                initializeHorario(piano, juanPerez, "Miércoles, Viernes", "18:00", "20:00", VACANTES_DEFAULT,
                                hoy.plusDays(20));

                initializeHorario(guitarra, pedroSanchez, "Lunes, Miércoles, Viernes", "17:00", "19:00",
                                VACANTES_DEFAULT,
                                hoy.plusDays(15));
                initializeHorario(guitarra, pedroSanchez, "Martes, Jueves, Sábados", "10:00", "12:00", VACANTES_DEFAULT,
                                hoy.plusDays(8));

                initializeHorario(violin, sofiaLopez, "Martes, Jueves", "16:30", "18:30", VACANTES_DEFAULT,
                                hoy.minusDays(5));
                initializeHorario(violin, sofiaLopez, "Lunes, Miércoles", "19:00", "21:00", VACANTES_DEFAULT,
                                hoy.plusMonths(1));
                initializeHorario(violin, sofiaLopez, "Sábados", "09:00", "13:00", VACANTES_DEFAULT, hoy.plusDays(3));

                initializeHorario(flauta, jeremyAgurto, "Lunes, Miércoles, Viernes", "15:00", "17:00", VACANTES_DEFAULT,
                                hoy.minusDays(7));
                initializeHorario(flauta, jeremyAgurto, "Martes, Jueves, Sábados", "18:00", "20:00", VACANTES_DEFAULT,
                                hoy.minusDays(14));

                System.out.println("--- CONFIGURACIÓN INICIAL DE HARMONY FINALIZADA ---");
        }

        // Busca un rol por nombre o lo crea si no existe
        private Role initializeRole(String roleName) {
                return roleRepository.findByName(roleName)
                                .orElseGet(() -> {
                                        Role newRole = Role.builder().name(roleName).build();
                                        System.out.println(" [DATA] Inicializando Role: " + roleName);
                                        return roleRepository.save(newRole);
                                });
        }

        // Crea el usuario admin si no existe
        private void initializeAdminUser(Role adminRole) {
                if (!userRepository.findByEmail("admin@harmony.com").isPresent()) {
                        Set<Role> adminRoles = new HashSet<>();
                        adminRoles.add(adminRole);
                        User adminUser = User.builder()
                                        .email("admin@harmony.com")
                                        .password(passwordEncoder.encode("adminPassword"))
                                        .enabled(true)
                                        .roles(adminRoles)
                                        .build();
                        userRepository.save(adminUser);
                        System.out.println(" [DATA] Inicializando Usuario Admin.");
                }
        }

        // Inicializa un profesor y su usuario asociado si no existen
        private Profesor initializeProfesor(String nombre, String telefono, Role profesorRole, String fotoUrl,
                        String informacion) {
                Optional<Profesor> existingProfesor = profesorRepository.findByNombreCompleto(nombre);
                if (existingProfesor.isPresent()) {
                        return existingProfesor.get();
                }

                // Crea usuario para el profesor
                User user = User.builder()
                                .email(nombre.toLowerCase().replace(" ", ".") + "@harmony.com")
                                .password(passwordEncoder.encode("password123"))
                                .enabled(true)
                                .roles(Set.of(profesorRole))
                                .build();
                userRepository.save(user);

                // Crea entidad profesor vinculada al usuario
                Profesor profesor = Profesor.builder()
                                .nombreCompleto(nombre)
                                .telefono(telefono)
                                .fotoUrl(fotoUrl)
                                .informacion(informacion)
                                .user(user)
                                .build();
                System.out.println(" [DATA] Inicializando Profesor: " + nombre);
                return profesorRepository.save(profesor);
        }

        // Busca un taller por nombre o lo crea si no existe
        private Taller initializeTaller(String nombre, String descripcion, Integer duracionSemanas,
                        Integer clasesPorSemana,
                        String imagenTaller, String imagenInicio, String temas, BigDecimal precio) {
                return tallerRepository.findByNombre(nombre)
                                .orElseGet(() -> {
                                        Taller newTaller = Taller.builder()
                                                        .nombre(nombre)
                                                        .descripcion(descripcion)
                                                        .duracionSemanas(duracionSemanas)
                                                        .clasesPorSemana(clasesPorSemana)
                                                        .imagenTaller(imagenTaller)
                                                        .imagenInicio(imagenInicio)
                                                        .activo(true)
                                                        .precio(precio)
                                                        .temas(temas)
                                                        .build();
                                        System.out.println(" [DATA] Inicializando Taller: " + nombre + " con precio S/ "
                                                        + precio);
                                        return tallerRepository.save(newTaller);
                                });
        }

        // Inicializa un horario si no existe conflicto
        private Horario initializeHorario(Taller taller, Profesor profesor, String dias, String horaInicio,
                        String horaFin,
                        int vacantes, LocalDate fechaInicio) {
                LocalTime inicio = LocalTime.parse(horaInicio);
                LocalTime fin = LocalTime.parse(horaFin);

                // Verifica si ya existe el horario
                Optional<Horario> existingHorario = horarioRepository
                                .findByTallerAndProfesorAndDiasDeClaseAndHoraInicioAndHoraFin(
                                                taller, profesor, dias, inicio, fin);

                if (existingHorario.isPresent()) {
                        System.out.println(" [DATA - SKIP] Horario ya existe para " + taller.getNombre() + " (Prof. "
                                        + profesor.getNombreCompleto() + "): " + dias + " de " + horaInicio + " a "
                                        + horaFin + ". Saltando inicialización.");
                        return existingHorario.get();
                }

                // Crea y guarda el nuevo horario
                long diasDuracion = (long) taller.getDuracionSemanas() * 7;
                LocalDate fechaFinCalculada = fechaInicio.plusDays(diasDuracion);

                Horario newHorario = Horario.builder()
                                .taller(taller)
                                .profesor(profesor)
                                .diasDeClase(dias)
                                .horaInicio(inicio)
                                .horaFin(fin)
                                .fechaInicio(fechaInicio)
                                .fechaFin(fechaFinCalculada)
                                .finalizado(false)
                                .vacantesDisponibles(vacantes)
                                .build();

                System.out.println(" [DATA] Inicializando Nuevo Horario para " + taller.getNombre() + " (Prof. "
                                + profesor.getNombreCompleto() + "): " + dias + " de " + horaInicio + " a " + horaFin
                                + ". Inicia: " + fechaInicio);
                return horarioRepository.save(newHorario);
        }
}
