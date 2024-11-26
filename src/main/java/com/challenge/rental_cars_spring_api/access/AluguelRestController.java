package com.challenge.rental_cars_spring_api.access;

import com.challenge.rental_cars_spring_api.core.queries.AluguelCarrosQuery;
import com.challenge.rental_cars_spring_api.core.queries.dtos.AluguelDto;
import com.challenge.rental_cars_spring_api.core.queries.dtos.AluguelResponseDto;
import com.challenge.rental_cars_spring_api.core.queries.dtos.ListarCarrosQueryResultItem;
import com.challenge.rental_cars_spring_api.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/alugueis")
@RequiredArgsConstructor
public class AluguelRestController {

    private final AluguelCarrosQuery aluguelCarrosQuery;

    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retorna a lista com os alugueis encontrados.", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ListarCarrosQueryResultItem.class))}),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)})})
    public ResponseEntity<AluguelResponseDto> listarAlugueis() {
        return new ResponseEntity<>(aluguelCarrosQuery.findAll(), HttpStatus.OK);
    }

    @PostMapping(value = "/processar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Arquivo processado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro no arquivo enviado."),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor")
    })
    public ResponseEntity<String> processarArquivo(
            @RequestParam("arquivo") MultipartFile arquivo) {
        if (!Objects.requireNonNull(arquivo.getOriginalFilename()).endsWith(".rtn")) {
            throw new ErrorResponse(
                    400,
                    "Bad Request",
                    "/alugueis/processar",
                    "Tipo de arquivo inválido. Apenas arquivos .rtn são suportados."
            );
        }

        try {
            aluguelCarrosQuery.processaArquivo(arquivo);
            return ResponseEntity.ok("Arquivo processado com sucesso.");
        } catch (Exception e) {
            throw new ErrorResponse(
                    500,
                    "Internal Server Error",
                    "/alugueis/processar",
                    "Erro ao processar o arquivo: " + e.getMessage()
            );
        }
    }


}
