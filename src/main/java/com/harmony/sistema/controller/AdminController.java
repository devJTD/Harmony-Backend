package com.harmony.sistema.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.harmony.sistema.dto.ClienteRegistroDTO;
import com.harmony.sistema.dto.CredencialesDTO;
import com.harmony.sistema.dto.InscripcionFormDTO;
import com.harmony.sistema.dto.ProfesorEdicionDTO;
import com.harmony.sistema.dto.ProfesorRegistroDTO;
import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.model.Taller;
import com.harmony.sistema.service.ClienteService;
import com.harmony.sistema.service.HorarioService;
import com.harmony.sistema.service.InscripcionService;
import com.harmony.sistema.service.ProfesorService;
import com.harmony.sistema.service.TallerService;

@Controller
public class AdminController {

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

    // Muestra la vista de gestión de clientes, lista todos los clientes y filtra
    // los horarios de talleres disponibles.
    @GetMapping("/admin/clientes")
    public String clientes(Model model) {
        System.out.println(
                " [ADMIN] Solicitud GET a /admin/clientes. Cargando lista de clientes y talleres disponibles.");
        // 1. Obtiene la lista de todos los clientes y la añade al modelo.
        List<Cliente> clientes = clienteService.listarClientes();
        model.addAttribute("clientes", clientes);
        model.addAttribute("cliente", new ClienteRegistroDTO());

        // 2. Obtiene los talleres activos.
        List<Taller> talleresActivos = tallerService.encontrarTalleresActivos();
        LocalDate hoy = LocalDate.now();

        // 3. Filtra los talleres para incluir solo horarios con vacantes y que no hayan
        // iniciado, y los añade al modelo.
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
        }).collect(Collectors.toList());
        model.addAttribute("talleres", talleresFiltrados);

        // 4. Retorna la vista.
        System.out.println(" [ADMIN] Cargados " + clientes.size() + " clientes y " + talleresFiltrados.size()
                + " talleres con horarios. Retornando 'clientesAdmin'.");
        return "clientesAdmin";
    }

    // Registra un nuevo cliente y sus inscripciones a talleres, procesando la
    // información del formulario.
    @PostMapping("/admin/clientes/registrar")
    public String registrarCliente(
            @ModelAttribute("cliente") ClienteRegistroDTO adminDto,
            @RequestParam Map<String, String> allRequestParams,
            RedirectAttributes redirectAttributes) {

        System.out.println(" [ADMIN] Solicitud POST a /admin/clientes/registrar. Cliente a registrar: "
                + adminDto.getNombreCompleto());

        // 1. Mapea los IDs de Taller y Horario seleccionados del formulario.
        Map<Long, Long> horariosSeleccionados = new HashMap<>();
        if (adminDto.getTalleresSeleccionados() != null) {
            for (Long tallerId : adminDto.getTalleresSeleccionados()) {
                String paramName = "horarioTaller" + tallerId;
                String horarioIdStr = allRequestParams.get(paramName);
                if (horarioIdStr != null && !horarioIdStr.isEmpty()) {
                    try {
                        horariosSeleccionados.put(tallerId, Long.valueOf(horarioIdStr));
                    } catch (NumberFormatException e) {
                        System.out.println(" [ADMIN ERROR] Error de formato en ID de horario para taller " + tallerId
                                + ". Deteniendo registro.");
                        redirectAttributes.addFlashAttribute("error",
                                "Error al procesar el horario del taller ID: " + tallerId);
                        return "redirect:/admin/clientes";
                    }
                }
            }
        }

        // 2. Transfiere los datos del DTO de registro al DTO de inscripción.
        InscripcionFormDTO inscripcionDto = new InscripcionFormDTO();
        inscripcionDto.setNombre(adminDto.getNombreCompleto());
        inscripcionDto.setEmail(adminDto.getCorreo());
        inscripcionDto.setTelefono(adminDto.getTelefono());
        inscripcionDto.setTalleresSeleccionados(adminDto.getTalleresSeleccionados());

        // 3. Procesa la inscripción completa (crea User, Cliente e Inscripciones) y
        // maneja el éxito/error.
        try {
            // CAMBIO CLAVE: Cambiamos 'User' por 'CredencialesDTO'
            CredencialesDTO credenciales = inscripcionService.procesarInscripcionCompleta(inscripcionDto,
                    horariosSeleccionados);

            String mensajeExito = String.format(
                    "Cliente **%s** registrado con éxito. Correo enviado a **%s**. Contraseña Temporal: **%s**.",
                    adminDto.getNombreCompleto(),
                    // Usamos los getters del nuevo DTO
                    credenciales.getCorreo(),
                    credenciales.getContrasenaTemporal());
            redirectAttributes.addFlashAttribute("success", mensajeExito);
            System.out.println(" [ADMIN SUCCESS] Cliente '" + adminDto.getNombreCompleto()
                    + "' registrado correctamente con " + horariosSeleccionados.size() + " inscripciones.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar cliente: " + e.getMessage());
            System.out.println(" [ADMIN ERROR] Fallo al registrar cliente: " + e.getMessage());
        }

        // 4. Redirige a la vista de clientes.
        return "redirect:/admin/clientes";
    }

    // Edita la información de un cliente existente (nombre, correo, teléfono).
    @PostMapping("/admin/clientes/editar")
    public String editarCliente(
            @RequestParam("id") Long clienteId,
            @RequestParam("nombre") String nombre,
            @RequestParam("correo") String correo,
            @RequestParam("telefono") String telefono,
            @RequestParam("originalCorreo") String originalCorreo,
            RedirectAttributes redirectAttributes) {

        System.out.println(" [ADMIN] Solicitud POST a /admin/clientes/editar. Editando Cliente ID: " + clienteId
                + " (Correo Original: " + originalCorreo + ")");

        // 1. Llama al servicio para actualizar los datos del cliente.
        try {
            Cliente clienteActualizado = clienteService.actualizarCliente(clienteId, nombre, correo, telefono,
                    originalCorreo);

            // 2. Prepara el mensaje de éxito, añadiendo una nota si el correo cambió.
            String mensajeExito = "Cliente **" + clienteActualizado.getNombreCompleto() + "** (ID: " + clienteId
                    + ") actualizado con éxito.";
            if (!originalCorreo.equalsIgnoreCase(correo)) {
                mensajeExito += " El correo fue modificado y se envió una **NUEVA CONTRASEÑA TEMPORAL** al nuevo correo.";
                System.out.println(
                        " [ADMIN] Correo de Cliente ID " + clienteId + " fue modificado. Se generó nueva contraseña.");
            }
            redirectAttributes.addFlashAttribute("success", mensajeExito);
            System.out.println(" [ADMIN SUCCESS] Cliente ID " + clienteId + " actualizado correctamente.");
        } catch (RuntimeException e) {
            // 3. Maneja cualquier error.
            redirectAttributes.addFlashAttribute("error",
                    "Error al actualizar cliente ID " + clienteId + ": " + e.getMessage());
            System.out.println(" [ADMIN ERROR] Fallo al actualizar Cliente ID " + clienteId + ": " + e.getMessage());
        }

        // 4. Redirige a la vista de clientes.
        return "redirect:/admin/clientes";
    }

    // Elimina un cliente y su User asociado de forma permanente.
    @PostMapping("/admin/clientes/eliminar")
    public String eliminarCliente(
            @RequestParam("clienteId") Long clienteId,
            RedirectAttributes redirectAttributes) {

        System.out.println(" [ADMIN] Solicitud POST a /admin/clientes/eliminar. Eliminando Cliente ID: " + clienteId);

        // 1. Llama al servicio para eliminar el cliente.
        try {
            clienteService.eliminarCliente(clienteId);
            // 2. Añade un mensaje de éxito.
            redirectAttributes.addFlashAttribute("success",
                    "Cliente con ID **" + clienteId + "** eliminado definitivamente del sistema.");
            System.out.println(" [ADMIN SUCCESS] Cliente ID " + clienteId + " eliminado permanentemente.");
        } catch (RuntimeException e) {
            // 3. Maneja cualquier error.
            redirectAttributes.addFlashAttribute("error",
                    "Error al eliminar cliente ID " + clienteId + ": " + e.getMessage());
            System.out.println(" [ADMIN ERROR] Fallo al eliminar Cliente ID " + clienteId + ": " + e.getMessage());
        }

        // 4. Redirige a la vista de clientes.
        return "redirect:/admin/clientes";
    }

    // Muestra la vista de gestión de profesores, lista todos los profesores y añade
    // DTOs de registro y edición al modelo.
    @GetMapping("/admin/profesores")
    public String profesores(Model model) {
        System.out.println(" [ADMIN] Solicitud GET a /admin/profesores. Cargando lista de profesores.");
        // 1. Obtiene y añade la lista de profesores al modelo.
        List<Profesor> profesores = profesorService.listarProfesores();
        model.addAttribute("profesores", profesores);
        // 2. Añade el DTO de registro para el formulario.
        model.addAttribute("profesor", new ProfesorRegistroDTO());
        // 3. Retorna la vista.
        System.out.println(" [ADMIN] Cargados " + profesores.size() + " profesores. Retornando 'profesoresAdmin'.");
        return "profesoresAdmin";
    }

    // Registra un nuevo profesor con su entidad User asociada.
    @PostMapping("/admin/profesores/registrar")
    public String registrarProfesor(@ModelAttribute("profesor") ProfesorRegistroDTO dto) {
        System.out.println(" [ADMIN] Solicitud POST a /admin/profesores/registrar. Registrando profesor: "
                + dto.getNombreCompleto());
        // 1. Llama al servicio para registrar el profesor.
        profesorService.registrarProfesor(dto);
        // 2. Redirige a la vista de profesores.
        System.out.println(" [ADMIN SUCCESS] Profesor '" + dto.getNombreCompleto() + "' registrado correctamente.");
        return "redirect:/admin/profesores?exito=registro";
    }

    // Edita los datos de un profesor existente.
    @PostMapping("/admin/profesores/editar")
    public String editarProfesor(@ModelAttribute ProfesorEdicionDTO dto) {
        System.out.println(" [ADMIN] Solicitud POST a /admin/profesores/editar. Editando profesor ID: " + dto.getId());
        // 1. Llama al servicio para editar el profesor y maneja el éxito/error en la
        // redirección.
        try {
            profesorService.editarProfesor(dto);
            System.out.println(" [ADMIN SUCCESS] Profesor ID " + dto.getId() + " editado correctamente.");
            return "redirect:/admin/profesores?exito=edicion";
        } catch (RuntimeException e) {
            // 2. Maneja el error.
            System.out.println(" [ADMIN ERROR] Fallo al editar profesor ID " + dto.getId() + ": " + e.getMessage());
            // Se mantiene el System.err para que el error aparezca en rojo, como en el
            // código original.
            System.err.println("Error al editar profesor: " + e.getMessage());
            return "redirect:/admin/profesores?error=edicion";
        }
    }

    // Elimina un profesor por ID.
    @GetMapping("/admin/profesores/eliminar/{id}")
    public String eliminarProfesor(@PathVariable("id") Long id) {
        System.out.println(" [ADMIN] Solicitud GET a /admin/profesores/eliminar/{id}. Eliminando profesor ID: " + id);
        // 1. Llama al servicio para eliminar el profesor.
        profesorService.eliminarProfesor(id);
        // 2. Redirige a la vista de profesores.
        System.out.println(" [ADMIN] Profesor ID " + id + " eliminado.");
        return "redirect:/admin/profesores?exito=eliminacion";
    }

    // Muestra la vista de gestión de talleres y horarios, lista talleres y
    // profesores, y añade DTOs de formulario.
    @GetMapping("/admin/talleres")
    public String talleres(Model model) {
        System.out.println(" [ADMIN] Solicitud GET a /admin/talleres. Cargando talleres y profesores.");
        // 1. Obtiene y añade las listas de talleres y talleres activos.
        List<Taller> talleres = tallerService.listarTalleres();
        List<Taller> talleresActivos = tallerService.encontrarTalleresActivos();
        model.addAttribute("tallerCrearForm", new Taller());
        // 2. Obtiene y añade la lista de profesores.
        model.addAttribute("profesores", profesorService.listarProfesores());
        model.addAttribute("talleres", talleres);
        model.addAttribute("talleresActivos", talleresActivos);
        // 3. Añade el DTO de edición.
        model.addAttribute("tallerEditarForm", new Taller());
        // 4. Retorna la vista.
        System.out.println(" [ADMIN] Cargados " + talleres.size() + " talleres. Retornando 'talleresAdmin'.");
        return "talleresAdmin";
    }

    // Registra un nuevo taller.
    @PostMapping("/admin/talleres/registrar")
    public String registrarTaller(
            @ModelAttribute("tallerCrearForm") Taller taller,
            RedirectAttributes redirectAttributes) {

        System.out.println(
                " [ADMIN] Solicitud POST a /admin/talleres/registrar. Registrando taller: " + taller.getNombre());
        // 1. Llama al servicio para crear el taller y maneja el éxito/error.
        try {
            tallerService.crearTallerSolo(taller);
            redirectAttributes.addFlashAttribute("success", "Taller creado con éxito.");
            System.out.println(" [ADMIN SUCCESS] Taller '" + taller.getNombre() + "' creado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar taller: " + e.getMessage());
            System.out.println(" [ADMIN ERROR] Fallo al registrar taller: " + e.getMessage());
        }
        // 2. Redirige a la vista de talleres.
        return "redirect:/admin/talleres";
    }

    // Edita la información de un taller existente.
    @PostMapping("/admin/talleres/editar")
    public String editarTaller(@ModelAttribute("tallerEditarForm") Taller tallerActualizado,
            RedirectAttributes redirectAttributes) {
        System.out.println(
                " [ADMIN] Solicitud POST a /admin/talleres/editar. Editando taller ID: " + tallerActualizado.getId());
        // 1. Llama al servicio para editar el taller y maneja el éxito/error.
        try {
            tallerService.editarTaller(tallerActualizado);
            redirectAttributes.addFlashAttribute("success", "Taller actualizado con éxito.");
            System.out.println(" [ADMIN SUCCESS] Taller ID " + tallerActualizado.getId() + " actualizado.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error al editar taller: " + e.getMessage());
            System.out.println(
                    " [ADMIN ERROR] Fallo al editar taller ID " + tallerActualizado.getId() + ": " + e.getMessage());
        }
        // 2. Redirige a la vista de talleres.
        return "redirect:/admin/talleres";
    }

    // Registra un nuevo horario asociado a un taller y profesor.
    @PostMapping("/admin/horarios/registrar")
    public String registrarHorario(
            @RequestParam("tallerId") Long tallerId,
            @RequestParam("profesorId") Long profesorId,
            @RequestParam(value = "diasDeClase", required = false) String[] diasDeClaseArray,
            @RequestParam("horaInicio") LocalTime horaInicio,
            @RequestParam("horaFin") LocalTime horaFin,
            @RequestParam("fechaInicio") LocalDate fechaInicio,
            @RequestParam("vacantesDisponibles") int vacantesDisponibles,
            RedirectAttributes redirectAttributes) {

        System.out.println(" [ADMIN] Solicitud POST a /admin/horarios/registrar. Taller ID: " + tallerId
                + ", Profesor ID: " + profesorId);

        // 1. Procesa los días de clase y valida que se haya seleccionado al menos uno.
        String diasDeClase = (diasDeClaseArray != null && diasDeClaseArray.length > 0)
                ? String.join(", ", diasDeClaseArray)
                : "";
        if (diasDeClase.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar al menos un día de clase.");
            System.out.println(" [ADMIN ERROR] Fallo al registrar horario: No se seleccionaron días de clase.");
            return "redirect:/admin/talleres";
        }

        // 2. Llama al servicio para crear el horario y maneja el éxito/error.
        try {
            horarioService.crearHorario(
                    tallerId,
                    profesorId,
                    diasDeClase,
                    horaInicio,
                    horaFin,
                    fechaInicio,
                    vacantesDisponibles);
            redirectAttributes.addFlashAttribute("success", "Horario creado con éxito.");
            System.out.println(
                    " [ADMIN SUCCESS] Horario registrado para Taller ID " + tallerId + " (" + diasDeClase + ").");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar el horario: " + e.getMessage());
            System.out.println(" [ADMIN ERROR] Fallo al registrar horario: " + e.getMessage());
        }
        // 3. Redirige a la vista de talleres.
        return "redirect:/admin/talleres";
    }

    // Edita un horario existente.
    @PostMapping("/admin/horarios/editar")
    public String editarHorario(
            @RequestParam("id") Long horarioId,
            @RequestParam("profesorId") Long profesorId,
            @RequestParam("diasDeClase") String diasDeClase,
            @RequestParam("horaInicio") LocalTime horaInicio,
            @RequestParam(value = "horaFin", required = false) LocalTime horaFin,
            @RequestParam("fechaInicio") LocalDate fechaInicio,
            @RequestParam("vacantesDisponibles") int vacantesDisponibles,
            RedirectAttributes redirectAttributes) {

        System.out.println(" [ADMIN] Solicitud POST a /admin/horarios/editar. Editando Horario ID: " + horarioId);

        // 1. Si la hora fin es nula, la recupera del horario existente.
        if (horaFin == null) {
            System.out.println(" [ADMIN] Hora Fin no proporcionada para Horario ID " + horarioId
                    + ". Recuperando valor existente.");
            Horario horarioExistente = horarioService.getHorarioById(horarioId);
            horaFin = horarioExistente.getHoraFin();
        }

        // 2. Llama al servicio para editar el horario y maneja el éxito/error.
        try {
            horarioService.editarHorario(
                    horarioId,
                    profesorId,
                    diasDeClase,
                    horaInicio,
                    horaFin,
                    fechaInicio,
                    vacantesDisponibles);
            redirectAttributes.addFlashAttribute("success", "Horario ID " + horarioId + " actualizado con éxito.");
            System.out.println(" [ADMIN SUCCESS] Horario ID " + horarioId + " actualizado.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error al editar el horario: " + e.getMessage());
            System.out.println(" [ADMIN ERROR] Fallo al editar Horario ID " + horarioId + ": " + e.getMessage());
        }
        // 3. Redirige a la vista de talleres.
        return "redirect:/admin/talleres";
    }

    // Retorna los datos de un horario específico en formato JSON (API endpoint).
    @GetMapping("/api/horarios/{id}")
    @ResponseBody
    public ResponseEntity<Horario> getHorarioJson(@PathVariable("id") Long id) {
        System.out.println(" [API] Solicitud GET a /api/horarios/{id}. Buscando Horario ID: " + id);
        // 1. Llama al servicio para obtener el horario.
        try {
            Horario horario = horarioService.getHorarioById(id);
            // 2. Retorna el horario con código HTTP 200.
            System.out.println(" [API SUCCESS] Horario ID " + id + " encontrado y retornado.");
            return ResponseEntity.ok(horario);
        } catch (RuntimeException e) {
            // 3. Retorna código HTTP 404 si no se encuentra.
            System.out.println(" [API ERROR] Horario ID " + id + " no encontrado. Retornando 404.");
            return ResponseEntity.notFound().build();
        }
    }

    // Elimina un horario por ID.
    @GetMapping("/admin/horarios/eliminar/{id}")
    public String eliminarHorario(@PathVariable("id") Long horarioId, RedirectAttributes redirectAttributes) {
        System.out
                .println(" [ADMIN] Solicitud GET a /admin/horarios/eliminar/{id}. Eliminando Horario ID: " + horarioId);
        // 1. Llama al servicio para eliminar el horario y maneja el éxito/error.
        try {
            horarioService.eliminarHorario(horarioId);
            redirectAttributes.addFlashAttribute("success", "Horario ID " + horarioId + " eliminado permanentemente.");
            System.out.println(" [ADMIN SUCCESS] Horario ID " + horarioId + " eliminado.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al eliminar el horario ID " + horarioId + ": " + e.getMessage());
            System.out.println(" [ADMIN ERROR] Fallo al eliminar Horario ID " + horarioId + ": " + e.getMessage());
        }
        // 2. Redirige a la vista de talleres.
        return "redirect:/admin/talleres";
    }

}