package br.com.projetosecsr.aluguelequipamentos.devolucao.request;

import br.com.projetosecsr.aluguelequipamentos.devolucao.entidade.CondicaoFisicaDevolucao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ItemDevolucaoRequest(

		@NotNull @Positive Long itemReservaId,

		@NotNull CondicaoFisicaDevolucao condicaoFisica,

		@Size(max = 500) String observacoes

) {

}
