package com.harmony.sistema.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.harmony.sistema.model.Horario;
import com.harmony.sistema.repository.HorarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HorarioScheduler {

    private final HorarioRepository horarioRepository;

    // Se ejecuta al iniciar la aplicación
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void verificarHorariosFinalizados() {
        System.out.println("🕒 [SCHEDULER] Verificando horarios finalizados...");

        // Buscamos horarios no finalizados cuya fecha de fin sea anterior a hoy
        List<Horario> horariosVencidos = horarioRepository.findByFinalizadoFalseAndFechaFinBefore(LocalDate.now());

        if (!horariosVencidos.isEmpty()) {
            horariosVencidos.forEach(horario -> {
                horario.setFinalizado(true);
                System.out.println("✅ Horario ID " + horario.getId() + " finalizado autom\u00e1ticamente. Fecha fin: "
                        + horario.getFechaFin());
            });
            horarioRepository.saveAll(horariosVencidos);
            System.out.println("🔄 Total de horarios actualizados: " + horariosVencidos.size());
        } else {
            System.out.println("ℹ\ufe0f No se encontraron horarios vencidos pendientes de finalizar.");
        }
    }
}
