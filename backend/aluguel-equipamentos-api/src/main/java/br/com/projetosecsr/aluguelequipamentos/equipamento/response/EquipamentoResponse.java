package br.com.projetosecsr.aluguelequipamentos.equipamento.response;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.projetosecsr.aluguelequipamentos.equipamento.entidade.Equipamento;

public record EquipamentoResponse(

		Long id, String nome, String descricao, Long categoriaId, String categoriaNome, BigDecimal valorDiaria,
		boolean ativo, Instant dataCriacao, Instant dataAtualizacao

) {

	public static EquipamentoResponse de(Equipamento equipamento) {

		return new EquipamentoResponse(equipamento.getId(), equipamento.getNome(), equipamento.getDescricao(),
				equipamento.getCategoria().getId(), equipamento.getCategoria().getNome(), equipamento.getValorDiaria(),
				equipamento.isAtivo(), equipamento.getDataCriacao(), equipamento.getDataAtualizacao());
	}

}
