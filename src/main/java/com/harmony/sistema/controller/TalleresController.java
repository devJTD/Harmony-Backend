package com.harmony.sistema.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Taller;
import com.harmony.sistema.service.HorarioService;
import com.harmony.sistema.service.TallerService;

@Controller
public class TalleresController {

    @Autowired
    private TallerService tallerService;
    @Autowired
    private HorarioService horarioService;

    // Muestra la vista pública de talleres, cargando la información de talleres activos y sus horarios disponibles.
    @GetMapping("/talleres")
    public String talleres(Model model) {
        System.out.println(" [REQUEST] Mapeando solicitud GET a /talleres. Iniciando carga de talleres y horarios.");

        // 1. Obtiene la lista de todos los talleres que están activos.
        List<Taller> talleres = tallerService.encontrarTalleresActivos();
        System.out.println(" [SERVICE] Talleres activos encontrados: " + talleres.size());
        
        // 2. Crea un mapa donde la clave es el ID del Taller y el valor es la lista de horarios 'abiertos' (con vacantes y no iniciados).
        System.out.println(" [SERVICE] Mapeando horarios abiertos por Taller ID.");
        Map<Long, List<Horario>> horariosAbiertosPorTaller = talleres.stream()
                .collect(Collectors.toMap(
                        Taller::getId,
                        taller -> horarioService.getHorariosAbiertosByTallerId(taller.getId())
                ));
        
        // 3. Crea un mapa para indicar si cada taller tiene al menos un horario definido (abierto o cerrado).
        System.out.println(" [SERVICE] Verificando talleres con horarios definidos.");
        Map<Long, Boolean> tieneHorariosDefinidos = talleres.stream()
                .collect(Collectors.toMap(
                        Taller::getId,
                        taller -> !horarioService.getAllHorariosByTallerId(taller.getId()).isEmpty()
                ));

        // 4. Agrega los talleres, el mapa de horarios abiertos y el mapa de horarios definidos al modelo.
        model.addAttribute("talleres", talleres);
        model.addAttribute("horariosAbiertosPorTaller", horariosAbiertosPorTaller);
        model.addAttribute("tieneHorariosDefinidos", tieneHorariosDefinidos);
        System.out.println(" [DATA] Información de talleres y horarios agregada al modelo.");

        // 5. Retorna el nombre de la vista.
        System.out.println(" [VIEW] Retornando vista 'talleres'.");
        return "talleres";
    }
}