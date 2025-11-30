package com.harmony.sistema.controller.publico;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harmony.sistema.model.Taller;
import com.harmony.sistema.service.TallerService;

@RestController
@RequestMapping("/api/talleres")
@CrossOrigin(origins = "http://localhost:4200")
public class IndexRestController {

    @Autowired
    private TallerService tallerService;

    /**
     * Endpoint para obtener talleres activos.
     * GET /api/talleres/activos
     */
    @GetMapping("/activos")
    public List<Taller> getTalleresActivos() {
        System.out.println(" [API REQUEST] Solicitud GET a /api/talleres/activos. Devolviendo JSON.");
        List<Taller> talleresActivos = tallerService.encontrarTalleresActivos();
        return talleresActivos;
    }
}