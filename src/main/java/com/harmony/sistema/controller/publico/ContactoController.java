package com.harmony.sistema.controller.publico;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.harmony.sistema.dto.ContactoFormDTO;
import com.harmony.sistema.service.EmailService;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class ContactoController {

    @Autowired
    private EmailService emailService;

    @Value("${spring.mail.username}")
    private String adminEmail;

    /**
     * Endpoint REST para enviar mensajes de contacto desde Angular.
     * POST /contacto/enviar
     * Envía un correo al administrador con los datos del formulario de contacto.
     */
    @PostMapping("/contacto/enviar")
    @ResponseBody
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<Map<String, Object>> enviarContactoRest(@RequestBody ContactoFormDTO form) {
        System.out.println(" [REST REQUEST] POST a /contacto/enviar (API REST)");
        System.out.println(" [CONTACTO] Procesando mensaje de: " + form.getCorreo());
        System.out.println(" [CONTACTO] Nombre: " + form.getNombre());
        System.out.println(" [CONTACTO] Asunto: " + form.getAsunto());

        Map<String, Object> response = new HashMap<>();

        try {
            String subject = "Consulta de Contacto - " + form.getAsunto() + " (Harmony)";
            System.out.println(" [EMAIL] Preparando envío de correo");
            System.out.println(" [EMAIL] Destinatario (Admin): " + adminEmail);
            System.out.println(" [EMAIL] Asunto: " + subject);

            String body = String.format(
                    """
                            ¡Has recibido un nuevo mensaje de contacto!

                            Nombre: %s
                            Correo del cliente: %s
                            Asunto Seleccionado: %s

                            Mensaje:
                            %s
                            """,
                    form.getNombre(), form.getCorreo(), form.getAsunto(), form.getMensaje());

            emailService.enviarCorreo(adminEmail, subject, body);
            System.out.println(" [EMAIL SUCCESS] Correo de contacto enviado exitosamente a: " + adminEmail);

            response.put("success", true);
            response.put("message", "✅ ¡Mensaje enviado! Recibimos tu consulta y te responderemos a la brevedad.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println(" [EMAIL ERROR] Ocurrió un error al intentar enviar correo de contacto");
            System.err.println(" [EMAIL ERROR] Remitente: " + form.getCorreo());
            System.err.println(" [EMAIL ERROR] Detalle: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("message",
                    "❌ Ocurrió un error al intentar enviar tu mensaje. Por favor, verifica tu información o intenta más tarde.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}