package com.challenge.rental_cars_spring_api.access;


import com.challenge.rental_cars_spring_api.core.queries.AluguelCarrosQuery;
import com.challenge.rental_cars_spring_api.core.queries.dtos.AluguelDto;
import com.challenge.rental_cars_spring_api.core.queries.dtos.AluguelResponseDto;
import com.challenge.rental_cars_spring_api.exception.ErrorResponse;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.AluguelRepository;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.CarroRepository;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AluguelRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class AluguelRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AluguelCarrosQuery aluguelCarrosQuery;


    @MockBean
    private CarroRepository carroRepository;

    @MockBean
    private ClienteRepository clienteRepository;

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void listarAlugueis_shouldReturnListOfAlugueis() throws Exception {
        // Mockando o retorno esperado do serviço
        List<AluguelDto> alugueis = List.of(
                new AluguelDto(
                        LocalDate.of(2024, 11, 1),
                        "Carro A",
                        1000,
                        "Cliente A",
                        "+55(11)91234-5678",
                        LocalDate.of(2024, 11, 10),
                        new BigDecimal("500.00"),
                        "SIM"
                ),
                new AluguelDto(
                        LocalDate.of(2024, 11, 2),
                        "Carro B",
                        2000,
                        "Cliente B",
                        "+55(21)91234-5678",
                        LocalDate.of(2024, 11, 15),
                        new BigDecimal("700.00"),
                        "NAO"
                )
        );

        AluguelResponseDto responseDto = new AluguelResponseDto(alugueis, new BigDecimal("700.00"));

        when(aluguelCarrosQuery.findAll()).thenReturn(responseDto);

        mockMvc.perform(get("/alugueis")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alugueis", hasSize(2)))
                .andExpect(jsonPath("$.totalNaoPago", is(700.00)))
                .andExpect(jsonPath("$.alugueis[0].modelo", is("Carro A")))
                .andExpect(jsonPath("$.alugueis[1].pago", is("NAO")));
    }

    @Test
    void processarArquivo_shouldReturnSuccess_whenValidFileProvided() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "alugueis.rtn",
                MediaType.TEXT_PLAIN_VALUE,
                "03152024010120240105\n".getBytes()
        );

        doNothing().when(aluguelCarrosQuery).processaArquivo(any(MockMultipartFile.class));

        mockMvc.perform(multipart("/alugueis/processar")
                        .file(arquivo)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Arquivo processado com sucesso."));
    }

    @Test
    void processarArquivo_shouldReturnBadRequest_whenInvalidFileExtensionProvided() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "alugueis.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "03152024010120240105\n".getBytes()
        );

        mockMvc.perform(multipart("/alugueis/processar")
                        .file(arquivo)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Tipo de arquivo inválido. Apenas arquivos .rtn são suportados."));
    }

    @Test
    void processarArquivo_shouldReturnServerError_whenExceptionThrown() throws Exception {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "alugueis.rtn",
                MediaType.TEXT_PLAIN_VALUE,
                "03152024010120240105\n".getBytes()
        );

        doThrow(new RuntimeException("Erro ao processar arquivo"))
                .when(aluguelCarrosQuery).processaArquivo(any(MockMultipartFile.class));

        mockMvc.perform(multipart("/alugueis/processar")
                        .file(arquivo)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Erro ao processar o arquivo: Erro ao processar arquivo")); // Alterado para $.message
    }
}