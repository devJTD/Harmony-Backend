package com.harmony.sistema.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harmony.sistema.dto.TallerResponseDTO;
import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Taller;
import com.harmony.sistema.service.HorarioService;
import com.harmony.sistema.service.TallerService;

/**
 * REST Controller para exponer datos de Talleres para el frontend de Angular.
 */
@RestController
@RequestMapping("/api/talleres")
public class TallerRestController {

    @Autowired
    private TallerService tallerService;
    
    @Autowired
    private HorarioService horarioService;

    /**
     * Endpoint para obtener la lista de talleres activos con sus horarios abiertos.
     * * @return Lista de objetos Taller, cada uno enriquecido con su lista de horariosAbiertos 
     * y el indicador tieneHorariosDefinidos.
     */
    @GetMapping("/detallados/activos") // Nuevo endpoint para la página de talleres
    public List<TallerResponseDTO> getTalleresDetalladosActivos() {
        System.out.println(" [REST] Mapeando solicitud GET a /api/talleres/detallados/activos.");

        // 1. Obtener talleres activos
        List<Taller> talleres = tallerService.encontrarTalleresActivos();

        // 2. Crear los DTOs de respuesta
        List<TallerResponseDTO> dtoList = talleres.stream()
            .map(taller -> {
                // Obtener horarios abiertos para el taller
                List<Horario> horariosAbiertos = horarioService.getHorariosAbiertosByTallerId(taller.getId());
                
                // Verificar si tiene al menos un horario definido (abierto o cerrado/pasado)
                boolean tieneHorariosDefinidos = !horarioService.getAllHorariosByTallerId(taller.getId()).isEmpty();

                // Crear el DTO de respuesta
                return new TallerResponseDTO(
                    taller.getId(),
                    taller.getNombre(),
                    taller.getDescripcion(),
                    taller.getImagenTaller(),
                    taller.getDuracionSemanas(),
                    taller.getClasesPorSemana(),
                    taller.getPrecio().doubleValue(), // Convertir BigDecimal a double para JSON (Angular 'number')
                    taller.getTemas(),
                    horariosAbiertos,
                    tieneHorariosDefinidos
                );
            })
            .collect(Collectors.toList());

        System.out.println(" [REST SUCCESS] Retornando " + dtoList.size() + " talleres detallados.");
        return dtoList;
    }
}