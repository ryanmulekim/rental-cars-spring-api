package com.challenge.rental_cars_spring_api.access;


import com.challenge.rental_cars_spring_api.core.queries.ListarCarrosQuery;
import com.challenge.rental_cars_spring_api.core.queries.dtos.ListarCarrosQueryResultItem;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.AluguelRepository;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.CarroRepository;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarrosRestController.class)
class CarrosRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListarCarrosQuery listarCarrosQuery;

    @MockBean
    private AluguelRepository aluguelRepository;

    @MockBean
    private CarroRepository carroRepository;

    @MockBean
    private ClienteRepository clienteRepository;

    private List<ListarCarrosQueryResultItem> carros;


    @BeforeEach
    void setUp() {
        carros = Arrays.asList(
                new ListarCarrosQueryResultItem(1L, "Modelo A"),
                new ListarCarrosQueryResultItem(2L, "Modelo B")
        );
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void listarCarros_shouldReturnListOfCarros() throws Exception {
        when(listarCarrosQuery.execute()).thenReturn(carros);

        mockMvc.perform(get("/carros")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].modelo", is("Modelo A")));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void listarCarros_shouldReturnEmptyList() throws Exception {
        when(listarCarrosQuery.execute()).thenReturn(List.of());

        mockMvc.perform(get("/carros")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void listarCarros_shouldReturn500OnInternalError() throws Exception {
        when(listarCarrosQuery.execute()).thenThrow(new RuntimeException("Erro interno"));

        mockMvc.perform(get("/carros")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}