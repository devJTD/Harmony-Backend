package com.harmony.sistema.dto; // Asegúrate de usar el paquete correcto

/**
 * DTO para la respuesta después de una inscripción exitosa.
 * Contiene las credenciales temporales del nuevo usuario/cliente.
 */
public class InscripcionResponseDTO {

    private String correo;
    private String contrasenaTemporal;

    // --- Constructores, Getters y Setters ---

    public InscripcionResponseDTO() {}

    public InscripcionResponseDTO(String correo, String contrasenaTemporal) {
        this.correo = correo;
        this.contrasenaTemporal = contrasenaTemporal;
    }

    // Getters y Setters (Necesarios para que Spring lo serialice a JSON)

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasenaTemporal() {
        return contrasenaTemporal;
    }

    public void setContrasenaTemporal(String contrasenaTemporal) {
        this.contrasenaTemporal = contrasenaTemporal;
    }
}