package com.challenge.rental_cars_spring_api.infrastructure.repositories;

import com.challenge.rental_cars_spring_api.core.domain.Aluguel;
import com.challenge.rental_cars_spring_api.core.domain.Carro;
import com.challenge.rental_cars_spring_api.core.domain.Cliente;
import com.challenge.rental_cars_spring_api.core.queries.dtos.AluguelDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AluguelRepository extends JpaRepository<Aluguel, Long> {

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Aluguel a " +
            "WHERE a.carro.id = :carroId " +
            "AND a.cliente.id = :clienteId " +
            "AND a.dataAluguel = :dataAluguel " +
            "AND a.dataDevolucao = :dataDevolucao")
    boolean verificaDuplicidade(@Param("carroId") Long carroId,
                                @Param("clienteId") Long clienteId,
                                @Param("dataAluguel") LocalDate dataAluguel,
                                @Param("dataDevolucao") LocalDate dataDevolucao);

    @Query("SELECT new com.challenge.rental_cars_spring_api.core.queries.dtos.AluguelDto(" +
            "a.dataAluguel, c.modelo, c.km, cl.nome, cl.telefone, a.dataDevolucao, a.valor, " +
            "CASE WHEN a.pago = true THEN 'SIM' ELSE 'NAO' END) " +
            "FROM Aluguel a " +
            "JOIN a.carro c " +
            "JOIN a.cliente cl")
    List<AluguelDto> findAllAlugueis();

    @Query("SELECT SUM(a.valor) FROM Aluguel a WHERE a.pago = false")
    BigDecimal calcularTotalNaoPago();
}

