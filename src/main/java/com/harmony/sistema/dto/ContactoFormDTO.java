package com.harmony.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactoFormDTO {
    private String nombre;
    private String correo;
    private String asunto;
    private String mensaje;
    
}