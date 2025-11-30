package com.harmony.sistema.controller.profesor;

import com.harmony.sistema.dto.CambioClaveRequest; // Importado el nuevo DTO
import com.harmony.sistema.dto.HorarioProfesorDTO; // Importado el nuevo DTO
import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.model.User;
import com.harmony.sistema.repository.ProfesorRepository;
import com.harmony.sistema.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profesor")
@CrossOrigin(origins = "http://localhost:4200")
public class ProfesorPanelController {

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Endpoint para obtener los horarios asignados al profesor autenticado.
     * GET /api/profesor/horarios
     */
    @GetMapping("/horarios")
    public ResponseEntity<List<HorarioProfesorDTO>> getHorarios(Authentication authentication) {
        String email = authentication.getName();

        // CORREGIDO: findByUserEmail ahora existe en ProfesorRepository
        Profesor profesor = profesorRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        List<HorarioProfesorDTO> horarios = profesor.getHorariosImpartidos().stream() // Usamos getHorariosImpartidos
                .map(h -> {
                    HorarioProfesorDTO dto = new HorarioProfesorDTO();
                    dto.setId(h.getId());
                    dto.setDiasDeClase(h.getDiasDeClase());
                    dto.setHoraInicio(h.getHoraInicio().toString());
                    dto.setHoraFin(h.getHoraFin().toString());

                    // Usamos el DTO anidado
                    dto.setTaller(new HorarioProfesorDTO.TallerSimpleDTO(h.getTaller().getNombre()));
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(horarios);
    }

    /**
     * Endpoint para cambiar la contraseña del profesor.
     * POST /api/profesor/cambiar-clave
     */
    @PostMapping("/cambiar-clave")
    public ResponseEntity<String> cambiarClave(
            @RequestBody CambioClaveRequest request, // Usando el DTO externo
            Authentication authentication) {

        String email = authentication.getName();

        if (!request.getNuevaContrasena().equals(request.getConfirmarContrasena())) {
            return ResponseEntity.badRequest().body("Las contraseñas no coinciden");
        }

        if (request.getNuevaContrasena().length() < 6) {
            return ResponseEntity.badRequest().body("La contraseña debe tener al menos 6 caracteres");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(request.getNuevaContrasena()));
        userRepository.save(user);

        return ResponseEntity.ok("Contraseña cambiada exitosamente");
    }

    @Autowired
    private com.harmony.sistema.service.ProfesorService profesorService;

    /**
     * Endpoint para cancelar una clase específica.
     * POST /api/profesor/cancelar-clase/{horarioId}
     */
    @PostMapping("/cancelar-clase/{horarioId}")
    public ResponseEntity<?> cancelarClase(@PathVariable Long horarioId,
            @RequestBody com.harmony.sistema.model.ClaseCancelada claseCancelada) {
        try {
            com.harmony.sistema.model.ClaseCancelada saved = profesorService.cancelarClase(horarioId, claseCancelada);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}