
package br.com.projetosecsr.aluguelequipamentos.retirada.response;

import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.CondicaoFisicaRetirada;
import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.ItemRetirada;

public record ItemRetiradaResponse(

		Long id, Long itemReservaId, Long unidadeEquipamentoId, String codigoUnidade, String equipamentoNome,
		CondicaoFisicaRetirada condicaoFisica, String observacoes

) {

	public static ItemRetiradaResponse de(ItemRetirada itemRetirada) {

		return new ItemRetiradaResponse(itemRetirada.getId(), itemRetirada.getItemReserva().getId(),
				itemRetirada.getItemReserva().getUnidadeEquipamento().getId(),
				itemRetirada.getItemReserva().getUnidadeEquipamento().getCodigo(),
				itemRetirada.getItemReserva().getUnidadeEquipamento().getEquipamento().getNome(),
				itemRetirada.getCondicaoFisica(), itemRetirada.getObservacoes());
	}
}
