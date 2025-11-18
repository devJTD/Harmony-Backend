package com.harmony.sistema.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Noticia {
    private Long id; // Identificador único
    private String titulo; // El título de la noticia
    private String contenido; // Texto principal de la noticia
    private String imagenUrl; // URL de la imagen asociada
}
