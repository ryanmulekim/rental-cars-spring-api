package com.challenge.rental_cars_spring_api.core.queries.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@AllArgsConstructor
public class AluguelDto {

    private LocalDate dataAluguel;
    private String modelo;
    private Integer km;
    private String nome;
    private String telefone;
    private LocalDate dataDevolucao;
    private BigDecimal valor;
    private String pago;

}

