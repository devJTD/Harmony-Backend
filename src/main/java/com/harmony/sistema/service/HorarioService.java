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

    // Obtiene los horarios asignados a un Profesor buscando por su email asociado en la tabla User.
    public List<Horario> getHorariosByProfesorEmail(String profesorEmail) {
        System.out.println(" [HORARIO SERVICE] Buscando horarios asignados al profesor con email.");
        return horarioRepository.findByProfesorUserEmail(profesorEmail);
    }

    // Obtiene los horarios de los talleres en los que un Cliente está inscrito, buscando por su email asociado en la tabla User.
    public List<Horario> getHorariosByClienteEmail(String clienteEmail) {
        System.out.println(" [HORARIO SERVICE] Buscando horarios de talleres inscritos por el cliente con email.");
        return horarioRepository.findByInscripcionesClienteUserEmail(clienteEmail);
    }
    
    // Obtiene todos los horarios de un taller que aún no han comenzado y tienen vacantes disponibles.
    public List<Horario> getHorariosAbiertosByTallerId(Long tallerId) {
        System.out.println(" [HORARIO SERVICE] Buscando horarios abiertos (futuros y con vacantes) para Taller ID: " + tallerId);
        return horarioRepository.findByTallerIdAndFechaInicioAfterAndVacantesDisponiblesGreaterThan(
            tallerId, LocalDate.now(), 0);
    }

    // Obtiene todos los horarios asociados a un taller por su ID.
    public List<Horario> getAllHorariosByTallerId(Long tallerId) {
        System.out.println(" [HORARIO SERVICE] Buscando todos los horarios para Taller ID: " + tallerId);
        return horarioRepository.findByTallerId(tallerId);
    }

    // Obtiene un horario por ID, lanzando una excepción si no lo encuentra.
    public Horario getHorarioById(Long id) {
        System.out.println(" [HORARIO SERVICE] Buscando horario por ID: " + id);
        return horarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Horario no encontrado con ID: " + id));
    }

    // Crea y guarda un nuevo horario, validando la existencia de Taller/Profesor y la unicidad del horario.
    @Transactional
    public Horario crearHorario(Long tallerId, Long profesorId, String diasDeClase, 
                                 LocalTime horaInicio, LocalTime horaFin, LocalDate fechaInicio, 
                                 int vacantesDisponibles) {
        System.out.println(" [HORARIO SERVICE] Iniciando creación de nuevo horario.");
        Taller taller = tallerService.obtenerTallerPorId(tallerId); 
        Profesor profesor = profesorService.obtenerProfesorPorId(profesorId); 

        // 1. Valida existencia de Taller y Profesor.
        if (taller == null) {
            System.out.println(" [HORARIO SERVICE ERROR] Taller no encontrado con ID: " + tallerId);
            throw new RuntimeException("Taller no encontrado con ID: " + tallerId);
        }
        if (profesor == null) {
            System.out.println(" [HORARIO SERVICE ERROR] Profesor no encontrado con ID: " + profesorId);
            throw new RuntimeException("Profesor no encontrado con ID: " + profesorId);
        }
        System.out.println(" [HORARIO SERVICE] Taller y Profesor validados.");

        // 2. Valida unicidad: Mismo Taller, Profesor, Días y Horas.
        Optional<Horario> conflicto = horarioRepository.findByTallerAndProfesorAndDiasDeClaseAndHoraInicioAndHoraFin(
            taller, profesor, diasDeClase, horaInicio, horaFin);

        if (conflicto.isPresent()) {
            System.out.println(" [HORARIO SERVICE ERROR] Conflicto de unicidad detectado.");
            throw new RuntimeException("Ya existe un horario idéntico asignado a este profesor y taller.");
        }
        System.out.println(" [HORARIO SERVICE] Validación de unicidad completada (no hay conflicto).");
        
        // 3. Construye el nuevo objeto Horario.
        Horario nuevoHorario = Horario.builder()
            .taller(taller)
            .profesor(profesor)
            .diasDeClase(diasDeClase)
            .horaInicio(horaInicio)
            .horaFin(horaFin)
            .fechaInicio(fechaInicio)
            .vacantesDisponibles(vacantesDisponibles)
            .build();
            
        // 4. Guarda y retorna el nuevo horario.
        Horario savedHorario = horarioRepository.save(nuevoHorario);
        System.out.println(" [HORARIO SERVICE SUCCESS] Horario creado y guardado con ID: " + savedHorario.getId());
        return savedHorario;
    }
    
    // Modifica un horario existente, actualizando todos sus atributos.
    @Transactional
    public Horario editarHorario(Long horarioId, Long profesorId, String diasDeClase, 
                                 LocalTime horaInicio, LocalTime horaFin, LocalDate fechaInicio, 
                                 int vacantesDisponibles) {
        System.out.println(" [HORARIO SERVICE] Iniciando edición de horario con ID: " + horarioId);

        // 1. Busca el Horario existente.
        Horario horario = horarioRepository.findById(horarioId)
            .orElseThrow(() -> new RuntimeException("Horario no encontrado con ID: " + horarioId));
        System.out.println(" [HORARIO SERVICE] Horario encontrado.");
            
        // 2. Busca el Profesor.
        Profesor nuevoProfesor = profesorService.obtenerProfesorPorId(profesorId); 

        if (nuevoProfesor == null) {
            System.out.println(" [HORARIO SERVICE ERROR] Profesor no encontrado con ID: " + profesorId);
            throw new RuntimeException("Profesor no encontrado con ID: " + profesorId);
        }
        System.out.println(" [HORARIO SERVICE] Nuevo Profesor validado.");

        // 3. Actualiza los campos del horario.
        horario.setProfesor(nuevoProfesor);
        horario.setDiasDeClase(diasDeClase);
        horario.setHoraInicio(horaInicio);
        horario.setHoraFin(horaFin);
        horario.setFechaInicio(fechaInicio);
        horario.setVacantesDisponibles(vacantesDisponibles); 
        System.out.println(" [HORARIO SERVICE] Campos del horario actualizados.");

        // 4. Guarda y retorna el horario modificado.
        Horario updatedHorario = horarioRepository.save(horario);
        System.out.println(" [HORARIO SERVICE SUCCESS] Horario ID " + horarioId + " modificado y guardado exitosamente.");
        return updatedHorario;
    }

    // Elimina permanentemente un horario por su ID.
    @Transactional
    public void eliminarHorario(Long horarioId) {
        System.out.println(" [HORARIO SERVICE] Iniciando eliminación de horario con ID: " + horarioId);
        // 1. Verifica si el horario existe.
        if (!horarioRepository.existsById(horarioId)) {
            System.out.println(" [HORARIO SERVICE ERROR] Horario no encontrado con ID: " + horarioId);
            throw new RuntimeException("Horario no encontrado con ID: " + horarioId);
        }
        
        // 2. Elimina el horario por ID.
        horarioRepository.deleteById(horarioId);
        System.out.println(" [HORARIO SERVICE SUCCESS] Horario ID " + horarioId + " eliminado exitosamente.");
    }
}
