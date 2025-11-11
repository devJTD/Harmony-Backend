package com.harmony.sistema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AcercaController {

    @GetMapping("/acerca")
    public String acercaDeNosotros() {
        // Log añadido
        System.out.println(" [REQUEST] Mapeando solicitud GET a /acerca. Retornando vista 'acerca'.");
        return "acerca";
    }
}