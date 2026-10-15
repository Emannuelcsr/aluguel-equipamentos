package br.com.projetosecsr.aluguelequipamentos.reserva.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CadastrarReservaRequest(

		@NotNull(message = "O cliente é obrigatório.") Long clienteId,

		@NotNull(message = "A data inicial é obrigatória") LocalDate dataInicial,

		@NotNull(message = "A data final é obrigatória") LocalDate dataFinal,

		@NotEmpty(message = "A reserva deve possuir pelo menos uma unidade.") List<@NotNull(message = "O id da unidade é obrigatório.") Long> unidadesIds

) {

}
