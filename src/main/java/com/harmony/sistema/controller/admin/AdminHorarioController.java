package com.harmony.sistema.controller.admin;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.harmony.sistema.model.Horario;
import com.harmony.sistema.service.HorarioService;

/**
 * Controller REST para gestión de horarios en el panel de administración.
 * Maneja operaciones CRUD de horarios.
 */
@RestController
@RequestMapping("/api/admin/horarios")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminHorarioController {

    @Autowired
    private HorarioService horarioService;

    /**
     * Endpoint para obtener un horario por ID.
     * GET /api/admin/horarios/{id}
     */
    @GetMapping("/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Horario> obtenerHorario(@PathVariable Long id) {
        System.out.println("🔵 [API ADMIN] GET /api/admin/horarios/" + id + " - Obteniendo horario");

        try {
            Horario horario = horarioService.getHorarioById(id);
            System.out.println("✅ [API ADMIN SUCCESS] Horario obtenido: " + id);
            return ResponseEntity.ok(horario);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Horario no encontrado: " + id);
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint para registrar un nuevo horario.
     * POST /api/admin/horarios
     */
    @PostMapping
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> registrarHorario(
            @RequestParam("tallerId") Long tallerId,
            @RequestParam("profesorId") Long profesorId,
            @RequestParam(value = "diasDeClase", required = false) String[] diasDeClaseArray,
            @RequestParam("horaInicio") LocalTime horaInicio,
            @RequestParam("horaFin") LocalTime horaFin,
            @RequestParam("fechaInicio") LocalDate fechaInicio,
            @RequestParam("vacantesDisponibles") int vacantesDisponibles) {

        System.out.println("🔵 [API ADMIN] POST /api/admin/horarios - Registrando horario");
        System.out.println("🎓 [API ADMIN] Taller ID: " + tallerId);
        System.out.println("👨‍🏫 [API ADMIN] Profesor ID: " + profesorId);
        System.out.println("📅 [API ADMIN] Fecha inicio: " + fechaInicio);

        try {
            String diasDeClase = (diasDeClaseArray != null && diasDeClaseArray.length > 0)
                    ? String.join(", ", diasDeClaseArray)
                    : "";

            if (diasDeClase.isEmpty()) {
                System.out.println("⚠️ [API ADMIN] No se seleccionaron días de clase");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Debe seleccionar al menos un día de clase");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            Horario nuevoHorario = horarioService.crearHorario(tallerId, profesorId, diasDeClase,
                    horaInicio, horaFin, fechaInicio, vacantesDisponibles);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Horario creado exitosamente");
            response.put("horario", nuevoHorario);

            System.out.println("✅ [API ADMIN SUCCESS] Horario registrado con ID: " + nuevoHorario.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al registrar horario: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al registrar horario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint para editar un horario existente.
     * PUT /api/admin/horarios/{id}
     */
    @PutMapping("/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> editarHorario(
            @PathVariable Long id,
            @RequestParam("profesorId") Long profesorId,
            @RequestParam("diasDeClase") String diasDeClase,
            @RequestParam("horaInicio") LocalTime horaInicio,
            @RequestParam(value = "horaFin", required = false) LocalTime horaFin,
            @RequestParam("fechaInicio") LocalDate fechaInicio,
            @RequestParam("vacantesDisponibles") int vacantesDisponibles) {

        System.out.println("🔵 [API ADMIN] PUT /api/admin/horarios/" + id + " - Editando horario");

        try {
            if (horaFin == null) {
                Horario horarioExistente = horarioService.getHorarioById(id);
                horaFin = horarioExistente.getHoraFin();
            }

            Horario horarioActualizado = horarioService.editarHorario(id, profesorId, diasDeClase,
                    horaInicio, horaFin, fechaInicio, vacantesDisponibles);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Horario actualizado exitosamente");
            response.put("horario", horarioActualizado);

            System.out.println("✅ [API ADMIN SUCCESS] Horario actualizado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al editar horario: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al editar horario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint para eliminar un horario.
     * DELETE /api/admin/horarios/{id}
     */
    @DeleteMapping("/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> eliminarHorario(@PathVariable Long id) {
        System.out.println("🔵 [API ADMIN] DELETE /api/admin/horarios/" + id + " - Eliminando horario");

        try {
            horarioService.eliminarHorario(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Horario eliminado exitosamente");

            System.out.println("✅ [API ADMIN SUCCESS] Horario eliminado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al eliminar horario: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar horario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
