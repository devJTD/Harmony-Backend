package com.harmony.sistema.dto;

// Usamos Lombok para simplificar:
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatosPersonalesFormDTO {
    private String nombre; // Mapea a nombreCompleto en el cliente
    private String email;
    private String telefono;
}