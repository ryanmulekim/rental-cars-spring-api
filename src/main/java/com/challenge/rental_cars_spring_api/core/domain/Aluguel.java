package com.challenge.rental_cars_spring_api.core.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@Entity
@Table(name = "aluguel", uniqueConstraints = @UniqueConstraint(columnNames = {"carro_id", "cliente_id"}))
public class Aluguel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "aluguel_seq_gen")
    @SequenceGenerator(name = "aluguel_seq_gen", sequenceName = "aluguel_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "carro_id", nullable = false)
    private Carro carro;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    private LocalDate dataAluguel;
    private LocalDate dataDevolucao;

    @Column(precision = 7, scale = 2, nullable = false)
    private BigDecimal valor;

    private boolean pago;
}
