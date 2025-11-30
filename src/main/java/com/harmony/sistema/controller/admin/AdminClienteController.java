package com.harmony.sistema.controller.admin;

import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.RestController;

import com.harmony.sistema.dto.ClienteRegistroDTO;
import com.harmony.sistema.dto.CredencialesDTO;
import com.harmony.sistema.dto.InscripcionFormDTO;
import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Taller;
import com.harmony.sistema.service.ClienteService;
import com.harmony.sistema.service.InscripcionService;
import com.harmony.sistema.service.TallerService;

/**
 * Controller REST para gestión de clientes en el panel de administración.
 * Maneja operaciones CRUD de clientes e inscripciones.
 */
@RestController
@RequestMapping("/api/admin/clientes")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminClienteController {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private TallerService tallerService;

    /**
     * Endpoint para listar todos los clientes con sus inscripciones activas.
     * GET /api/admin/clientes
     */
    @GetMapping
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<List<Map<String, Object>>> listarClientes() {
        System.out.println("[INFO] [CONTROLLER] GET /api/admin/clientes - Listando clientes con inscripciones");
        try {
            List<Cliente> clientes = clienteService.listarClientes();

            List<Map<String, Object>> clientesConInscripciones = clientes.stream().map(cliente -> {
                Map<String, Object> clienteMap = new HashMap<>();
                clienteMap.put("id", cliente.getId());
                clienteMap.put("nombreCompleto", cliente.getNombreCompleto());
                clienteMap.put("correo", cliente.getCorreo());
                clienteMap.put("telefono", cliente.getTelefono());

                String correoUser = cliente.getUser() != null ? cliente.getUser().getEmail() : cliente.getCorreo();
                Map<String, String> userMap = new HashMap<>();
                userMap.put("email", correoUser);
                clienteMap.put("user", userMap);

                List<Map<String, Object>> inscripcionesList = cliente.getInscripciones().stream()
                        .filter(inscripcion -> !Boolean.TRUE.equals(inscripcion.getHorario().getFinalizado()))
                        .map(inscripcion -> {
                            Map<String, Object> inscripcionMap = new HashMap<>();
                            Map<String, Object> horarioMap = new HashMap<>();
                            Horario horario = inscripcion.getHorario();

                            horarioMap.put("id", horario.getId());
                            horarioMap.put("diasDeClase", horario.getDiasDeClase());
                            horarioMap.put("horaInicio", horario.getHoraInicio().toString());
                            horarioMap.put("horaFin", horario.getHoraFin().toString());

                            Map<String, Object> tallerMap = new HashMap<>();
                            Taller taller = horario.getTaller();
                            tallerMap.put("id", taller.getId());
                            tallerMap.put("nombre", taller.getNombre());
                            horarioMap.put("taller", tallerMap);

                            inscripcionMap.put("horario", horarioMap);
                            return inscripcionMap;
                        })
                        .collect(Collectors.toList());

                clienteMap.put("inscripciones", inscripcionesList);

                System.out.println("[INFO] [CONTROLLER] Cliente ID " + cliente.getId() +
                        " - Correo: " + correoUser +
                        " - Inscripciones: " + inscripcionesList.size());

                return clienteMap;
            }).collect(Collectors.toList());

            System.out.println("[SUCCESS] [CONTROLLER] " + clientesConInscripciones.size()
                    + " clientes obtenidos con inscripciones");
            return ResponseEntity.ok(clientesConInscripciones);
        } catch (Exception e) {
            System.err.println("[ERROR] [CONTROLLER] Error al listar clientes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para obtener talleres con horarios disponibles (no iniciados y con
     * vacantes).
     * GET /api/admin/clientes/talleres-disponibles
     */
    @GetMapping("/talleres-disponibles")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<List<Taller>> obtenerTalleresDisponibles() {
        System.out.println("[INFO] [CONTROLLER] GET /api/admin/clientes/talleres-disponibles");
        try {
            List<Taller> talleresActivos = tallerService.encontrarTalleresActivos();
            LocalDate hoy = LocalDate.now();
            System.out.println("📅 [CONTROLLER] Fecha actual: " + hoy);

            List<Taller> talleresFiltrados = talleresActivos.stream().map(taller -> {
                List<Horario> horariosDisponibles = taller.getHorarios().stream()
                        .filter(horario -> {
                            boolean tieneVacantes = horario.getVacantesDisponibles() > 0;
                            boolean noHaIniciado = horario.getFechaInicio() != null
                                    && !horario.getFechaInicio().isBefore(hoy);

                            System.out.println("[INFO] [CONTROLLER] Horario ID " + horario.getId() + " - Vacantes: "
                                    + horario.getVacantesDisponibles() + ", Fecha inicio: " + horario.getFechaInicio()
                                    + ", No ha iniciado: " + noHaIniciado);

                            return tieneVacantes && noHaIniciado;
                        })
                        .collect(Collectors.toList());
                taller.setHorarios(horariosDisponibles);
                return taller;
            }).filter(taller -> !taller.getHorarios().isEmpty())
                    .collect(Collectors.toList());

            System.out.println("[SUCCESS] [CONTROLLER] " + talleresFiltrados.size() + " talleres disponibles");
            return ResponseEntity.ok(talleresFiltrados);
        } catch (Exception e) {
            System.err.println("[ERROR] [CONTROLLER] Error al obtener talleres disponibles: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para registrar un nuevo cliente con inscripciones a talleres.
     * POST /api/admin/clientes
     */
    @PostMapping
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> registrarCliente(@RequestBody ClienteRegistroDTO clienteDto) {

        System.out.println(
                "🔵 [API ADMIN] POST /api/admin/clientes - Registrando cliente: " + clienteDto.getNombreCompleto());
        System.out.println("[INFO] [CONTROLLER] Email: " + clienteDto.getCorreo());
        System.out.println("[INFO] [CONTROLLER] Teléfono: " + clienteDto.getTelefono());
        System.out.println("[INFO] [CONTROLLER] Talleres seleccionados: " + clienteDto.getTalleresSeleccionados());

        try {
            InscripcionFormDTO inscripcionDto = new InscripcionFormDTO();
            inscripcionDto.setNombre(clienteDto.getNombreCompleto());
            inscripcionDto.setEmail(clienteDto.getCorreo());
            inscripcionDto.setTelefono(clienteDto.getTelefono());

            Map<Long, Long> horariosMap = clienteDto.getTalleresSeleccionados();

            if (horariosMap != null && !horariosMap.isEmpty()) {
                horariosMap.forEach((tallerId, horarioId) -> {
                    System.out.println("[INFO] [CONTROLLER] Taller ID " + tallerId + " -> Horario ID " + horarioId);
                });
            }

            System.out.println("[INFO] [CONTROLLER] Llamando a inscripcionService.procesarInscripcionCompleta");
            CredencialesDTO credenciales = inscripcionService.procesarInscripcionCompleta(
                    inscripcionDto,
                    horariosMap != null ? horariosMap : new HashMap<>());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente registrado exitosamente");
            response.put("email", credenciales.getCorreo());
            response.put("temporalPassword", credenciales.getContrasenaTemporal());

            System.out.println("[SUCCESS] [CONTROLLER] Cliente registrado: " + clienteDto.getNombreCompleto());
            System.out.println("[INFO] [CONTROLLER] Contraseña temporal: " + credenciales.getContrasenaTemporal());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.err.println("[ERROR] [CONTROLLER] Error al registrar cliente: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al registrar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint para editar un cliente existente.
     * PUT /api/admin/clientes/{id}
     */
    @PutMapping("/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> editarCliente(
            @PathVariable Long id,
            @RequestBody ClienteRegistroDTO clienteDto) {

        System.out.println("[INFO] [CONTROLLER] PUT /api/admin/clientes/" + id + " - Editando cliente");
        System.out.println("[INFO] [CONTROLLER] Datos nuevos - Nombre: " + clienteDto.getNombreCompleto());
        System.out.println("[INFO] [CONTROLLER] Email: " + clienteDto.getCorreo());
        System.out.println("[INFO] [CONTROLLER] Teléfono: " + clienteDto.getTelefono());

        try {
            Cliente clienteOriginal = clienteService.listarClientes().stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            String correoOriginal = clienteOriginal.getUser() != null ? clienteOriginal.getUser().getEmail()
                    : clienteOriginal.getCorreo();

            System.out.println("[INFO] [CONTROLLER] Correo original: " + correoOriginal);

            Cliente clienteActualizado = clienteService.actualizarCliente(
                    id,
                    clienteDto.getNombreCompleto(),
                    clienteDto.getCorreo(),
                    clienteDto.getTelefono(),
                    correoOriginal);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente actualizado exitosamente");
            response.put("cliente", clienteActualizado);

            System.out.println("[SUCCESS] [CONTROLLER] Cliente actualizado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[ERROR] [CONTROLLER] Error al editar cliente: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al editar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint para eliminar un cliente.
     * DELETE /api/admin/clientes/{id}
     */
    @DeleteMapping("/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> eliminarCliente(@PathVariable Long id) {
        System.out.println("[INFO] [CONTROLLER] DELETE /api/admin/clientes/" + id + " - Eliminando cliente");

        try {
            clienteService.eliminarCliente(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente eliminado exitosamente");

            System.out.println("[SUCCESS] [CONTROLLER] Cliente eliminado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[ERROR] [CONTROLLER] Error al eliminar cliente: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar cliente: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint para agregar una inscripción a un cliente existente.
     * POST /api/admin/clientes/{id}/inscripciones
     */
    @PostMapping("/{id}/inscripciones")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> agregarInscripcion(
            @PathVariable Long id,
            @RequestBody Map<String, Long> payload) {

        System.out.println("[INFO] [CONTROLLER] POST /api/admin/clientes/" + id + "/inscripciones - Agregando inscripción");
        Long horarioId = payload.get("horarioId");
        System.out.println("📅 [CONTROLLER] Horario ID: " + horarioId);

        try {
            inscripcionService.inscribirClienteExistente(id, horarioId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Inscripción agregada exitosamente");

            System.out.println("[SUCCESS] [CONTROLLER] Inscripción agregada al cliente: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[ERROR] [CONTROLLER] Error al agregar inscripción: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al agregar inscripción: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint para eliminar una inscripción específica de un cliente.
     * DELETE /api/admin/clientes/{id}/inscripciones/{horarioId}
     */
    @DeleteMapping("/{id}/inscripciones/{horarioId}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> eliminarInscripcion(
            @PathVariable Long id,
            @PathVariable Long horarioId) {

        System.out.println("[INFO] [CONTROLLER] DELETE /api/admin/clientes/" + id + "/inscripciones/" + horarioId
                + " - Eliminando inscripción");

        try {
            inscripcionService.eliminarInscripcion(id, horarioId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Inscripción eliminada exitosamente");

            System.out.println("[SUCCESS] [CONTROLLER] Inscripción eliminada del cliente: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[ERROR] [CONTROLLER] Error al eliminar inscripción: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar inscripción: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
