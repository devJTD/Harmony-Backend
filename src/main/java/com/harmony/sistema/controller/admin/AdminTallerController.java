package com.harmony.sistema.controller.admin;

import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harmony.sistema.model.Taller;
import com.harmony.sistema.service.TallerService;

/**
 * Controller REST para gestión de talleres en el panel de administración.
 * Maneja operaciones CRUD de talleres.
 */
@RestController
@RequestMapping("/api/admin/talleres")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminTallerController {

    @Autowired
    private TallerService tallerService;

    /**
     * Endpoint para listar todos los talleres.
     * GET /api/admin/talleres
     */
    @GetMapping
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<List<Taller>> listarTalleres() {
        System.out.println("🔵 [API ADMIN] GET /api/admin/talleres - Listando talleres");
        try {
            List<Taller> talleres = tallerService.listarTalleres();
            System.out.println("✅ [API ADMIN SUCCESS] " + talleres.size() + " talleres obtenidos");
            return ResponseEntity.ok(talleres);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al listar talleres: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para listar talleres activos.
     * GET /api/admin/talleres/activos
     */
    @GetMapping("/activos")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<List<Taller>> listarTalleresActivos() {
        System.out.println("🔵 [API ADMIN] GET /api/admin/talleres/activos - Listando talleres activos");
        try {
            List<Taller> talleresActivos = tallerService.encontrarTalleresActivos();
            System.out.println("✅ [API ADMIN SUCCESS] " + talleresActivos.size() + " talleres activos obtenidos");
            return ResponseEntity.ok(talleresActivos);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al listar talleres activos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para registrar un nuevo taller.
     * POST /api/admin/talleres
     */
    @PostMapping
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> registrarTaller(@RequestBody Taller taller) {
        System.out.println("========================================");
        System.out.println("🔵 [API ADMIN] POST /api/admin/talleres");
        System.out.println("========================================");
        System.out.println("📝 [API ADMIN] Datos recibidos:");
        System.out.println("   - Nombre: " + taller.getNombre());
        System.out.println("   - Descripción: " + (taller.getDescripcion() != null
                ? taller.getDescripcion().substring(0, Math.min(50, taller.getDescripcion().length())) + "..."
                : "null"));
        System.out.println("   - Duración Semanas: " + taller.getDuracionSemanas());
        System.out.println("   - Clases por Semana: " + taller.getClasesPorSemana());
        System.out.println("   - Precio: " + taller.getPrecio());
        System.out.println("   - Imagen Taller: " + taller.getImagenTaller());
        System.out.println("   - Imagen Inicio: " + taller.getImagenInicio());
        System.out.println("   - Temas: " + (taller.getTemas() != null
                ? taller.getTemas().substring(0, Math.min(30, taller.getTemas().length())) + "..."
                : "null"));

        try {
            System.out.println("🚀 [API ADMIN] Llamando a tallerService.crearTallerSolo()");
            Taller nuevoTaller = tallerService.crearTallerSolo(taller);
            System.out.println("✅ [API ADMIN SUCCESS] Taller creado con ID: " + nuevoTaller.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Taller creado exitosamente");
            response.put("taller", nuevoTaller);

            System.out.println("========================================\n");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Excepción capturada:");
            System.out.println("   - Tipo: " + e.getClass().getName());
            System.out.println("   - Mensaje: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al registrar taller: " + e.getMessage());

            System.out.println("========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint para editar un taller existente.
     * PUT /api/admin/talleres/{id}
     */
    @PutMapping("/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> editarTaller(
            @PathVariable Long id,
            @RequestBody Taller tallerActualizado) {

        System.out.println("========================================");
        System.out.println("🔵 [API ADMIN] PUT /api/admin/talleres/" + id);
        System.out.println("========================================");
        System.out.println("📝 [API ADMIN] Datos recibidos para actualización:");
        System.out.println("   - Taller ID: " + id);
        System.out.println("   - Nuevo Nombre: " + tallerActualizado.getNombre());
        System.out
                .println(
                        "   - Nueva Descripción: " + (tallerActualizado.getDescripcion() != null
                                ? tallerActualizado.getDescripcion().substring(0,
                                        Math.min(50, tallerActualizado.getDescripcion().length())) + "..."
                                : "null"));
        System.out.println("   - Nuevo Precio: " + tallerActualizado.getPrecio());
        System.out.println("   - Nueva Imagen Taller: " + tallerActualizado.getImagenTaller());
        System.out.println("   - Nueva Imagen Inicio: " + tallerActualizado.getImagenInicio());
        System.out.println("   - Activo: " + tallerActualizado.isActivo());

        try {
            System.out.println("🚀 [API ADMIN] Llamando a tallerService.editarTaller()");
            tallerActualizado.setId(id);
            Taller taller = tallerService.editarTaller(tallerActualizado);
            System.out.println("✅ [API ADMIN SUCCESS] Taller actualizado correctamente");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Taller actualizado exitosamente");
            response.put("taller", taller);

            System.out.println("========================================\n");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Excepción capturada:");
            System.out.println("   - Tipo: " + e.getClass().getName());
            System.out.println("   - Mensaje: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al editar taller: " + e.getMessage());

            System.out.println("========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Endpoint para eliminar un taller.
     * DELETE /api/admin/talleres/{id}
     */
    @DeleteMapping("/{id}")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> eliminarTaller(@PathVariable Long id) {
        System.out.println("🔵 [API ADMIN] DELETE /api/admin/talleres/" + id + " - Eliminando taller");

        try {
            tallerService.eliminarTaller(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Taller eliminado exitosamente");

            System.out.println("✅ [API ADMIN SUCCESS] Taller eliminado: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ [API ADMIN ERROR] Error al eliminar taller: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al eliminar taller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
