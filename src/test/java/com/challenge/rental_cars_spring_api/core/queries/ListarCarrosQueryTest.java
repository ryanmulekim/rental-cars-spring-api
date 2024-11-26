package com.challenge.rental_cars_spring_api.core.queries;

import com.challenge.rental_cars_spring_api.core.domain.Carro;
import com.challenge.rental_cars_spring_api.core.queries.dtos.ListarCarrosQueryResultItem;
import com.challenge.rental_cars_spring_api.infrastructure.repositories.CarroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ListarCarrosQueryTest {

    @Mock
    private CarroRepository carroRepository;

    private ListarCarrosQuery listarCarrosQuery;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listarCarrosQuery = new ListarCarrosQuery(carroRepository);
    }

    @Test
    void execute_shouldReturnListOfCarros() {

        Carro carro1 = new Carro();
        carro1.setId(1L);
        carro1.setModelo("Toyota Corolla");
        carro1.setAno("2022");
        carro1.setQtdPassageiros(5);
        carro1.setKm(15000);
        carro1.setFabricante("Toyota");
        carro1.setVlrDiaria(BigDecimal.valueOf(200));

        Carro carro2 = new Carro();
        carro2.setId(2L);
        carro2.setModelo("Honda Civic");
        carro2.setAno("2023");
        carro2.setQtdPassageiros(5);
        carro2.setKm(10000);
        carro2.setFabricante("Honda");
        carro2.setVlrDiaria(BigDecimal.valueOf(250));

        when(carroRepository.findAll()).thenReturn(List.of(carro1, carro2));

        List<ListarCarrosQueryResultItem> result = listarCarrosQuery.execute();

        assertThat(result).hasSize(2);

        ListarCarrosQueryResultItem firstCar = result.getFirst();
        assertThat(firstCar.id()).isEqualTo(1L);
        assertThat(firstCar.modelo()).isEqualTo("Toyota Corolla");

        ListarCarrosQueryResultItem secondCar = result.get(1);
        assertThat(secondCar.id()).isEqualTo(2L);
        assertThat(secondCar.modelo()).isEqualTo("Honda Civic");

    }

    @Test
    void execute_shouldReturnEmptyList_whenNoCarrosFound() {

        when(carroRepository.findAll()).thenReturn(List.of());

        List<ListarCarrosQueryResultItem> result = listarCarrosQuery.execute();

        assertThat(result).isEmpty();
    }
}