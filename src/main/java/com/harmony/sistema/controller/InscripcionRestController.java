package com.harmony.sistema.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.harmony.sistema.dto.DatosPersonalesFormDTO;
import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.dto.CredencialesDTO; // Importamos el DTO de salida real del servicio
import com.harmony.sistema.dto.InscripcionFormDTO;
import com.harmony.sistema.dto.InscripcionPayloadDTO; 
import com.harmony.sistema.dto.InscripcionResponseDTO; // DTO final para la respuesta JSON
import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Taller;
import com.harmony.sistema.service.InscripcionService;
import com.harmony.sistema.service.TallerService;

@RestController
@RequestMapping("/api/inscripcion") // Base REST path
public class InscripcionRestController {

    @Autowired
    TallerService tallerService;
    @Autowired
    InscripcionService inscripcionService;

    // --- NUEVO ENDPOINT PARA EL PASO 1: CREAR CLIENTE ---
    @PostMapping("/cliente")
    public ResponseEntity<Cliente> guardarDatosPersonales(@RequestBody DatosPersonalesFormDTO datos) {
        System.out.println(" [REST REQUEST] POST a /api/inscripcion/cliente. Creando cliente: " + datos.getNombre());
        try {
            // Llama al nuevo método del servicio para crear/obtener el cliente
            Cliente clienteGuardado = inscripcionService.guardarOObtenerClienteTemporal(datos);
            System.out.println(" [REST SERVICE SUCCESS] Cliente creado/obtenido con ID: " + clienteGuardado.getId());
            
            // Retorna 200 OK con el objeto Cliente (incluyendo el ID)
            return new ResponseEntity<>(clienteGuardado, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println(" [REST SERVICE ERROR] Fallo al guardar datos personales. Detalle: " + e.getMessage());
            // Retorna 400 Bad Request en caso de fallo (ej. email ya registrado)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    
    // Este endpoint obtiene los talleres, similar al antiguo GET, pero retorna JSON.
    @GetMapping("/talleresDisponibles")
    public ResponseEntity<List<Taller>> getTalleresDisponibles() {
        System.out.println(" [REST REQUEST] GET a /api/inscripcion/talleresDisponibles. Cargando talleres disponibles.");
        
        List<Taller> talleres = tallerService.encontrarTalleresActivos();
        LocalDate hoy = LocalDate.now();

        // Lógica de filtrado de horarios (exactamente la misma lógica del GET anterior)
        List<Taller> talleresFiltrados = talleres.stream().map(taller -> {
            if (taller.isActivo()) {
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
        })
        .filter(taller -> !taller.getHorarios().isEmpty()) // Opcional: solo talleres con horarios disponibles
        .collect(Collectors.toList());

        System.out.println(" [REST DATA] Cargados " + talleresFiltrados.size() + " talleres activos con horarios disponibles.");
        
        // Retorna 200 OK con la lista de Talleres en JSON
        return new ResponseEntity<>(talleresFiltrados, HttpStatus.OK);
    }

    // Este endpoint procesa la inscripción, reemplazando el antiguo POST /confirmacion
    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmarInscripcion(@RequestBody InscripcionPayloadDTO payload) {
        System.out.println(" [REST REQUEST] POST a /api/inscripcion/confirmar. Procesando inscripción para: " + payload.getEmail());

        // 1. Mapear InscripcionPayloadDTO a InscripcionFormDTO para usar el Servicio existente
        InscripcionFormDTO formDTO = new InscripcionFormDTO();
        formDTO.setNombre(payload.getNombre());
        formDTO.setEmail(payload.getEmail());
        formDTO.setTelefono(payload.getTelefono());
        // **IMPORTANTE:** Si InscripcionFormDTO requiere talleresSeleccionados (lista de Taller IDs) 
        // para la validación inicial, puedes construirlo aquí desde el payload.
        
        // 2. Mapear la lista de inscripciones del payload a Map<TallerId, HorarioId>
        Map<Long, Long> horariosSeleccionados = new HashMap<>();
        if (payload.getInscripciones() != null) {
            payload.getInscripciones().forEach(inscripcion -> {
                horariosSeleccionados.put(inscripcion.getTallerId(), inscripcion.getHorarioId());
            });
        }
        System.out.println(" [REST DATA] Inscripciones a procesar: " + horariosSeleccionados.size());

        // 3. Llama al servicio de negocio (que ahora devuelve CredencialesDTO).
        try {
            // CAMBIO: Usamos CredencialesDTO (lo que devuelve el servicio)
            CredencialesDTO credenciales = inscripcionService.procesarInscripcionCompleta(formDTO, horariosSeleccionados);
            System.out.println(" [REST SERVICE SUCCESS] Usuario creado con correo: " + credenciales.getCorreo());

            // 4. Prepara la respuesta usando InscripcionResponseDTO.
            // Los DTOs tienen la misma estructura, solo cambian de nombre.
            InscripcionResponseDTO response = new InscripcionResponseDTO(
                credenciales.getCorreo(), 
                credenciales.getContrasenaTemporal()
            );
            
            // 5. Retorna 201 Created con la respuesta JSON.
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            System.err.println(" [REST SERVICE ERROR] Fallo al procesar la inscripción. Detalle: " + e.getMessage());
            // 6. En caso de error, retorna 400 Bad Request.
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Fallo en la inscripción.");
            errorResponse.put("mensaje", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}
