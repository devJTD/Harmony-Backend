package com.harmony.sistema.controller.publico;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin; // Importar para CORS
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.service.ProfesorService;

@RestController // ⬅️ IMPORTANTE: Devuelve JSON.
@RequestMapping("/api/profesores") // ⬅️ Endpoint que Angular llamará.
// 🚨 CRUCIAL PARA LA COMUNICACIÓN: Permite peticiones desde Angular
// (típicamente 4200)
@CrossOrigin(origins = "http://localhost:4200")
public class ProfesorPublicController {

    @Autowired
    private ProfesorService profesorService;

    @GetMapping
    public List<Profesor> listarProfesores() {
        System.out.println(" [REST REQUEST] Solicitud GET a /api/profesores. Devolviendo lista de profesores.");
        return profesorService.listarProfesores();
    }
}