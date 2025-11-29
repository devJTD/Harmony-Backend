package com.harmony.sistema.service;

import com.harmony.sistema.model.Horario;
import com.harmony.sistema.model.Inscripcion;
import com.harmony.sistema.repository.ClienteRepository;
import com.harmony.sistema.repository.HorarioRepository;
import com.harmony.sistema.repository.InscripcionRepository;
import com.harmony.sistema.dto.ClienteRegistroDTO;
import com.harmony.sistema.dto.CredencialesDTO;
import com.harmony.sistema.dto.DatosPersonalesFormDTO;
import com.harmony.sistema.dto.InscripcionFormDTO;
import com.harmony.sistema.model.Cliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para la gestión de inscripciones.
 * Refactorizado para seguir el principio de Responsabilidad Única (SRP).
 * Delega responsabilidades específicas a servicios especializados.
 */
@Service
public class InscripcionService {

        @Autowired
        private ClienteService clienteService;

        @Autowired
        private ClienteRepository clienteRepository;

        @Autowired
        private InscripcionRepository inscripcionRepository;

        @Autowired
        private HorarioRepository horarioRepository;

        // ✅ Servicios especializados (SRP)
        @Autowired
        private ServicioVacantes servicioVacantes;

        @Autowired
        private ServicioRegistroCliente servicioRegistroCliente;

        @Autowired
        private ServicioNotificacion servicioNotificacion;

        /**
         * Guarda u obtiene un cliente temporal basado en los datos personales
         * proporcionados.
         * Este método se usa en el flujo de inscripción cuando el cliente aún no está
         * registrado.
         */
        @Transactional
        public Cliente guardarOObtenerClienteTemporal(DatosPersonalesFormDTO datos) {

                System.out.println(
                                " [INSCRIPCION SERVICE] Procesando datos personales para email: " + datos.getEmail());

                // Verificar si ya existe un cliente con este correo
                Optional<Cliente> clienteTemporalOpt = clienteService.encontrarClientePorCorreo(datos.getEmail());
                if (clienteTemporalOpt.isPresent()) {
                        Cliente clienteTemporal = clienteTemporalOpt.get();
                        clienteTemporal.setNombreCompleto(datos.getNombre());
                        clienteTemporal.setTelefono(datos.getTelefono());
                        clienteRepository.save(clienteTemporal);
                        System.out.println(" [INSCRIPCION SERVICE] Cliente temporal ya existe (ID: "
                                        + clienteTemporal.getId()
                                        + "). Devolviendo existente y actualizado.");
                        return clienteTemporal;
                }

                // Crear nuevo cliente temporal
                ClienteRegistroDTO registroDTO = new ClienteRegistroDTO(
                                datos.getNombre(),
                                datos.getEmail(),
                                datos.getTelefono());

                Cliente clienteRecienCreado = clienteService.crearClienteTemporal(registroDTO);

                System.out.println(" [INSCRIPCION SERVICE] Cliente recién registrado como TEMPORAL (ID: "
                                + clienteRecienCreado.getId() + ").");
                return clienteRecienCreado;
        }

        /**
         * Procesa la inscripción completa de un cliente.
         * Coordina el registro del cliente, creación de usuario, inscripciones y
         * notificaciones.
         * 
         * ✅ REFACTORIZADO: Ahora delega responsabilidades a servicios especializados
         */
        @Transactional
        public CredencialesDTO procesarInscripcionCompleta(InscripcionFormDTO dto,
                        Map<Long, Long> horariosSeleccionados) {
                System.out.println(" [INSCRIPCION SERVICE] Iniciando proceso de inscripción completa para: "
                                + dto.getEmail());

                // 1. Registrar y activar cliente (delega a ServicioRegistroCliente)
                CredencialesDTO credenciales = servicioRegistroCliente.registrarYActivarCliente(dto);

                // 2. Obtener el cliente registrado
                Cliente cliente = clienteService.encontrarClientePorEmail(dto.getEmail());

                // 3. Crear inscripciones para cada horario seleccionado
                System.out.println(" [INSCRIPCION SERVICE] Procesando " + horariosSeleccionados.size()
                                + " inscripciones.");

                horariosSeleccionados.forEach((tallerId, horarioId) -> {
                        Horario horario = horarioRepository.findById(horarioId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Horario con ID " + horarioId + " no encontrado."));

                        crearInscripcion(cliente, horario);
                });

                System.out.println(
                                " [INSCRIPCION SERVICE SUCCESS] Proceso de inscripción completa finalizado.");

                return credenciales;
        }

        /**
         * Crea una inscripción individual para un cliente en un horario específico.
         * ✅ REFACTORIZADO: Usa ServicioVacantes para gestionar vacantes
         */
        @Transactional
        public void crearInscripcion(Cliente cliente, Horario horario) {
                System.out.println(" [INSCRIPCION SERVICE] Creando inscripción para Cliente ID: " + cliente.getId()
                                + " en Horario ID: " + horario.getId());

                // Validar que no exista inscripción duplicada
                Optional<Inscripcion> inscripcionExistente = inscripcionRepository
                                .findByClienteIdAndHorarioId(cliente.getId(), horario.getId());
                if (inscripcionExistente.isPresent()) {
                        throw new RuntimeException("El cliente ya está inscrito en este horario.");
                }

                // Reservar vacante (delega a ServicioVacantes)
                servicioVacantes.reservarVacante(horario);

                // Crear y guardar inscripción
                Inscripcion inscripcion = new Inscripcion();
                inscripcion.setCliente(cliente);
                inscripcion.setHorario(horario);
                inscripcion.setFechaInscripcion(LocalDate.now());
                inscripcion.setPagado(true);

                inscripcionRepository.save(inscripcion);

                System.out.println(" [INSCRIPCION SERVICE] Inscripción creada para Horario ID: " + horario.getId());
        }

        // ==========================================
        // MÉTODOS PARA GESTIÓN DE INSCRIPCIONES
        // ==========================================

        /**
         * Inscribe un cliente existente en un horario.
         * ✅ REFACTORIZADO: Usa ServicioVacantes
         */
        @Transactional
        public void inscribirClienteExistente(Long clienteId, Long horarioId) {
                System.out.println(" [INSCRIPCION SERVICE] Inscribiendo cliente existente ID: " + clienteId
                                + " en horario ID: " + horarioId);

                Cliente cliente = clienteRepository.findById(clienteId)
                                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + clienteId));

                Horario horario = horarioRepository.findById(horarioId)
                                .orElseThrow(() -> new RuntimeException("Horario no encontrado con ID: " + horarioId));

                crearInscripcion(cliente, horario);

                System.out.println(" [INSCRIPCION SERVICE] Inscripción exitosa.");
        }

        /**
         * Elimina una inscripción y libera la vacante.
         * ✅ REFACTORIZADO: Usa ServicioVacantes
         */
        @Transactional
        public void eliminarInscripcion(Long clienteId, Long horarioId) {
                System.out.println(" [INSCRIPCION SERVICE] Eliminando inscripción - Cliente ID: " + clienteId
                                + ", Horario ID: " + horarioId);

                Inscripcion inscripcion = inscripcionRepository.findByClienteIdAndHorarioId(clienteId, horarioId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Inscripción no encontrada para el cliente y horario especificados."));

                Horario horario = inscripcion.getHorario();

                inscripcionRepository.delete(inscripcion);

                // Liberar vacante (delega a ServicioVacantes)
                servicioVacantes.liberarVacante(horario);

                System.out.println(" [INSCRIPCION SERVICE] Inscripción eliminada y vacante liberada.");
        }

        /**
         * Solicita la baja de un cliente de un horario.
         * ✅ REFACTORIZADO: Usa ServicioNotificacion
         */
        public void solicitarBaja(Long clienteId, Long horarioId, String motivo) {
                System.out.println(" [INSCRIPCION SERVICE] Solicitud de baja - Cliente ID: " + clienteId
                                + ", Horario ID: " + horarioId);

                Cliente cliente = clienteRepository.findById(clienteId)
                                .orElseThrow(() -> new RuntimeException("Cliente no encontrado."));

                Horario horario = horarioRepository.findById(horarioId)
                                .orElseThrow(() -> new RuntimeException("Horario no encontrado."));

                // Validar que la inscripción exista
                inscripcionRepository.findByClienteIdAndHorarioId(clienteId, horarioId)
                                .orElseThrow(() -> new RuntimeException(
                                                "No se encontró una inscripción activa para este horario."));

                // Enviar notificación (delega a ServicioNotificacion)
                servicioNotificacion.enviarSolicitudBaja(cliente, horario, motivo);
        }

        /**
         * Obtiene las inscripciones de un cliente.
         */
        public List<Inscripcion> obtenerInscripcionesPorCliente(Long clienteId) {
                return inscripcionRepository.findByClienteId(clienteId);
        }
}