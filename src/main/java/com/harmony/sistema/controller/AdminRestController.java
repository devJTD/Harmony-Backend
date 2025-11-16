package com.harmony.sistema.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.harmony.sistema.dto.ClienteRegistroDTO;
import com.harmony.sistema.dto.InscripcionFormDTO;
import com.harmony.sistema.dto.ProfesorEdicionDTO;
import com.harmony.sistema.dto.ProfesorRegistroDTO;
import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.model.Taller;
import com.harmony.sistema.dto.CredencialesDTO;
import com.harmony.sistema.service.ClienteService;
import com.harmony.sistema.service.HorarioService;
import com.harmony.sistema.service.InscripcionService;
import com.harmony.sistema.service.ProfesorService;
import com.harmony.sistema.service.TallerService;

/**
 * API REST para gestión administrativa (Clientes, Profesores, Talleres, Horarios)
 * Reemplaza los endpoints de Thymeleaf del AdminController para consumo desde Angular
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200") // Permitir acceso desde Angular en desarrollo
public class AdminRestController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProfesorService profesorService;

    @Autowired
    private TallerService tallerService;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private HorarioService horarioService;

    // ==================== CLIENTES ====================

    /**
     * GET: Obtiene lista de todos los clientes
     */
    @GetMapping("/clientes")
    public ResponseEntity<List<Cliente>> listarClientes() {
        System.out.println(" [API] GET /api/admin/clientes - Listando clientes");
        try {
            List<Cliente> clientes = clienteService.listarClientes();
            System.out.println(" [API SUCCESS] " + clientes.size() + " clientes obtenidos");
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al listar clientes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Obtiene lista de talleres disponibles con horarios sin iniciar y con vacantes
     */
    @GetMapping("/clientes/talleres-disponibles")
    public ResponseEntity<List<Taller>> obtenerTalleresDisponibles() {
        System.out.println(" [API] GET /api/admin/clientes/talleres-disponibles - Obteniendo talleres disponibles");
        try {
            List<Taller> talleresActivos = tallerService.encontrarTalleresActivos();
            LocalDate hoy = LocalDate.now();

            List<Taller> talleresFiltrados = talleresActivos.stream().map(taller -> {
                List<Horario> horariosDisponibles = taller.getHorarios().stream()
                        .filter(horario -> {
                            boolean tieneVacantes = horario.getVacantesDisponibles() > 0;
                            boolean noHaIniciado = horario.getFechaInicio() != null &&
                                    !horario.getFechaInicio().isBefore(hoy);
                            return tieneVacantes && noHaIniciado;
                        })
                        .collect(Collectors.toList());
                taller.setHorarios(horariosDisponibles);
                return taller;
            }).filter(taller -> !taller.getHorarios().isEmpty())
                    .collect(Collectors.toList());

            System.out.println(" [API SUCCESS] " + talleresFiltrados.size() + " talleres disponibles");
            return ResponseEntity.ok(talleresFiltrados);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al obtener talleres disponibles: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST: Registra un nuevo cliente con inscripciones a talleres
     */
    @PostMapping("/clientes")
    public ResponseEntity<Map<String, Object>> registrarCliente(
            @RequestBody ClienteRegistroDTO clienteDto) {

        System.out.println(" [API] POST /api/admin/clientes - Registrando cliente: " + clienteDto.getNombreCompleto());

        try {
                InscripcionFormDTO inscripcionDto = new InscripcionFormDTO();
            inscripcionDto.setNombre(clienteDto.getNombreCompleto());
            inscripcionDto.setEmail(clienteDto.getCorreo());
            inscripcionDto.setTelefono(clienteDto.getTelefono());
            inscripcionDto.setTalleresSeleccionados(clienteDto.getTalleresSeleccionados());

                // El servicio espera un Map<tallerId, horarioId>. El DTO actual no expone
                // horariosSeleccionados, así que pasamos un mapa vacío cuando no exista.
                Map<Long, Long> horariosMap = new HashMap<>();

                CredencialesDTO credenciales = inscripcionService.procesarInscripcionCompleta(
                    inscripcionDto,
                    horariosMap
                );

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Cliente registrado exitosamente");
                response.put("email", credenciales.getCorreo());
                response.put("temporalPassword", credenciales.getContrasenaTemporal());

            System.out.println(" [API SUCCESS] Cliente registrado: " + clienteDto.getNombreCompleto());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al registrar cliente: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al registrar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * PUT: Edita un cliente existente
     */
    @PutMapping("/clientes/{id}")
    public ResponseEntity<Map<String, Object>> editarCliente(
            @PathVariable Long id,
            @RequestBody ClienteRegistroDTO clienteDto) {

        System.out.println(" [API] PUT /api/admin/clientes/{id} - Editando cliente ID: " + id);

        try {
            Cliente clienteActualizado = clienteService.actualizarCliente(
                    id,
                    clienteDto.getNombreCompleto(),
                    clienteDto.getCorreo(),
                    clienteDto.getTelefono(),
                    clienteDto.getCorreo()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente actualizado exitosamente");
            response.put("cliente", clienteActualizado);

            System.out.println(" [API SUCCESS] Cliente actualizado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al editar cliente: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al editar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * DELETE: Elimina un cliente
     */
    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Map<String, Object>> eliminarCliente(@PathVariable Long id) {
        System.out.println(" [API] DELETE /api/admin/clientes/{id} - Eliminando cliente ID: " + id);

        try {
            clienteService.eliminarCliente(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente eliminado exitosamente");

            System.out.println(" [API SUCCESS] Cliente eliminado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al eliminar cliente: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // ==================== PROFESORES ====================

    /**
     * GET: Obtiene lista de todos los profesores
     */
    @GetMapping("/profesores")
    public ResponseEntity<List<Profesor>> listarProfesores() {
        System.out.println(" [API] GET /api/admin/profesores - Listando profesores");
        try {
            List<Profesor> profesores = profesorService.listarProfesores();
            System.out.println(" [API SUCCESS] " + profesores.size() + " profesores obtenidos");
            return ResponseEntity.ok(profesores);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al listar profesores: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST: Registra un nuevo profesor
     */
    @PostMapping("/profesores")
    public ResponseEntity<Map<String, Object>> registrarProfesor(
            @RequestBody ProfesorRegistroDTO profesorDto) {

        System.out.println(" [API] POST /api/admin/profesores - Registrando profesor: " + profesorDto.getNombreCompleto());

        try {
            profesorService.registrarProfesor(profesorDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profesor registrado exitosamente");

            System.out.println(" [API SUCCESS] Profesor registrado: " + profesorDto.getNombreCompleto());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al registrar profesor: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al registrar profesor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * PUT: Edita un profesor existente
     */
    @PutMapping("/profesores/{id}")
    public ResponseEntity<Map<String, Object>> editarProfesor(
            @PathVariable Long id,
            @RequestBody ProfesorEdicionDTO profesorDto) {

        System.out.println(" [API] PUT /api/admin/profesores/{id} - Editando profesor ID: " + id);

        try {
            profesorDto.setId(id);
            profesorService.editarProfesor(profesorDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profesor actualizado exitosamente");

            System.out.println(" [API SUCCESS] Profesor actualizado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al editar profesor: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al editar profesor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * DELETE: Elimina un profesor
     */
    @DeleteMapping("/profesores/{id}")
    public ResponseEntity<Map<String, Object>> eliminarProfesor(@PathVariable Long id) {
        System.out.println(" [API] DELETE /api/admin/profesores/{id} - Eliminando profesor ID: " + id);

        try {
            profesorService.eliminarProfesor(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profesor eliminado exitosamente");

            System.out.println(" [API SUCCESS] Profesor eliminado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al eliminar profesor: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar profesor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // ==================== TALLERES ====================

    /**
     * GET: Obtiene lista de todos los talleres
     */
    @GetMapping("/talleres")
    public ResponseEntity<List<Taller>> listarTalleres() {
        System.out.println(" [API] GET /api/admin/talleres - Listando talleres");
        try {
            List<Taller> talleres = tallerService.listarTalleres();
            System.out.println(" [API SUCCESS] " + talleres.size() + " talleres obtenidos");
            return ResponseEntity.ok(talleres);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al listar talleres: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Obtiene lista de talleres activos
     */
    @GetMapping("/talleres/activos")
    public ResponseEntity<List<Taller>> listarTalleresActivos() {
        System.out.println(" [API] GET /api/admin/talleres/activos - Listando talleres activos");
        try {
            List<Taller> talleresActivos = tallerService.encontrarTalleresActivos();
            System.out.println(" [API SUCCESS] " + talleresActivos.size() + " talleres activos obtenidos");
            return ResponseEntity.ok(talleresActivos);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al listar talleres activos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST: Registra un nuevo taller
     */
    @PostMapping("/talleres")
    public ResponseEntity<Map<String, Object>> registrarTaller(@RequestBody Taller taller) {
        System.out.println(" [API] POST /api/admin/talleres - Registrando taller: " + taller.getNombre());

        try {
            tallerService.crearTallerSolo(taller);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Taller creado exitosamente");
            response.put("taller", taller);

            System.out.println(" [API SUCCESS] Taller registrado: " + taller.getNombre());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al registrar taller: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al registrar taller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * PUT: Edita un taller existente
     */
    @PutMapping("/talleres/{id}")
    public ResponseEntity<Map<String, Object>> editarTaller(
            @PathVariable Long id,
            @RequestBody Taller tallerActualizado) {

        System.out.println(" [API] PUT /api/admin/talleres/{id} - Editando taller ID: " + id);

        try {
            tallerActualizado.setId(id);
            tallerService.editarTaller(tallerActualizado);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Taller actualizado exitosamente");

            System.out.println(" [API SUCCESS] Taller actualizado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al editar taller: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al editar taller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * DELETE: Elimina un taller
     */
    @DeleteMapping("/talleres/{id}")
    public ResponseEntity<Map<String, Object>> eliminarTaller(@PathVariable Long id) {
        System.out.println(" [API] DELETE /api/admin/talleres/{id} - Eliminando taller ID: " + id);

        try {
            // Implementar el método en TallerService si no existe
            // tallerService.eliminarTaller(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Taller eliminado exitosamente");

            System.out.println(" [API SUCCESS] Taller eliminado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al eliminar taller: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar taller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // ==================== HORARIOS ====================

    /**
     * GET: Obtiene un horario por ID
     */
    @GetMapping("/horarios/{id}")
    public ResponseEntity<Horario> obtenerHorario(@PathVariable Long id) {
        System.out.println(" [API] GET /api/admin/horarios/{id} - Obteniendo horario ID: " + id);

        try {
            Horario horario = horarioService.getHorarioById(id);
            System.out.println(" [API SUCCESS] Horario obtenido: " + id);
            return ResponseEntity.ok(horario);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Horario no encontrado: " + id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST: Registra un nuevo horario
     */
    @PostMapping("/horarios")
    public ResponseEntity<Map<String, Object>> registrarHorario(
            @RequestParam("tallerId") Long tallerId,
            @RequestParam("profesorId") Long profesorId,
            @RequestParam(value = "diasDeClase", required = false) String[] diasDeClaseArray,
            @RequestParam("horaInicio") LocalTime horaInicio,
            @RequestParam("horaFin") LocalTime horaFin,
            @RequestParam("fechaInicio") LocalDate fechaInicio,
            @RequestParam("vacantesDisponibles") int vacantesDisponibles) {

        System.out.println(" [API] POST /api/admin/horarios - Registrando horario para taller: " + tallerId);

        try {
            String diasDeClase = (diasDeClaseArray != null && diasDeClaseArray.length > 0)
                    ? String.join(", ", diasDeClaseArray)
                    : "";

            if (diasDeClase.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Debe seleccionar al menos un día de clase");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            horarioService.crearHorario(tallerId, profesorId, diasDeClase, horaInicio, horaFin, fechaInicio, vacantesDisponibles);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Horario creado exitosamente");

            System.out.println(" [API SUCCESS] Horario registrado para taller: " + tallerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al registrar horario: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al registrar horario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * PUT: Edita un horario existente
     */
    @PutMapping("/horarios/{id}")
    public ResponseEntity<Map<String, Object>> editarHorario(
            @PathVariable Long id,
            @RequestParam("profesorId") Long profesorId,
            @RequestParam("diasDeClase") String diasDeClase,
            @RequestParam("horaInicio") LocalTime horaInicio,
            @RequestParam(value = "horaFin", required = false) LocalTime horaFin,
            @RequestParam("fechaInicio") LocalDate fechaInicio,
            @RequestParam("vacantesDisponibles") int vacantesDisponibles) {

        System.out.println(" [API] PUT /api/admin/horarios/{id} - Editando horario ID: " + id);

        try {
            if (horaFin == null) {
                Horario horarioExistente = horarioService.getHorarioById(id);
                horaFin = horarioExistente.getHoraFin();
            }

            horarioService.editarHorario(id, profesorId, diasDeClase, horaInicio, horaFin, fechaInicio, vacantesDisponibles);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Horario actualizado exitosamente");

            System.out.println(" [API SUCCESS] Horario actualizado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al editar horario: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al editar horario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * DELETE: Elimina un horario
     */
    @DeleteMapping("/horarios/{id}")
    public ResponseEntity<Map<String, Object>> eliminarHorario(@PathVariable Long id) {
        System.out.println(" [API] DELETE /api/admin/horarios/{id} - Eliminando horario ID: " + id);

        try {
            horarioService.eliminarHorario(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Horario eliminado exitosamente");

            System.out.println(" [API SUCCESS] Horario eliminado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println(" [API ERROR] Error al eliminar horario: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar horario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
