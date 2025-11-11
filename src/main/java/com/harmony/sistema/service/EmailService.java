package com.harmony.sistema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Crea y envía un correo electrónico simple utilizando el JavaMailSender.
    public void enviarCorreo(String para, String asunto, String cuerpo) {
        System.out.println(" [EMAIL SERVICE] Iniciando proceso de envío de correo.");
        // 1. Crea un nuevo objeto SimpleMailMessage.
        SimpleMailMessage mensaje = new SimpleMailMessage();
        System.out.println(" [EMAIL SERVICE] Objeto SimpleMailMessage creado.");
        // 2. Establece el destinatario.
        mensaje.setTo(para);
        System.out.println(" [EMAIL SERVICE] Destinatario establecido: " + para);
        // 3. Establece el asunto.
        mensaje.setSubject(asunto);
        System.out.println(" [EMAIL SERVICE] Asunto establecido: " + asunto);
        // 4. Establece el cuerpo del mensaje.
        mensaje.setText(cuerpo);
        // 5. Envía el mensaje utilizando el JavaMailSender.
        try {
            mailSender.send(mensaje);
            System.out.println(" [EMAIL SERVICE SUCCESS] Correo enviado exitosamente a: " + para);
        } catch (MailException e) {
            System.out.println(" [EMAIL SERVICE ERROR] Fallo al enviar correo a: " + para + ". Error: " + e.getMessage());
        }
    }
}