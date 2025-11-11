package com.harmony.sistema.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // IMPORTANTE: Nuevo import para Flash Attributes

import com.harmony.sistema.dto.CredencialesDTO; // IMPORTANTE: Nuevo import para el DTO de credenciales
import com.harmony.sistema.dto.InscripcionFormDTO;
import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Taller;
import com.harmony.sistema.service.InscripcionService;
import com.harmony.sistema.service.TallerService;

@Controller
public class InscripcionController {

    @Autowired
    TallerService tallerService;
    
    @Autowired
    InscripcionService inscripcionService; // Asegúrate de que este servicio ahora retorna CredencialesDTO

    // Muestra el formulario de inscripción, filtrando los talleres activos para mostrar solo horarios disponibles (con vacantes y no iniciados).
    @GetMapping("/inscripcion")
    public String inscripcion(Model model) {
        System.out.println(" [REQUEST] Mapeando solicitud GET a /inscripcion. Cargando talleres disponibles.");
        
        // 1. Obtiene la lista de todos los talleres activos.
        List<Taller> talleres = tallerService.encontrarTalleresActivos();
        LocalDate hoy = LocalDate.now();

        // 2. Filtra los talleres, manteniendo solo los horarios que tienen vacantes y cuya fecha de inicio no ha pasado.
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
        }).collect(Collectors.toList());

        // 3. Agrega la lista filtrada al modelo.
        model.addAttribute("talleres", talleresFiltrados);
        System.out.println(" [DATA] Cargados " + talleresFiltrados.size() + " talleres activos con horarios disponibles.");

        // 4. Retorna la vista de inscripción.
        return "inscripcion";
    }

    /**
     * Procesa el formulario de inscripción, creando la entidad User, Cliente y los registros de Inscripción.
     * Utiliza RedirectAttributes para evitar que los datos de la URL sean visibles en la redirección.
     */
    @PostMapping("/confirmacion")
    public String procesarInscripcion(
            InscripcionFormDTO dto,
            @RequestParam Map<String, String> allRequestParams,
            RedirectAttributes redirectAttributes) { // Cambio: Usamos RedirectAttributes en lugar de Model

        System.out.println(" [REQUEST] Mapeando solicitud POST a /confirmacion. Procesando inscripción para: " + dto.getEmail());

        // 1. Itera sobre los talleres seleccionados en el DTO y mapea el Taller ID con el Horario ID.
        Map<Long, Long> horariosSeleccionados = new HashMap<>();
        if (dto.getTalleresSeleccionados() != null) {
            System.out.println(" [INFO] Intentando mapear " + dto.getTalleresSeleccionados().size() + " talleres seleccionados.");
            for (Long tallerId : dto.getTalleresSeleccionados()) {
                String paramName = "horarioTaller" + tallerId;
                String horarioIdStr = allRequestParams.get(paramName);
                if (horarioIdStr != null && !horarioIdStr.isEmpty()) {
                    try {
                        horariosSeleccionados.put(tallerId, Long.valueOf(horarioIdStr));
                    } catch (NumberFormatException e) {
                        System.out.println(" [ERROR] Error al parsear ID de horario para el Taller " + tallerId + ": " + horarioIdStr + ". Detalle: " + e.getMessage());
                    }
                }
            }
        }
        System.out.println(" [DATA] Horarios seleccionados mapeados: " + horariosSeleccionados.size());

        // 2. Llama al servicio para procesar la inscripción completa.
        try {
            // ASUMIMOS que este método ha sido actualizado para devolver CredencialesDTO
            CredencialesDTO credenciales = inscripcionService.procesarInscripcionCompleta(dto, horariosSeleccionados);
            System.out.println(" [SERVICE SUCCESS] Inscripción completa procesada. Usuario creado con correo: " + credenciales.getCorreo());

            // 3. Usa Flash Attributes para pasar los datos al siguiente GET (protege la URL).
            redirectAttributes.addFlashAttribute("correo", credenciales.getCorreo());
            redirectAttributes.addFlashAttribute("contrasena", credenciales.getContrasenaTemporal()); // Contraseña temporal NO codificada
            redirectAttributes.addFlashAttribute("exito", "¡Inscripción exitosa! Revisa tu correo.");

            // 4. Redirige a la URL de confirmación (método GET).
            return "redirect:/confirmacion";
            
        } catch (RuntimeException e) {
            System.out.println(" [SERVICE ERROR] Fallo al procesar la inscripción. Detalle: " + e.getMessage());
            
            // 5. En caso de error, usa Flash Attribute para el mensaje y redirige al formulario.
            // Esto elimina el query parameter visible (?error=...)
            redirectAttributes.addFlashAttribute("error", "Error en la inscripción: " + e.getMessage());
            return "redirect:/inscripcion"; 
        }
    }

    /**
     * Muestra la página de confirmación.
     * Los datos 'correo' y 'contrasena' llegan aquí automáticamente en el 'Model'
     * si la solicitud fue redirigida desde el POST mediante Flash Attributes.
     */
    @GetMapping("/confirmacion")
    public String mostrarConfirmacion(Model model) {
        // Verifica si los atributos flash existen. Si no existen, significa que el usuario
        // intentó acceder directamente con un GET.
        if (model.getAttribute("correo") == null) {
             System.out.println(" [REQUEST] Solicitud GET directa a /confirmacion sin Flash Attributes. Redirigiendo a /.");
            return "redirect:/"; // Redirige a la página principal si no hay datos de confirmación
        }
        System.out.println(" [REQUEST] Solicitud GET a /confirmacion con Flash Attributes. Mostrando vista.");
        return "confirmacion";
    }
}