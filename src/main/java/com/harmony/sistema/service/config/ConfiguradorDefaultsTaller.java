package com.harmony.sistema.service.config;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.harmony.sistema.model.Taller;

/**
 * Componente especializado para configurar valores por defecto en talleres.
 * Responsabilidad única: Aplicar valores por defecto a talleres nuevos.
 */
@Component
public class ConfiguradorDefaultsTaller {

    /**
     * Aplica valores por defecto a un taller nuevo.
     * 
     * @param taller El taller al que se aplicarán los valores por defecto
     */
    public void aplicarValoresPorDefecto(Taller taller) {
        System.out.println("⚙️ [CONFIGURADOR DEFAULTS] Aplicando valores por defecto al taller.");

        // Precio por defecto: 0
        if (taller.getPrecio() == null) {
            taller.setPrecio(BigDecimal.ZERO);
            System.out.println("⚠️ [CONFIGURADOR DEFAULTS] Precio null, asignando 0");
        }

        // Duración por defecto: 12 semanas
        if (taller.getDuracionSemanas() == null || taller.getDuracionSemanas() <= 0) {
            taller.setDuracionSemanas(12);
            System.out.println("⚠️ [CONFIGURADOR DEFAULTS] Duración semanas inválida, asignando 12");
        }

        // Clases por semana por defecto: 2
        if (taller.getClasesPorSemana() == null || taller.getClasesPorSemana() <= 0) {
            taller.setClasesPorSemana(2);
            System.out.println("⚠️ [CONFIGURADOR DEFAULTS] Clases por semana inválidas, asignando 2");
        }

        // Marcar como activo por defecto
        taller.setActivo(true);

        System.out.println("✅ [CONFIGURADOR DEFAULTS] Valores por defecto aplicados:");
        System.out.println("   - Precio: " + taller.getPrecio());
        System.out.println("   - Duración: " + taller.getDuracionSemanas() + " semanas");
        System.out.println("   - Clases/semana: " + taller.getClasesPorSemana());
        System.out.println("   - Activo: " + taller.isActivo());
    }
}
