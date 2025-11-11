package com.harmony.sistema.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmony.sistema.model.Taller;
import com.harmony.sistema.repository.TallerRepository;

@Service
public class TallerService {
    @Autowired
    private TallerRepository tallerRepository;

    // 1. Retorna una lista de todos los talleres cuya propiedad 'activo' es verdadera.
    public List<Taller> encontrarTalleresActivos() {
        System.out.println(" [TALLER SERVICE] Buscando todos los talleres activos.");
        return tallerRepository.findByActivoTrue();
    }

    // 1. Retorna una lista de todos los talleres almacenados en la base de datos.
    public List<Taller> listarTalleres() {
        System.out.println(" [TALLER SERVICE] Listando todos los talleres (activos e inactivos).");
        return tallerRepository.findAll();
    }

    // 1. Establece el taller como activo y lo guarda en la base de datos.
    @Transactional
    public Taller crearTallerSolo(Taller taller) {
        System.out.println(" [TALLER SERVICE] Iniciando creación de un nuevo taller.");
        taller.setActivo(true);
        System.out.println(" [TALLER SERVICE] Estableciendo el taller como activo.");
        // Los demás campos (nombre, descripcion, precio, temas, etc.) ya están cargados por @ModelAttribute
        Taller nuevoTaller = tallerRepository.save(taller);
        System.out.println(" [TALLER SERVICE SUCCESS] Taller creado y guardado con ID: " + nuevoTaller.getId());
        return nuevoTaller;
    }

    // 1. Obtiene un Taller existente por su ID, actualiza sus campos con los valores del DTO recibido, y lo guarda.
    @Transactional
    public Taller editarTaller(Taller tallerActualizado) { 
        System.out.println(" [TALLER SERVICE] Iniciando edición de taller con ID: " + tallerActualizado.getId());
        // 1. Verifica si el taller existe, si no, lanza una excepción.
        Optional<Taller> tallerOpt = tallerRepository.findById(tallerActualizado.getId());
        
        if (tallerOpt.isEmpty()) {
            System.out.println(" [TALLER SERVICE ERROR] Taller no encontrado con ID: " + tallerActualizado.getId());
            throw new RuntimeException("Taller con ID " + tallerActualizado.getId() + " no encontrado.");
        }
        
        Taller tallerExistente = tallerOpt.get();
        System.out.println(" [TALLER SERVICE] Taller existente encontrado.");
        
        // 2. Actualiza los campos principales del taller existente.
        tallerExistente.setNombre(tallerActualizado.getNombre());
        tallerExistente.setDescripcion(tallerActualizado.getDescripcion());
        tallerExistente.setDuracionSemanas(tallerActualizado.getDuracionSemanas());
        tallerExistente.setClasesPorSemana(tallerActualizado.getClasesPorSemana());
        tallerExistente.setImagenTaller(tallerActualizado.getImagenTaller());
        tallerExistente.setImagenInicio(tallerActualizado.getImagenInicio());
        tallerExistente.setPrecio(tallerActualizado.getPrecio());
        tallerExistente.setActivo(tallerActualizado.isActivo());
        
        // 3. Actualiza el campo temas.
        tallerExistente.setTemas(tallerActualizado.getTemas());
        System.out.println(" [TALLER SERVICE] Campos del taller actualizados.");

        // 4. Guarda y retorna el taller actualizado.
        Taller updatedTaller = tallerRepository.save(tallerExistente);
        System.out.println(" [TALLER SERVICE SUCCESS] Taller con ID " + updatedTaller.getId() + " actualizado y guardado.");
        return updatedTaller;
    }
    
    // 1. Busca un Taller por ID y retorna el resultado, o lanza una excepción si no lo encuentra.
    public Taller obtenerTallerPorId(Long tallerId) {
        System.out.println(" [TALLER SERVICE] Buscando taller por ID para obtener: " + tallerId);
        return tallerRepository.findById(tallerId)
            .orElseThrow(() -> new RuntimeException("Taller con ID " + tallerId + " no encontrado."));
    }
}