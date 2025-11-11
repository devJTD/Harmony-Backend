package com.harmony.sistema.dto; // O un paquete 'dto' si lo prefieres

import java.util.List;

import com.harmony.sistema.model.Horario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TallerResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String imagenTaller; // Mantiene el nombre de campo original
    private Integer duracionSemanas;
    private Integer clasesPorSemana;
    private Double precio; // Usamos Double para que se mapee fácilmente a 'number' en Angular
    private String temas;
    
    // Lista de horarios abiertos/disponibles
    private List<Horario> horariosAbiertos;
    
    // Indicador si el taller alguna vez tuvo horarios definidos
    private boolean tieneHorariosDefinidos;
}