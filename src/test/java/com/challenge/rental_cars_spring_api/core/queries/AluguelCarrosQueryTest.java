package com.challenge.rental_cars_spring_api.core.queries;

import com.challenge.rental_cars_spring_api.core.domain.Aluguel;
import com.challenge.rental_cars_spring_api.core.domain.Carro;
import com.challenge.rental_cars_spring_api.core.domain.Cliente;
import com.challenge.rental_cars_spring_api.core.queries.dtos.AluguelDto;
import com.challenge.rental_cars_spring_api.core.queries.dtos.CamposArquivoDto;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.AluguelRepository;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.CarroRepository;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AluguelCarrosQueryTest {

    @Mock
    private AluguelRepository aluguelRepository;

    @Mock
    private CarroRepository carroRepository;

    @Mock
    private ClienteRepository clienteRepository;

    private AluguelCarrosQuery aluguelCarrosQuery;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        aluguelCarrosQuery = new AluguelCarrosQuery(aluguelRepository, carroRepository, clienteRepository);
    }

    @Test
    void findAll_shouldReturnAluguelResponseDto() {
        AluguelDto aluguel1 = new AluguelDto(LocalDate.now(), "Modelo A", 1000, "Cliente 1", "+55(11)12345-6789", LocalDate.now().plusDays(5), BigDecimal.valueOf(500), "NAO");
        AluguelDto aluguel2 = new AluguelDto(LocalDate.now().minusDays(3), "Modelo B", 2000, "Cliente 2", "+55(21)98765-4321", LocalDate.now().plusDays(7), BigDecimal.valueOf(700), "SIM");

        when(aluguelRepository.findAllAlugueis()).thenReturn(List.of(aluguel1, aluguel2));
        when(aluguelRepository.calcularTotalNaoPago()).thenReturn(BigDecimal.valueOf(500));

        var response = aluguelCarrosQuery.findAll();

        assertThat(response.getAlugueis()).hasSize(2);
        assertThat(response.getTotalNaoPago()).isEqualTo(BigDecimal.valueOf(500));
    }

    @Test
    void processaArquivo_shouldProcessValidFile() throws IOException {
        String conteudoArquivo =
                        "03152024010120240105\n" +
                        "11012024010220240108\n" +
                        "05102024010320240110\n";

        MultipartFile arquivoMock = new MockMultipartFile(
                "arquivo",
                "valid_file.rtn",
                "text/plain",
                conteudoArquivo.getBytes()
        );

        Carro carro1 = new Carro();
        carro1.setId(3L);
        carro1.setVlrDiaria(new BigDecimal("100.00"));

        Carro carro2 = new Carro();
        carro2.setId(11L);
        carro2.setVlrDiaria(new BigDecimal("150.00"));

        Carro carro3 = new Carro();
        carro3.setId(5L);
        carro3.setVlrDiaria(new BigDecimal("200.00"));

        when(carroRepository.findById(3L)).thenReturn(Optional.of(carro1));
        when(carroRepository.findById(11L)).thenReturn(Optional.of(carro2));
        when(carroRepository.findById(5L)).thenReturn(Optional.of(carro3));

        Cliente cliente1 = new Cliente();
        cliente1.setId(15L);

        Cliente cliente2 = new Cliente();
        cliente2.setId(2L);

        Cliente cliente3 = new Cliente();
        cliente3.setId(3L);

        when(clienteRepository.findById(15L)).thenReturn(Optional.of(cliente1));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente2));
        when(clienteRepository.findById(3L)).thenReturn(Optional.of(cliente3));

        aluguelCarrosQuery.processaArquivo(arquivoMock);

        verify(aluguelRepository, times(1)).save(any(Aluguel.class));
    }

    @Test
    void processaArquivo_shouldIgnoreInvalidLines() throws IOException {
        String conteudoArquivo =
                "Linha inválida\n" +
                        "031520240101\n" +
                        "03152024010120240105\n";

        MultipartFile arquivoMock = new MockMultipartFile(
                "arquivo",
                "invalid_file.rtn",
                "text/plain",
                conteudoArquivo.getBytes()
        );

        Carro carro = new Carro();
        carro.setId(3L);
        carro.setVlrDiaria(new BigDecimal("100.00"));
        when(carroRepository.findById(3L)).thenReturn(Optional.of(carro));

        Cliente cliente = new Cliente();
        cliente.setId(15L);
        when(clienteRepository.findById(15L)).thenReturn(Optional.of(cliente));

        aluguelCarrosQuery.processaArquivo(arquivoMock);

        verify(aluguelRepository, times(1)).save(any(Aluguel.class));
    }

    @Test
    void processaArquivo_shouldHandleMissingCarro() throws IOException {
        String conteudoArquivo =
                "03152024010120240105\n";

        MultipartFile arquivoMock = new MockMultipartFile(
                "arquivo",
                "missing_carro.rtn",
                "text/plain",
                conteudoArquivo.getBytes()
        );

        when(carroRepository.findById(3L)).thenReturn(Optional.empty());

        aluguelCarrosQuery.processaArquivo(arquivoMock);

        verify(aluguelRepository, never()).save(any(Aluguel.class));
    }

}