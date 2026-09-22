package com.vetSystem.vet_system.controller;

import com.vetSystem.vet_system.dto.DuenoDTO;
import com.vetSystem.vet_system.exception.ResourceNotFoundException;
import com.vetSystem.vet_system.service.DuenoService;
import com.vetSystem.vet_system.service.MascotaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de INTEGRACION de la capa web: @WebMvcTest levanta solo lo necesario
 * para HTTP (DispatcherServlet, Jackson, Bean Validation, @RestControllerAdvice)
 * y el Controller indicado. No crea Services, Repositories ni conexion a MySQL.
 *
 * Los Services se reemplazan con @MockitoBean. (El doc usa @MockBean: fue
 * deprecado en Spring Boot 3.4 y ELIMINADO en Spring Boot 4.)
 */
@WebMvcTest(DuenoController.class)
class DuenoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DuenoService duenoService;

    // DuenoController tambien depende de MascotaService (endpoint anidado).
    // Sin este mock el contexto no arranca: falta un bean.
    @MockitoBean
    private MascotaService mascotaService;

    private final DuenoDTO carlos =
            new DuenoDTO(1L, "Carlos", "Gonzalez", "28543210", "1145678901", "carlos@email.com");

    @Test
    void getAllDuenos_cuandoNoHayDuenos_retorna200ConListaVacia() throws Exception {
        // Arrange
        when(duenoService.getAllDuenos()).thenReturn(List.of());

        // Act + Assert
        mockMvc.perform(get("/api/duenos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getDuenoById_cuandoExiste_retorna200ConElDueno() throws Exception {
        // Arrange
        when(duenoService.getDuenoById(1L)).thenReturn(carlos);

        // Act + Assert
        mockMvc.perform(get("/api/duenos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Carlos"));
    }

    @Test
    void getDuenoById_cuandoNoExiste_retorna404ConErrorResponse() throws Exception {
        // Arrange
        when(duenoService.getDuenoById(99L)).thenThrow(new ResourceNotFoundException("Dueno", 99L));

        // Act + Assert: tambien prueba que el GlobalExceptionHandler actua
        mockMvc.perform(get("/api/duenos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/duenos/99"));
    }

    @Test
    void createDueno_cuandoBodyValido_retorna201() throws Exception {
        // Arrange
        when(duenoService.createDueno(any(DuenoDTO.class))).thenReturn(carlos);
        String body = """
                {"nombre":"Carlos","apellido":"Gonzalez","dni":"28543210",
                 "telefono":"1145678901","email":"carlos@email.com"}
                """;

        // Act + Assert
        mockMvc.perform(post("/api/duenos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createDueno_cuandoEmailVacio_retorna400YNoLlamaAlService() throws Exception {
        // Arrange
        String body = """
                {"nombre":"Carlos","apellido":"Gonzalez","dni":"28543210","email":""}
                """;

        // Act + Assert
        mockMvc.perform(post("/api/duenos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        // Fast fail: @Valid corto la peticion antes de llegar al Service.
        verifyNoInteractions(duenoService);
    }
}
