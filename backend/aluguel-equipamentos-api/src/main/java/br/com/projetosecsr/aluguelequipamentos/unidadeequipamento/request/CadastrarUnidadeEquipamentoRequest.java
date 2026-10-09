package br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.request;

import jakarta.validation.constraints.NotNull;

public record CadastrarUnidadeEquipamentoRequest(

		@NotNull(message = "O equipamento é obrigatório.") Long equipamentoId

) {

}
