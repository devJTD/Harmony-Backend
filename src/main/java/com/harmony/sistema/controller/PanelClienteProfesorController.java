package com.harmony.sistema.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.harmony.sistema.model.Horario;
import com.harmony.sistema.service.ContraseñaService;
import com.harmony.sistema.service.HorarioService;
import com.harmony.sistema.service.UserService;

@Controller
public class PanelClienteProfesorController {

    @Autowired
    private UserService userService;
    @Autowired
    private HorarioService horarioService;
    @Autowired
    private ContraseñaService contrasenaService;

    // Muestra el horario del usuario logueado, ajustando el contenido según el rol (Profesor o Cliente).
    @GetMapping("/horario")
    @SuppressWarnings("ConvertToStringSwitch")
    public String mostrarHorario(Model model) {
        System.out.println(" [REQUEST] Mapeando solicitud GET a /horario. Acceso al panel de usuario.");
        // 1. Obtiene la autenticación actual (email) y el rol del usuario logueado.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        String role = userService.getRoleByUserEmail(userEmail);
        String nombreCompleto = userService.getNombreCompletoByUserEmail(userEmail);
        List<Horario> horarios;
        String tituloBienvenida = "Usuario";
        
        System.out.println(" [AUTH] Usuario logueado: " + userEmail + ", con rol: " + role);
        
        // 2. Determina el rol y obtiene los horarios correspondientes.
        if (role != null) {
            if (role.equals("ROLE_PROFESOR")) {
                horarios = horarioService.getHorariosByProfesorEmail(userEmail);
                tituloBienvenida = "Profesor";
                System.out.println(" [HORARIO] Obteniendo horarios para PROFESOR.");
            } else if (role.equals("ROLE_CLIENTE")) {
                horarios = horarioService.getHorariosByClienteEmail(userEmail);
                tituloBienvenida = "Cliente";
                System.out.println(" [HORARIO] Obteniendo horarios para CLIENTE.");
            } else {
                horarios = List.of();
                System.out.println(" [HORARIO] Rol no reconocido o sin horarios asignados.");
            }
        } else {
            horarios = List.of();
            System.out.println(" [HORARIO] Usuario sin rol definido.");
        }
        
        // 3. Añade la información relevante al modelo para la vista.
        model.addAttribute("userRole", role);
        model.addAttribute("userName", nombreCompleto);
        model.addAttribute("tituloBienvenida", tituloBienvenida);
        model.addAttribute("horarios", horarios);
        System.out.println(" [VIEW] Cargados " + horarios.size() + " horarios. Retornando vista 'horario'.");
        
        // 4. Retorna la vista del horario.
        return "horario";
    }

    // Muestra el formulario para cambiar la contraseña.
    @GetMapping("/cambiar-clave")
    public String showChangePasswordForm() {
        System.out.println(" [REQUEST] Mapeando solicitud GET a /cambiar-clave. Mostrando formulario de cambio de contraseña.");
        // 1. Retorna la vista del formulario de cambio de contraseña.
        return "password";
    }

    // Procesa el cambio de contraseña del usuario logueado.
    @PostMapping("/cambiar-clave")
    public String processChangePassword(
            @RequestParam("nuevaContrasena") String nuevaContrasena,
            @RequestParam("confirmarContrasena") String confirmarContrasena,
            RedirectAttributes redirectAttributes) {

        System.out.println(" [REQUEST] Mapeando solicitud POST a /cambiar-clave. Intentando cambiar contraseña.");
        
        // 1. Obtiene el email del usuario logueado.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        
        System.out.println(" [AUTH] Procesando cambio de clave para usuario: " + userEmail);
        
        // 2. Valida la longitud de la nueva contraseña.
        if (nuevaContrasena.length() < 6) {
            System.out.println(" [VALIDATION ERROR] La nueva contraseña es demasiado corta.");
            redirectAttributes.addFlashAttribute("error", "Error: La contraseña debe tener al menos 6 caracteres.");
            return "redirect:/cambiar-clave";
        }
        
        // 3. Valida que las contraseñas coincidan.
        if (!nuevaContrasena.equals(confirmarContrasena)) {
            System.out.println(" [VALIDATION ERROR] Las contraseñas no coinciden.");
            redirectAttributes.addFlashAttribute("error", "Error: Las contraseñas no coinciden.");
            return "redirect:/cambiar-clave";
        }
        
        // 4. Llama al servicio para actualizar la contraseña.
        System.out.println(" [SERVICE] Llamando a ContraseñaService para actualizar la clave.");
        boolean exito = contrasenaService.actualizarContrasena(userEmail, nuevaContrasena);
        
        // 5. Maneja el resultado de la actualización, redirigiendo a logout si es exitoso.
        if (exito) {
            System.out.println(" [SUCCESS] Contraseña actualizada con éxito para " + userEmail + ". Redirigiendo a /logout.");
            redirectAttributes.addFlashAttribute("success", "¡Contraseña actualizada con éxito! Por favor, vuelve a iniciar sesión.");
            return "redirect:/logout";
        } else {
            System.out.println(" [FAILURE] Fallo al actualizar la contraseña para " + userEmail + ".");
            redirectAttributes.addFlashAttribute("error", "Error de seguridad: No se pudo encontrar o actualizar el usuario.");
            return "redirect:/cambiar-clave";
        }
    }
}