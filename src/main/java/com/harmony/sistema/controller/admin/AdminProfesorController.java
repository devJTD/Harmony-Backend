package com.harmony.sistema.controller.admin;

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

import com.harmony.sistema.dto.ProfesorEdicionDTO;
import com.harmony.sistema.dto.ProfesorRegistroDTO;
import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.service.ProfesorService;

/**
 * Controller REST para gestión de profesores en el panel de administración.
 * Maneja operaciones CRUD de profesores.
 */
@RestController
@RequestMapping("/api/admin/profesores")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminProfesorController {

    @Autowired
    private ProfesorService profesorService;

    /**
     * Endpoint para listar todos los profesores.
     * GET /api/admin/profesores
     */
    @GetMapping
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<List<Map<String, Object>>> listarProfesores() {
        System.out.println("🔵 [API ADMIN] GET /api/admin/profesores - Listando profesores");
        try {
            List<Profesor> profesores = profesorService.listarProfesores();

            List<Map<String, Object>> profesoresConCorreo = profesores.stream().map(profesor -> {
                Map<String, Object> profesorMap = new HashMap<>();
                profesorMap.put("id", profesor.getId());
                profesorMap.put("nombreCompleto", profesor.getNombreCompleto());
                profesorMap.put("telefono", profesor.getTelefono());
                profesorMap.put("fotoUrl", profesor.getFotoUrl());
                profesorMap.put("informacion", profesor.getInformacion());

                String correo = profesor.getUser() != null ? profesor.getUser().getEmail() : "";
                profesorMap.put("correo", correo);

                System.out.println("👨‍🏫 [API ADMIN] Profesor ID " + profesor.getId() + " - Correo: " + correo);

                return profesorMap;
            }).collect(Collectors.toList());

            System.out.println("✅ [API ADMIN SUCCESS] " + profesoresConCorreo.size() + " profesores obtenidos");
            return ResponseEntity.ok(profesoresConCorreo);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al listar profesores: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para registrar un nuevo profesor.
     * POST /api/admin/profesores
     */
    @PostMapping
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> registrarProfesor(@RequestBody ProfesorRegistroDTO profesorDto) {

        System.out.println("🔵 [API ADMIN] POST /api/admin/profesores - Registrando profesor");
        System.out.println("👤 [API ADMIN] Nombre: " + profesorDto.getNombreCompleto());
        System.out.println("📧 [API ADMIN] Correo: " + profesorDto.getCorreo());
        System.out.println("📞 [API ADMIN] Teléfono: " + profesorDto.getTelefono());

        try {
            Profesor nuevoProfesor = profesorService.registrarProfesor(profesorDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profesor registrado exitosamente");
            response.put("profesor", nuevoProfesor);

            System.out.println("✅ [API ADMIN SUCCESS] Profesor registrado con ID: " + nuevoProfesor.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al registrar profesor: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al registrar profesor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint para editar un profesor existente.
     * PUT /api/admin/profesores/{id}
     */
    @PutMapping("/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> editarProfesor(
            @PathVariable Long id,
            @RequestBody ProfesorEdicionDTO profesorDto) {

        System.out.println("🔵 [API ADMIN] PUT /api/admin/profesores/" + id + " - Editando profesor");
        System.out.println("📝 [API ADMIN] Datos nuevos - Nombre: " + profesorDto.getNombreCompleto());
        System.out.println("📧 [API ADMIN] Email: " + profesorDto.getCorreo());

        try {
            profesorDto.setId(id);
            Profesor profesorActualizado = profesorService.editarProfesor(profesorDto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profesor actualizado exitosamente");
            response.put("profesor", profesorActualizado);

            System.out.println("✅ [API ADMIN SUCCESS] Profesor actualizado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al editar profesor: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al editar profesor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint para eliminar un profesor.
     * DELETE /api/admin/profesores/{id}
     */
    @DeleteMapping("/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> eliminarProfesor(@PathVariable Long id) {
        System.out.println("🔵 [API ADMIN] DELETE /api/admin/profesores/" + id + " - Eliminando profesor");

        try {
            profesorService.eliminarProfesor(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profesor eliminado exitosamente");

            System.out.println("✅ [API ADMIN SUCCESS] Profesor eliminado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al eliminar profesor: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar profesor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
