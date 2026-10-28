package br.com.projetosecsr.aluguelequipamentos.devolucao.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CadastrarDevolucaoRequest(

		@NotNull @Positive Long reservaId,

		@NotNull Boolean confirmacaoRecebimento,

		@NotEmpty @Valid List<ItemDevolucaoRequest> itens) {

}
