package com.harmony.sistema.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmony.sistema.dto.DatosPersonalesFormDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class InscripcionRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGuardarDatosPersonalesEndpoint() throws Exception {
        // Crear DTO de datos personales
        DatosPersonalesFormDTO datos = new DatosPersonalesFormDTO();
        datos.setNombre("Cliente Test");
        datos.setEmail("clientetest@test.com");
        datos.setTelefono("123456789");

        // Ejecutar POST /api/inscripcion/cliente
        mockMvc.perform(post("/api/inscripcion/cliente")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").exists());
    }
}
