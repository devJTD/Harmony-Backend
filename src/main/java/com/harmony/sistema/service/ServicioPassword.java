package com.harmony.sistema.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

/**
 * Servicio especializado para la generación de contraseñas.
 * Responsabilidad única: Generar contraseñas seguras y temporales.
 */
@Service
public class ServicioPassword {

    /**
     * Genera una contraseña temporal única.
     * Utiliza UUID para garantizar unicidad.
     * 
     * @return Contraseña temporal con formato "temporal-XXXXXXXX"
     */
    public String generarPasswordTemporal() {
        String random = UUID.randomUUID().toString().substring(0, 8);
        String password = "temporal-" + random;
        System.out.println("🔐 [SERVICIO PASSWORD] Contraseña temporal generada.");
        return password;
    }

    /**
     * Valida si una contraseña cumple con los requisitos mínimos.
     * 
     * @param password La contraseña a validar
     * @return true si la contraseña es válida, false en caso contrario
     */
    public boolean esPasswordValida(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        return true;
    }
}
