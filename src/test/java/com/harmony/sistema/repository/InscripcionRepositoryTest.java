package com.harmony.sistema.repository;

import com.harmony.sistema.model.Cliente;
import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Inscripcion;
import com.harmony.sistema.model.Taller;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class InscripcionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Test
    void testFindByClienteIdAndHorarioId() {
        // Crear y guardar un taller
        Taller taller = new Taller();
        taller.setNombre("Taller Test");
        taller.setDescripcion("Descripción test");
        taller.setImagenTaller("test.jpg");
        taller.setActivo(true);
        entityManager.persist(taller);

        // Crear y guardar un horario
        Horario horario = new Horario();
        horario.setTaller(taller);
        horario.setDiasDeClase("Lunes");
        horario.setHoraInicio(LocalTime.of(10, 0));
        horario.setHoraFin(LocalTime.of(12, 0));
        horario.setFechaInicio(LocalDate.now());
        horario.setVacantesDisponibles(10);
        entityManager.persist(horario);

        // Crear y guardar un cliente
        Cliente cliente = Cliente.builder()
                .nombreCompleto("Cliente Test")
                .correo("cliente@test.com")
                .telefono("123456789")
                .build();
        entityManager.persist(cliente);

        // Crear y guardar una inscripción
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setCliente(cliente);
        inscripcion.setHorario(horario);
        inscripcion.setFechaInscripcion(LocalDate.now());
        inscripcion.setPagado(false);
        entityManager.persist(inscripcion);
        entityManager.flush();

        // Ejecutar el método
        Optional<Inscripcion> resultado = inscripcionRepository.findByClienteIdAndHorarioId(
                cliente.getId(), horario.getId());

        // Verificar que funciona
        assertTrue(resultado.isPresent());
        assertEquals(cliente.getId(), resultado.get().getCliente().getId());
        assertEquals(horario.getId(), resultado.get().getHorario().getId());
    }
}
