package com.harmony.sistema.service.validation;

import org.springframework.stereotype.Component;

import com.harmony.sistema.model.Taller;

/**
 * Componente especializado para validaciones de Taller.
 * Responsabilidad única: Validar datos de talleres antes de crear/editar.
 */
@Component
public class ValidadorTaller {

    /**
     * Valida que un taller tenga todos los datos requeridos para su creación.
     * 
     * @param taller El taller a validar
     * @throws RuntimeException si alguna validación falla
     */
    public void validarParaCreacion(Taller taller) {
        System.out.println("🔍 [VALIDADOR TALLER] Validando taller para creación.");

        validarNombre(taller.getNombre());
        validarDescripcion(taller.getDescripcion());

        System.out.println("✅ [VALIDADOR TALLER] Validaciones pasadas correctamente.");
    }

    /**
     * Valida que un taller tenga todos los datos requeridos para su edición.
     * 
     * @param taller El taller a validar
     * @throws RuntimeException si alguna validación falla
     */
    public void validarParaEdicion(Taller taller) {
        System.out.println("🔍 [VALIDADOR TALLER] Validando taller para edición.");

        if (taller.getId() == null) {
            throw new RuntimeException("El ID del taller es obligatorio para edición.");
        }

        validarNombre(taller.getNombre());
        validarDescripcion(taller.getDescripcion());

        System.out.println("✅ [VALIDADOR TALLER] Validaciones de edición pasadas correctamente.");
    }

    /**
     * Valida que el nombre del taller no esté vacío.
     */
    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println("❌ [VALIDADOR TALLER ERROR] Nombre del taller vacío.");
            throw new RuntimeException("El nombre del taller es obligatorio");
        }
    }

    /**
     * Valida que la descripción del taller no esté vacía.
     */
    private void validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            System.out.println("❌ [VALIDADOR TALLER ERROR] Descripción del taller vacía.");
            throw new RuntimeException("La descripción del taller es obligatoria");
        }
    }
}
