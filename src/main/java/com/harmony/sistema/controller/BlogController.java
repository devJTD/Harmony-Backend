package com.harmony.sistema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BlogController {

    @GetMapping("/blog")
    public String blog() {
        // Log añadido
        System.out.println(" [REQUEST] Mapeando solicitud GET a /blog. Retornando vista 'blog'.");
        return "blog";
    }
}