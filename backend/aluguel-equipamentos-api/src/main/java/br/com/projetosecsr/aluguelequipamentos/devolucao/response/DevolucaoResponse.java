package br.com.projetosecsr.aluguelequipamentos.devolucao.response;

import java.time.OffsetDateTime;
import java.util.List;

import br.com.projetosecsr.aluguelequipamentos.devolucao.entidade.Devolucao;

public record DevolucaoResponse(

		Long id, Long reservaId, OffsetDateTime dataHoraDevolucao, Long usuarioResponsavelId,
		String usuarioResponsavelNome, List<ItemDevolucaoResponse> itens

) {

	public static DevolucaoResponse de(Devolucao devolucao) {

		List<ItemDevolucaoResponse> itens = devolucao.getItens().stream().map(ItemDevolucaoResponse::de).toList();

		return new DevolucaoResponse(devolucao.getId(), devolucao.getReserva().getId(),
				devolucao.getDataHoraDevolucao(), devolucao.getResponsavel().getId(),
				devolucao.getResponsavel().getNome(), itens);
	}

}
