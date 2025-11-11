package com.harmony.sistema.dto;

import java.util.List;

public class ClienteRegistroDTO {

    private String nombreCompleto;
    private String correo;
    private String telefono;
    private List<Long> talleresSeleccionados;
    
    // CONSTRUCTOR REQUERIDO PARA RESOLVER EL ERROR INICIAL
    public ClienteRegistroDTO(String nombreCompleto, String correo, String telefono) {
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.telefono = telefono;
    }

    public ClienteRegistroDTO() {
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
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