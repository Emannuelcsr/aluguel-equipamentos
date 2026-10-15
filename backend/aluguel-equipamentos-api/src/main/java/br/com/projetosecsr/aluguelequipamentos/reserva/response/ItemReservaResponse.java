package br.com.projetosecsr.aluguelequipamentos.reserva.response;

import java.math.BigDecimal;

import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.ItemReserva;

public record ItemReservaResponse(

		Long id, Long unidadeEquipamentoId, String codigoUnidade, String equipamentoNome, BigDecimal valorDiaria,
		Long quantidadeDias, BigDecimal subtotal

) {

	public static ItemReservaResponse de(ItemReserva itemReserva) {

		return new ItemReservaResponse(itemReserva.getId(), itemReserva.getUnidadeEquipamento().getId(),
				itemReserva.getUnidadeEquipamento().getCodigo(),
				itemReserva.getUnidadeEquipamento().getEquipamento().getNome(), itemReserva.getValorDiaria(),
				itemReserva.getQuantidadeDias(), itemReserva.getSubtotal());
	}
}
