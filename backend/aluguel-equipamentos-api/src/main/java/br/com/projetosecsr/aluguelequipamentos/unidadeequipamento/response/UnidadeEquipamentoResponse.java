package br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.response;

import java.time.Instant;

import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.UnidadeEquipamento;

public record UnidadeEquipamentoResponse(

		Long id, String codigo, Long equipamentoId, String equipamentoNome, String status, boolean ativo,
		Instant dataCriacao, Instant dataAtualizacao

) {

	public static UnidadeEquipamentoResponse de(UnidadeEquipamento unidadeEquipamento) {

		return new UnidadeEquipamentoResponse(unidadeEquipamento.getId(), unidadeEquipamento.getCodigo(),
				unidadeEquipamento.getEquipamento().getId(), unidadeEquipamento.getEquipamento().getNome(),
				unidadeEquipamento.getStatus().name(), unidadeEquipamento.isAtivo(),
				unidadeEquipamento.getDataCriacao(), unidadeEquipamento.getDataAtualizacao());

	}
}
