package com.challenge.rental_cars_spring_api.core.queries.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CamposArquivoDto {

    private Long carroId;
    private Long clienteId;
    private LocalDate dataAluguel;
    private LocalDate dataDevolucao;
}
