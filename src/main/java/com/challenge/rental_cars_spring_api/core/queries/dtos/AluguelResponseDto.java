package com.challenge.rental_cars_spring_api.core.queries.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AluguelResponseDto {
    private List<AluguelDto> alugueis;
    private BigDecimal totalNaoPago;
}