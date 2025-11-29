package com.harmony.sistema.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginForm() {
        System.out.println(" [REQUEST] Mapeando solicitud GET a /login. Retornando vista del formulario 'login'.");
        return "login";
    }

    @PostMapping("/login")
    public String processLogin() {
        System.out.println(" [REQUEST] Mapeando solicitud POST a /login. Spring Security procesará la autenticación.");
        return "redirect:/horario";
    }
}