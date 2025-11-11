package com.harmony.sistema.dto; // Asegúrate de usar el paquete correcto

import java.util.List;

/**
 * DTO para la data de entrada del formulario de inscripción.
 * Coincide con la estructura del payload JSON de Angular.
 */
public class InscripcionPayloadDTO {

    // Datos Personales
    private String nombre;
    private String email;
    private String telefono;
    

    // Datos de Pago (Planos en el Payload)
    private String numeroTarjeta;
    private String fechaVencimiento;
    private String cvv;

    // Lista de Talleres e Horarios seleccionados
    private List<InscripcionDetalleDTO> inscripciones;

    // --- Constructor, Getters y Setters ---
    public InscripcionPayloadDTO() {}

    // Getters y Setters (Necesarios para que Spring mapee el JSON)

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

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public List<InscripcionDetalleDTO> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(List<InscripcionDetalleDTO> inscripciones) {
        this.inscripciones = inscripciones;
    }
}