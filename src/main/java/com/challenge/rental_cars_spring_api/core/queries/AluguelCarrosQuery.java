package com.challenge.rental_cars_spring_api.core.queries;

import com.challenge.rental_cars_spring_api.core.domain.Aluguel;
import com.challenge.rental_cars_spring_api.core.domain.Carro;
import com.challenge.rental_cars_spring_api.core.domain.Cliente;
import com.challenge.rental_cars_spring_api.core.queries.dtos.AluguelDto;
import com.challenge.rental_cars_spring_api.core.queries.dtos.AluguelResponseDto;
import com.challenge.rental_cars_spring_api.core.queries.dtos.CamposArquivoDto;
import com.challenge.rental_cars_spring_api.exception.ErrorResponse;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.AluguelRepository;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.CarroRepository;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AluguelCarrosQuery {

    private final AluguelRepository aluguelRepository;
    private final CarroRepository carroRepository;
    private final ClienteRepository clienteRepository;

    public AluguelResponseDto findAll() {
        List<AluguelDto> alugueis = aluguelRepository.findAllAlugueis();
        BigDecimal totalNaoPago = aluguelRepository.calcularTotalNaoPago();

        return AluguelResponseDto.builder()
                .alugueis(alugueis)
                .totalNaoPago(totalNaoPago)
                .build();
    }

    @Transactional
    public void processaArquivo(MultipartFile arquivo) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(arquivo.getInputStream()))) {
            String linha;
            while ((linha = reader.readLine()) != null) {

                if (!isLinhaValida(linha)) {
                    log.warn("Linha inválida encontrada: {}", linha);
                    continue;
                }

                CamposArquivoDto camposArquivoDto = extrairCamposArquivo(linha);

                Optional<Carro> carroOpt = carroRepository.findById(camposArquivoDto.getCarroId());
                if (carroOpt.isEmpty()) {
                    log.warn("Carro com ID {} não encontrado. Linha ignorada.", camposArquivoDto.getCarroId());
                    continue;
                }

                Optional<Cliente> clienteOpt = clienteRepository.findById(camposArquivoDto.getClienteId());
                if (clienteOpt.isEmpty()) {
                    log.warn("Cliente com ID {} não encontrado. Linha ignorada.", camposArquivoDto.getClienteId());
                    continue;
                }

                long diasAlugados = ChronoUnit.DAYS.between(camposArquivoDto.getDataAluguel(), camposArquivoDto.getDataDevolucao());
                BigDecimal valor = carroOpt.get().getVlrDiaria().multiply(BigDecimal.valueOf(diasAlugados));

                salvarAluguelComValidacao(camposArquivoDto, carroOpt.get(), clienteOpt.get(), valor);
            }
        } catch (IOException e) {
            log.error("Erro ao ler o arquivo: {}", e.getMessage(), e);
            throw new ErrorResponse(400, "Bad Request", "/alugueis/processar", "Erro ao ler o arquivo.");
        } catch (Exception e) {
            log.error("Erro inesperado: {}", e.getMessage(), e);
            throw new ErrorResponse(500, "Internal Server Error", "/alugueis/processar", "Erro inesperado durante o processamento.");
        }
    }

    @Transactional
    private void salvarAluguelComValidacao(CamposArquivoDto camposArquivoDto, Carro carro, Cliente cliente, BigDecimal valor) {
        boolean aluguelExiste = aluguelRepository.verificaDuplicidade(
                camposArquivoDto.getCarroId(),
                camposArquivoDto.getClienteId(),
                camposArquivoDto.getDataAluguel(),
                camposArquivoDto.getDataDevolucao()
        );

        if (aluguelExiste) {
            log.warn("Aluguel já existe para Carro ID {}, Cliente ID {}, Data Aluguel {} e Data Devolução {}",
                    camposArquivoDto.getCarroId(), camposArquivoDto.getClienteId(),
                    camposArquivoDto.getDataAluguel(), camposArquivoDto.getDataDevolucao());
            return;
        }

        Aluguel aluguel = new Aluguel();
        aluguel.setCarro(carro);
        aluguel.setCliente(cliente);
        aluguel.setDataAluguel(camposArquivoDto.getDataAluguel());
        aluguel.setDataDevolucao(camposArquivoDto.getDataDevolucao());
        aluguel.setValor(valor);
        aluguel.setPago(false);

        aluguelRepository.save(aluguel);
        log.info("Aluguel salvo com sucesso para Carro ID {} e Cliente ID {}", camposArquivoDto.getCarroId(), camposArquivoDto.getClienteId());
    }

    private CamposArquivoDto extrairCamposArquivo(String linha) {
        return CamposArquivoDto.builder()
                .carroId(Long.parseLong(linha.substring(0, 2).trim()))
                .clienteId(Long.parseLong(linha.substring(2, 4).trim()))
                .dataAluguel(LocalDate.parse(linha.substring(4, 12), DateTimeFormatter.ofPattern("yyyyMMdd")))
                .dataDevolucao(LocalDate.parse(linha.substring(12, 20), DateTimeFormatter.ofPattern("yyyyMMdd")))
                .build();
    }

    private boolean isLinhaValida(String linha) {
        return linha != null && linha.trim().length() == 20;
    }
}
