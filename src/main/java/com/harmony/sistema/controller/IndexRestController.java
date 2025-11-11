package com.harmony.sistema.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController; // ⬅️ CLAVE: Convierte el retorno a JSON

import com.harmony.sistema.model.Taller;
import com.harmony.sistema.service.TallerService;

@RestController // Anotación para API REST
@RequestMapping("/api/talleres") // ⬅️ Prefijo REST simple: http://localhost:8080/api/talleres
public class IndexRestController {

    @Autowired
    private TallerService tallerService; // Usaremos la lógica de negocio existente

    // Maneja la solicitud GET y devuelve la lista de talleres activos en formato JSON.
    // Endpoint: GET http://localhost:8080/api/talleres/activos
    @GetMapping("/activos") 
    public List<Taller> getTalleresActivos() {
        System.out.println(" [API REQUEST] Solicitud GET a /api/talleres/activos. Devolviendo JSON.");
        
        // La lógica de negocio está bien y se reutiliza:
        List<Taller> talleresActivos = tallerService.encontrarTalleresActivos();
        
        // El @RestController se encarga de convertir la lista a JSON.
        return talleresActivos;
    }
    
    // NOTA: Otros métodos CRUD (POST, PUT, DELETE) se agregarían aquí si se necesitan.
}