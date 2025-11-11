// Archivo: com.harmony.sistema.dto.InscripcionFormDTO.java

package com.harmony.sistema.dto;

import java.util.List;

public class InscripcionFormDTO {

    // 1. Datos Personales
    private String nombre;
    private String email;
    private String telefono;

    // 2. Selección de Talleres (IDs de los talleres seleccionados)
    private List<Long> talleresSeleccionados;

    // 2. Selección de Horarios (Captura el ID de Horario para cada taller)
    // Spring/Thymeleaf mapeará esto:
    // Por ejemplo: 'horarioTaller1' se mapeará a un campo Long si tienes un setter genérico,
    // pero para simplicidad, en el controlador mapearemos los parámetros directamente.
    // Para una solución limpia en un DTO, necesitarías mapeadores específicos o un Map,
    // pero para el envío directo del formulario a un método de controlador, no lo necesitamos en el DTO.

    // 3. Datos de Pago (aunque no se persistan, el formulario los exige para la validación)
    // No los incluimos aquí ya que el controlador probablemente solo necesita los datos del cliente y la selección.
    
    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public List<Long> getTalleresSeleccionados() {
        return talleresSeleccionados;
    }

    public void setTalleresSeleccionados(List<Long> talleresSeleccionados) {
        this.talleresSeleccionados = talleresSeleccionados;
    }
}