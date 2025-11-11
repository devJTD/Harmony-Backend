package com.harmony.sistema.dto; // Asegúrate de usar el paquete correcto

/**
 * DTO para el detalle de Taller/Horario seleccionado dentro del payload.
 */
public class InscripcionDetalleDTO {

    private Long tallerId;
    private Long horarioId;

    // --- Constructor, Getters y Setters ---
    public InscripcionDetalleDTO() {}

    // Getters y Setters (Necesarios para el mapeo)

    public Long getTallerId() {
        return tallerId;
    }

    public void setTallerId(Long tallerId) {
        this.tallerId = tallerId;
    }

    public Long getHorarioId() {
        return horarioId;
    }

    public void setHorarioId(Long horarioId) {
        this.horarioId = horarioId;
    }
}