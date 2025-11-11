package com.harmony.sistema.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.harmony.sistema.dto.ProfesorRegistroDTO;
import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.service.ProfesorService;

@Controller
@RequestMapping("/profesores")
public class ProfesoresController {

    @Autowired
    private ProfesorService profesorService;

    // Muestra la lista de todos los profesores registrados.
    @GetMapping
    public String profesores(Model model) {
        System.out.println(" [REQUEST] Mapeando solicitud GET a /profesores. Iniciando carga de profesores.");
        // 1. Obtiene la lista de todos los profesores del servicio.
        List<Profesor> profesores = profesorService.listarProfesores();
        System.out.println(" [SERVICE] Listado de profesores obtenido. Total: " + profesores.size());
        // 2. Agrega la lista de profesores al modelo.
        model.addAttribute("profesores", profesores);
        // 3. Agrega un DTO vacío al modelo para el formulario de registro.
        model.addAttribute("profesorRegistroDTO", new ProfesorRegistroDTO());
        // 4. Retorna la vista de profesores.
        System.out.println(" [VIEW] Retornando vista 'profesores'.");
        return "profesores";
    }
}