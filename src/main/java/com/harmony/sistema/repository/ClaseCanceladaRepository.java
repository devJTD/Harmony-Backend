package com.harmony.sistema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.harmony.sistema.model.ClaseCancelada;

@Repository
public interface ClaseCanceladaRepository extends JpaRepository<ClaseCancelada, Long> {
    // Busca y devuelve una lista de clases canceladas basándose en el ID del
    // horario asociado.
    List<ClaseCancelada> findByHorario_Id(Long horarioId);
}
