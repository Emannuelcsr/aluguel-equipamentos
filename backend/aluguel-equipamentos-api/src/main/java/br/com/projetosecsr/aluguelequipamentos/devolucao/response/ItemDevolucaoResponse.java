package br.com.projetosecsr.aluguelequipamentos.devolucao.response;

import br.com.projetosecsr.aluguelequipamentos.devolucao.entidade.CondicaoFisicaDevolucao;
import br.com.projetosecsr.aluguelequipamentos.devolucao.entidade.ItemDevolucao;

public record ItemDevolucaoResponse(

		Long id, Long itemReservaId, Long unidadeEquipamentoId, String codigoUnidade, String equipamentoNome,
		CondicaoFisicaDevolucao condicaoFisica, String observacoes

) {

	public static ItemDevolucaoResponse de(ItemDevolucao itemDevolucao) {

		return new ItemDevolucaoResponse(itemDevolucao.getId(), itemDevolucao.getItemReserva().getId(),
				itemDevolucao.getItemReserva().getUnidadeEquipamento().getId(),
				itemDevolucao.getItemReserva().getUnidadeEquipamento().getCodigo(),
				itemDevolucao.getItemReserva().getUnidadeEquipamento().getEquipamento().getNome(),
				itemDevolucao.getCondicaoFisica(), itemDevolucao.getObservacoes());
	}
}
