package com.harmony.sistema.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.harmony.sistema.model.Taller;
import com.harmony.sistema.service.TallerService;

@Controller
public class IndexController {

    @Autowired
    private TallerService tallerService;

    // Maneja la solicitud de la página de inicio ("/") y carga la lista de talleres activos.
    @GetMapping("/")
    public String index(Model model) {
        System.out.println(" [REQUEST] Mapeando solicitud GET a /. Cargando talleres activos para la página de inicio.");
        // 1. Obtiene la lista de todos los talleres que están marcados como activos.
        List<Taller> talleresActivos = tallerService.encontrarTalleresActivos();
        System.out.println(" [SERVICE] Talleres activos encontrados: " + talleresActivos.size());
        // 2. Agrega la lista de talleres activos al modelo para que la vista pueda acceder a ellos.
        model.addAttribute("talleres", talleresActivos);
        // 3. Retorna el nombre de la plantilla de la vista (index.html, por ejemplo).
        System.out.println(" [VIEW] Retornando vista 'index'.");
        return "index";
    }
}