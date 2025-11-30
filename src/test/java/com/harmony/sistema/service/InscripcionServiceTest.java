package com.harmony.sistema.service;

import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Inscripcion;
import com.harmony.sistema.repository.ClienteRepository;
import com.harmony.sistema.repository.HorarioRepository;
import com.harmony.sistema.repository.InscripcionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscripcionServiceTest {

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private HorarioRepository horarioRepository;

    @Mock
    private ServicioVacantes servicioVacantes;

    @InjectMocks
    private InscripcionService inscripcionService;

    @Test
    void testCrearInscripcion() {
        // Crear datos de prueba
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombreCompleto("Test Cliente")
                .correo("test@test.com")
                .telefono("123456789")
                .build();

        Horario horario = new Horario();
        horario.setId(1L);
        horario.setVacantesDisponibles(10);

        Inscripcion inscripcionEsperada = new Inscripcion();
        inscripcionEsperada.setCliente(cliente);
        inscripcionEsperada.setHorario(horario);
        inscripcionEsperada.setFechaInscripcion(LocalDate.now());

        // Configurar mocks
        when(inscripcionRepository.findByClienteIdAndHorarioId(1L, 1L)).thenReturn(Optional.empty());

        // Ejecutar el método
        inscripcionService.crearInscripcion(cliente, horario);

        // Verificar que funciona
        verify(inscripcionRepository, times(1)).save(any(Inscripcion.class));
    }

    @Test
    void testEliminarInscripcion() {
        // Crear datos de prueba
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombreCompleto("Test Cliente")
                .build();

        Horario horario = new Horario();
        horario.setId(1L);
        horario.setVacantesDisponibles(5);

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setCliente(cliente);
        inscripcion.setHorario(horario);

        // Configurar mocks
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(horarioRepository.findById(1L)).thenReturn(Optional.of(horario));
        when(inscripcionRepository.findByClienteIdAndHorarioId(1L, 1L)).thenReturn(Optional.of(inscripcion));

        // Ejecutar el método
        inscripcionService.eliminarInscripcion(1L, 1L);

        // Verificar que funciona
        verify(inscripcionRepository, times(1)).delete(inscripcion);
        verify(horarioRepository, times(1)).save(horario);
    }
}
