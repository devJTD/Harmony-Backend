package com.harmony.sistema.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Profesor;
import com.harmony.sistema.model.Taller;
import com.harmony.sistema.repository.HorarioRepository;

import jakarta.transaction.Transactional;

@Service
public class HorarioService {

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private TallerService tallerService;

    @Autowired
    private ProfesorService profesorService;

    // ✅ CORREGIDO: Asegurar que el profesor esté cargado
    public List<Horario> getHorariosByProfesorEmail(String profesorEmail) {
        System.out.println("[INFO] [HORARIO] Buscando horarios asignados al profesor con email.");
        List<Horario> horarios = horarioRepository.findByProfesorUserEmail(profesorEmail);
        // Forzar carga del profesor
        horarios.forEach(h -> {
            if (h.getProfesor() != null) {
                h.getProfesor().getNombreCompleto();
            }
        });
        return horarios;
    }

    // ✅ CORREGIDO: Asegurar que el profesor esté cargado
    public List<Horario> getHorariosByClienteEmail(String clienteEmail) {
        System.out.println("[INFO] [HORARIO] Buscando horarios de talleres inscritos por el cliente con email.");
        List<Horario> horarios = horarioRepository.findByInscripcionesClienteUserEmail(clienteEmail);
        // Forzar carga del profesor
        horarios.forEach(h -> {
            if (h.getProfesor() != null) {
                h.getProfesor().getNombreCompleto();
            }
        });
        return horarios;
    }

    // ✅ CORREGIDO: Asegurar que el profesor esté cargado
    public List<Horario> getHorariosAbiertosByTallerId(Long tallerId) {
        System.out.println(
                "[INFO] [HORARIO] Buscando horarios abiertos (futuros y con vacantes) para Taller ID: " + tallerId);
        List<Horario> horarios = horarioRepository.findByTallerIdAndFechaInicioAfterAndVacantesDisponiblesGreaterThan(
                tallerId, LocalDate.now(), 0);
        // Forzar carga del profesor y taller
        horarios.forEach(h -> {
            if (h.getProfesor() != null) {
                h.getProfesor().getNombreCompleto();
            }
            if (h.getTaller() != null) {
                h.getTaller().getNombre();
            }
        });
        return horarios;
    }

    // ✅ CORREGIDO: Asegurar que el profesor esté cargado
    public List<Horario> getAllHorariosByTallerId(Long tallerId) {
        System.out.println("[INFO] [HORARIO] Buscando todos los horarios para Taller ID: " + tallerId);
        List<Horario> horarios = horarioRepository.findByTallerId(tallerId);
        // Forzar carga del profesor
        horarios.forEach(h -> {
            if (h.getProfesor() != null) {
                h.getProfesor().getNombreCompleto();
            }
        });
        return horarios;
    }

    // ✅ CORREGIDO: Asegurar que el profesor esté cargado
    public Horario getHorarioById(Long id) {
        System.out.println("[INFO] [HORARIO] Buscando horario por ID: " + id);
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado con ID: " + id));
        // Forzar carga del profesor
        if (horario.getProfesor() != null) {
            horario.getProfesor().getNombreCompleto();
        }
        return horario;
    }

    @Transactional
    public Horario crearHorario(Long tallerId, Long profesorId, String diasDeClase,
            LocalTime horaInicio, LocalTime horaFin, LocalDate fechaInicio,
            int vacantesDisponibles) {
        System.out.println("[INFO] [HORARIO] Iniciando creación de nuevo horario.");
        Taller taller = tallerService.obtenerTallerPorId(tallerId);
        Profesor profesor = profesorService.obtenerProfesorPorId(profesorId);

        if (taller == null) {
            System.err.println("[ERROR] [HORARIO] Taller no encontrado con ID: " + tallerId);
            throw new RuntimeException("Taller no encontrado con ID: " + tallerId);
        }
        if (profesor == null) {
            System.err.println("[ERROR] [HORARIO] Profesor no encontrado con ID: " + profesorId);
            throw new RuntimeException("Profesor no encontrado con ID: " + profesorId);
        }
        System.out.println("[SUCCESS] [HORARIO] Taller y Profesor validados.");

        Optional<Horario> conflicto = horarioRepository.findByTallerAndProfesorAndDiasDeClaseAndHoraInicioAndHoraFin(
                taller, profesor, diasDeClase, horaInicio, horaFin);

        if (conflicto.isPresent()) {
            System.err.println("[ERROR] [HORARIO] Conflicto de unicidad detectado.");
            throw new RuntimeException("Ya existe un horario idéntico asignado a este profesor y taller.");
        }
        System.out.println("[SUCCESS] [HORARIO] Validación de unicidad completada (no hay conflicto).");

        // ✅ CÁLCULO AUTOMÁTICO DE FECHA FIN
        // Fecha Fin = Fecha Inicio + (Duración en Semanas * 7 días)
        // Restamos 1 día si queremos que termine exactamente al final de la última
        // semana (opcional, pero común)
        // Aquí usaremos la lógica simple: inicio + duración
        int diasDuracion = taller.getDuracionSemanas() * 7;
        LocalDate fechaFinCalculada = fechaInicio.plusDays(diasDuracion);

        Horario nuevoHorario = Horario.builder()
                .taller(taller)
                .profesor(profesor)
                .diasDeClase(diasDeClase)
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFinCalculada) // ✅ Asignamos la fecha calculada
                .vacantesDisponibles(vacantesDisponibles)
                .build();

        Horario savedHorario = horarioRepository.save(nuevoHorario);
        System.out.println("[SUCCESS] [HORARIO] Horario creado y guardado con ID: " + savedHorario.getId());
        return savedHorario;
    }

    @Transactional
    public Horario editarHorario(Long horarioId, Long profesorId, String diasDeClase,
            LocalTime horaInicio, LocalTime horaFin, LocalDate fechaInicio,
            int vacantesDisponibles) {
        System.out.println("[INFO] [HORARIO] Iniciando edición de horario con ID: " + horarioId);

        Horario horario = horarioRepository.findById(horarioId)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado con ID: " + horarioId));
        System.out.println("[SUCCESS] [HORARIO] Horario encontrado.");

        Profesor nuevoProfesor = profesorService.obtenerProfesorPorId(profesorId);

        if (nuevoProfesor == null) {
            System.err.println("[ERROR] [HORARIO] Profesor no encontrado con ID: " + profesorId);
            throw new RuntimeException("Profesor no encontrado con ID: " + profesorId);
        }
        System.out.println("[SUCCESS] [HORARIO] Nuevo Profesor validado.");

        // ✅ CÁLCULO AUTOMÁTICO DE FECHA FIN EN EDICIÓN
        int diasDuracion = horario.getTaller().getDuracionSemanas() * 7;
        LocalDate fechaFinCalculada = fechaInicio.plusDays(diasDuracion);

        horario.setProfesor(nuevoProfesor);
        horario.setDiasDeClase(diasDeClase);
        horario.setHoraInicio(horaInicio);
        horario.setHoraFin(horaFin);
        horario.setFechaInicio(fechaInicio);
        horario.setFechaFin(fechaFinCalculada); // ✅ Actualizamos fecha fin
        horario.setVacantesDisponibles(vacantesDisponibles);
        System.out.println("[INFO] [HORARIO] Campos del horario actualizados.");

        Horario updatedHorario = horarioRepository.save(horario);
        System.out.println(
                "[SUCCESS] [HORARIO] Horario ID " + horarioId + " modificado y guardado exitosamente.");
        return updatedHorario;
    }

    @Transactional
    public void eliminarHorario(Long horarioId) {
        System.out.println("[INFO] [HORARIO] Iniciando eliminación de horario con ID: " + horarioId);
        if (!horarioRepository.existsById(horarioId)) {
            System.err.println("[ERROR] [HORARIO] Horario no encontrado con ID: " + horarioId);
            throw new RuntimeException("Horario no encontrado con ID: " + horarioId);
        }

        horarioRepository.deleteById(horarioId);
        System.out.println("[SUCCESS] [HORARIO] Horario ID " + horarioId + " eliminado exitosamente.");
    }
}
