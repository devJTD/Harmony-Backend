package com.harmony.sistema.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity; // ¡Importación nueva!
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "horario")
public class Horario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String diasDeClase;

    private LocalTime horaInicio;
    private LocalTime horaFin;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    private int vacantesDisponibles;

    @ManyToOne
    @JoinColumn(name = "taller_id", nullable = false)
    @JsonBackReference(value = "taller-horarios")
    private Taller taller;

    @ManyToOne
    @JoinColumn(name = "profesor_id")
    @JsonIgnoreProperties("horariosImpartidos")
    private Profesor profesor;

    @OneToMany(mappedBy = "horario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "horario-inscripciones")
    private List<Inscripcion> inscripciones;
}