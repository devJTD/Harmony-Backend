package com.harmony.sistema.dto;

/**
 * Data Transfer Object (DTO) utilizado para pasar el correo electrónico
 * y la contraseña temporal (sin cifrar) del servicio al controlador,
 * justo después del registro.
 */
public class CredencialesDTO {

    private String correo;
    private String contrasenaTemporal;

    // Constructor vacío
    public CredencialesDTO() {}
    
    // Constructor con campos
    public CredencialesDTO(String correo, String contrasenaTemporal) {
        this.correo = correo;
        this.contrasenaTemporal = contrasenaTemporal;
    }

    // Getters
    public String getCorreo() {
        return correo;
    }

    public String getContrasenaTemporal() {
        return contrasenaTemporal;
    }

    // Setters (opcionales, pero se mantienen por convención)
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setContrasenaTemporal(String contrasenaTemporal) {
        this.contrasenaTemporal = contrasenaTemporal;
    }
}