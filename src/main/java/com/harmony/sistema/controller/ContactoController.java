package com.harmony.sistema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.harmony.sistema.dto.ContactoFormDTO;
import com.harmony.sistema.service.EmailService;

@Controller
public class ContactoController {

    @Autowired
    private EmailService emailService;

    // Obtener el correo del administrador desde application.properties (o similar)
    @Value("${spring.mail.username}") // Asume que el correo del admin está configurado como el remitente principal
    private String adminEmail;

    // Muestra la página del formulario de contacto.
    @GetMapping("/contacto")
    public String contacto() {
        System.out.println(" [REQUEST] Mapeando solicitud GET a /contacto. Retornando vista 'contacto'.");
        // 1. Retorna el nombre de la vista (contacto.html).
        return "contacto";
    }

    // Procesa el envío del formulario de contacto y envía un correo al administrador.
    @PostMapping("/contacto/enviar")
    public String enviarContacto(@ModelAttribute ContactoFormDTO form, RedirectAttributes redirectAttributes) {
        System.out.println(" [REQUEST] Mapeando solicitud POST a /contacto/enviar. Procesando mensaje de: " + form.getCorreo());
        try {
            // 1. Construye el asunto del correo a partir del DTO.
            String subject = "Consulta de Contacto - " + form.getAsunto() + " (Harmony)";
            System.out.println(" [EMAIL] Asunto del correo a enviar a Admin: " + subject);
            
            // 2. Construye el cuerpo del mensaje que recibirá el administrador usando Text Blocks.
            String body = String.format(
                """
                ¡Has recibido un nuevo mensaje de contacto!

                Nombre: %s
                Correo del cliente: %s
                Asunto Seleccionado: %s

                Mensaje:
                %s
                """,
                form.getNombre(), form.getCorreo(), form.getAsunto(), form.getMensaje()
            );
            
            // 3. Llama al servicio para enviar el correo al administrador.
            emailService.enviarCorreo(adminEmail, subject, body);
            System.out.println(" [EMAIL SUCCESS] Correo de contacto enviado exitosamente a: " + adminEmail);

            // 4. Agrega un mensaje de éxito para mostrar en la redirección.
            redirectAttributes.addFlashAttribute("success", "✅ ¡Mensaje enviado! Recibimos tu consulta y te responderemos a la brevedad.");

        } catch (Exception e) {
            // 5. Loggea el error para la depuración (ahora con sout).
            System.out.println(" [EMAIL ERROR] Ocurrió un error al intentar enviar correo de contacto de: " + form.getCorreo() + ". Detalle: " + e.getMessage());
            
            // 6. Agrega un mensaje de error para mostrar en la redirección.
            redirectAttributes.addFlashAttribute("error", "❌ Ocurrió un error al intentar enviar tu mensaje. Por favor, verifica tu información o intenta más tarde.");
        }

        // 7. Redirige de vuelta a la página de contacto.
        System.out.println(" [REDIRECT] Finalizado el procesamiento de contacto. Redirigiendo a /contacto.");
        return "redirect:/contacto";
    }
}