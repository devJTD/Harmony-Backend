package com.harmony.sistema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Horario;

/**
 * Servicio especializado para el envío de notificaciones.
 * Responsabilidad única: Gestionar el envío de correos electrónicos.
 */
@Service
public class ServicioNotificacion {

    @Autowired
    private EmailService emailService;

    /**
     * Envía las credenciales de acceso al nuevo cliente.
     * 
     * @param email    Email del destinatario
     * @param nombre   Nombre del cliente
     * @param password Contraseña temporal generada
     */
    public void enviarCredenciales(String email, String nombre, String password) {
        System.out.println("📧 [SERVICIO NOTIFICACION] Enviando credenciales a: " + email);

        String asunto = "¡Bienvenido a Harmony! Tus Credenciales de Acceso";
        String cuerpo = construirMensajeBienvenida(nombre, email, password);

        try {
            emailService.enviarCorreo(email, asunto, cuerpo);
            System.out.println("✅ [SERVICIO NOTIFICACION SUCCESS] Correo de bienvenida enviado a: " + email);
        } catch (Exception e) {
            System.out.println(
                    "❌ [SERVICIO NOTIFICACION ERROR] Error al enviar correo a " + email + ": " + e.getMessage());
        }
    }

    /**
     * Envía una solicitud de baja al administrador.
     * 
     * @param cliente Cliente que solicita la baja
     * @param horario Horario del que se da de baja
     * @param motivo  Motivo de la solicitud
     */
    public void enviarSolicitudBaja(Cliente cliente, Horario horario, String motivo) {
        System.out.println("📧 [SERVICIO NOTIFICACION] Enviando solicitud de baja de: " + cliente.getNombreCompleto());

        String asunto = "Solicitud de Baja - " + cliente.getNombreCompleto();
        String cuerpo = construirMensajeBaja(cliente, horario, motivo);

        String adminEmail = "admin@harmony.com";

        try {
            emailService.enviarCorreo(adminEmail, asunto, cuerpo);
            System.out.println("✅ [SERVICIO NOTIFICACION SUCCESS] Notificación de baja enviada al admin.");
        } catch (Exception e) {
            System.out
                    .println("❌ [SERVICIO NOTIFICACION ERROR] Error al enviar notificación de baja: " + e.getMessage());
        }
    }

    /**
     * Construye el mensaje de bienvenida con las credenciales.
     */
    private String construirMensajeBienvenida(String nombre, String email, String password) {
        return String.format(
                """
                        Hola %s,

                        ¡Tu inscripción ha sido confirmada y tu cuenta ha sido creada con éxito!

                        Tus credenciales de acceso son:
                        Usuario (Correo Electrónico): %s
                        Contraseña Temporal: %s

                        Por tu seguridad, te recomendamos encarecidamente cambiar tu contraseña inmediatamente después de iniciar sesión.

                        ¡Te esperamos en clase!
                        Saludos cordiales,
                        El equipo de Harmony
                        """,
                nombre, email, password);
    }

    /**
     * Construye el mensaje de solicitud de baja.
     */
    private String construirMensajeBaja(Cliente cliente, Horario horario, String motivo) {
        return String.format("""
                Solicitud de Baja de Taller

                Cliente: %s
                Correo: %s
                Taller: %s
                Horario: %s

                Motivo de la solicitud:
                %s
                """,
                cliente.getNombreCompleto(),
                cliente.getCorreo(),
                horario.getTaller().getNombre(),
                horario.getDiasDeClase() + " " + horario.getHoraInicio() + "-" + horario.getHoraFin(),
                motivo);
    }
}
