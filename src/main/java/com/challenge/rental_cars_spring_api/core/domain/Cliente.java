package com.challenge.rental_cars_spring_api.core.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity(name = "cliente")
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cpf;
    private String cnh;
    private String telefone;
}
