package com.harmony.sistema.controller.publico;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harmony.sistema.dto.CredencialesDTO;
import com.harmony.sistema.dto.DatosPersonalesFormDTO;
import com.harmony.sistema.dto.InscripcionFormDTO;
import com.harmony.sistema.dto.InscripcionPayloadDTO;
import com.harmony.sistema.dto.InscripcionResponseDTO;
import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Inscripcion;
import com.harmony.sistema.model.Taller;
import com.harmony.sistema.service.InscripcionService;
import com.harmony.sistema.service.TallerService;

@RestController
@RequestMapping("/api/inscripcion")
@CrossOrigin(origins = "http://localhost:4200")
public class InscripcionRestController {

    @Autowired
    TallerService tallerService;
    @Autowired
    InscripcionService inscripcionService;

    /**
     * Endpoint para guardar datos personales y crear/obtener cliente.
     * POST /api/inscripcion/cliente
     */
    @PostMapping("/cliente")
    public ResponseEntity<Cliente> guardarDatosPersonales(@RequestBody DatosPersonalesFormDTO datos) {
        System.out.println(" [REST REQUEST] POST a /api/inscripcion/cliente. Creando cliente: " + datos.getNombre());
        try {
            Cliente clienteGuardado = inscripcionService.guardarOObtenerClienteTemporal(datos);
            System.out.println(" [REST SERVICE SUCCESS] Cliente creado/obtenido con ID: " + clienteGuardado.getId());
            return new ResponseEntity<>(clienteGuardado, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println(" [REST SERVICE ERROR] Fallo al guardar datos personales. Detalle: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint para obtener talleres disponibles.
     * GET /api/inscripcion/talleresDisponibles
     */
    @GetMapping("/talleresDisponibles")
    public ResponseEntity<List<Taller>> getTalleresDisponibles() {
        System.out
                .println(" [REST REQUEST] GET a /api/inscripcion/talleresDisponibles. Cargando talleres disponibles.");

        List<Taller> talleres = tallerService.encontrarTalleresActivos();
        LocalDate hoy = LocalDate.now();

        List<Taller> talleresFiltrados = talleres.stream().map((Taller taller) -> {
            if (taller.isActivo() && taller.getHorarios() != null) {
                List<Horario> horariosDisponibles = taller.getHorarios().stream()
                        .filter(horario -> {
                            boolean tieneVacantes = horario.getVacantesDisponibles() > 0;
                            boolean noHaIniciado = horario.getFechaInicio() != null &&
                                    !horario.getFechaInicio().isBefore(hoy);
                            return tieneVacantes && noHaIniciado;
                        })
                        .collect(Collectors.toList());
                taller.setHorarios(horariosDisponibles);
            }
            return taller;
        }).filter(taller -> taller.getHorarios() != null && !taller.getHorarios().isEmpty())
                .collect(Collectors.toList());

        return new ResponseEntity<>(talleresFiltrados, HttpStatus.OK);
    }

    /**
     * Endpoint para confirmar la inscripción.
     * POST /api/inscripcion/confirmar
     */
    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarInscripcion(@RequestBody InscripcionPayloadDTO payload) {
        System.out.println(" [REST REQUEST] POST a /api/inscripcion/confirmar. Procesando inscripción para: "
                + payload.getEmail());

        InscripcionFormDTO formDTO = new InscripcionFormDTO();
        formDTO.setNombre(payload.getNombre());
        formDTO.setEmail(payload.getEmail());
        formDTO.setTelefono(payload.getTelefono());

        Map<Long, Long> horariosSeleccionados = new HashMap<>();
        if (payload.getInscripciones() != null) {
            payload.getInscripciones().forEach(inscripcion -> {
                horariosSeleccionados.put(inscripcion.getTallerId(), inscripcion.getHorarioId());
            });
        }
        System.out.println(" [REST DATA] Inscripciones a procesar: " + horariosSeleccionados.size());
        if (horariosSeleccionados.isEmpty()) {
            System.out.println(" [REST WARNING] Se recibió una solicitud de inscripción sin talleres seleccionados.");
        }

        try {
            CredencialesDTO credenciales = inscripcionService.procesarInscripcionCompleta(formDTO,
                    horariosSeleccionados);
            System.out.println(" [REST SERVICE SUCCESS] Usuario creado con correo: " + credenciales.getCorreo());

            InscripcionResponseDTO response = new InscripcionResponseDTO(
                    credenciales.getCorreo(),
                    credenciales.getContrasenaTemporal());

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            System.err.println(" [REST SERVICE ERROR] Fallo al procesar la inscripción. Detalle: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Fallo en la inscripción.");
            errorResponse.put("mensaje", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint para solicitar baja de un taller.
     * POST /api/inscripcion/solicitar-baja
     */
    @PostMapping("/solicitar-baja")
    @SuppressWarnings("UseSpecificCatch")
    public ResponseEntity<Map<String, Object>> solicitarBaja(@RequestBody Map<String, Object> payload) {
        System.out.println(" [REST REQUEST] POST a /api/inscripcion/solicitar-baja");

        try {
            Long clienteId = Long.valueOf(payload.get("clienteId").toString());
            Long horarioId = Long.valueOf(payload.get("horarioId").toString());
            String motivo = (String) payload.get("motivo");

            inscripcionService.solicitarBaja(clienteId, horarioId, motivo);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Solicitud de baja enviada correctamente.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println(" [REST SERVICE ERROR] Fallo al solicitar baja: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al procesar la solicitud: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint para realizar una nueva inscripción.
     * POST /api/inscripcion/nueva
     */
    @PostMapping("/nueva")
    public ResponseEntity<Map<String, Object>> nuevaInscripcion(@RequestBody Map<String, Long> payload) {
        System.out.println(" [REST REQUEST] POST a /api/inscripcion/nueva");

        try {
            Long clienteId = payload.get("clienteId");
            Long horarioId = payload.get("horarioId");

            inscripcionService.inscribirClienteExistente(clienteId, horarioId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Inscripción realizada con éxito.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println(" [REST SERVICE ERROR] Fallo al realizar nueva inscripción: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al realizar la inscripción: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint para obtener inscripciones por cliente.
     * GET /api/inscripcion/cliente/{clienteId}
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Map<String, Object>>> obtenerInscripcionesPorCliente(@PathVariable Long clienteId) {
        System.out.println(" [REST REQUEST] GET a /api/inscripcion/cliente/" + clienteId);
        try {
            List<Inscripcion> inscripciones = inscripcionService.obtenerInscripcionesPorCliente(clienteId);

            List<Map<String, Object>> response = inscripciones.stream().map(inscripcion -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", inscripcion.getId());
                map.put("fechaInscripcion", inscripcion.getFechaInscripcion());
                map.put("pagado", inscripcion.isPagado());

                if (inscripcion.getHorario() != null) {
                    Map<String, Object> horarioMap = new HashMap<>();
                    horarioMap.put("id", inscripcion.getHorario().getId());
                    horarioMap.put("diasDeClase", inscripcion.getHorario().getDiasDeClase());
                    horarioMap.put("horaInicio", inscripcion.getHorario().getHoraInicio());
                    horarioMap.put("horaFin", inscripcion.getHorario().getHoraFin());

                    if (inscripcion.getHorario().getTaller() != null) {
                        Map<String, Object> tallerMap = new HashMap<>();
                        tallerMap.put("id", inscripcion.getHorario().getTaller().getId());
                        tallerMap.put("nombre", inscripcion.getHorario().getTaller().getNombre());
                        horarioMap.put("taller", tallerMap);
                    }

                    map.put("horario", horarioMap);
                }
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println(" [REST SERVICE ERROR] Error al obtener inscripciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
