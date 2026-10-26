
package br.com.projetosecsr.aluguelequipamentos.retirada.response;

import java.time.OffsetDateTime;
import java.util.List;

import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.MetodoVerificacaoAutorizacao;
import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.Retirada;

public record RetiradaResponse(

		Long id, Long reservaId, OffsetDateTime dataHoraRetirada, String nomeRecebedor, String documentoRecebedor,
		MetodoVerificacaoAutorizacao metodoVerificacao, String descricaoOutroMetodo, Long usuarioResponsavelId,
		String usuarioResponsavelNome, List<ItemRetiradaResponse> itens

) {

	public static RetiradaResponse de(Retirada retirada) {

		List<ItemRetiradaResponse> itens = retirada.getItens().stream().map(ItemRetiradaResponse::de).toList();

		return new RetiradaResponse(retirada.getId(), retirada.getReserva().getId(), retirada.getDataHoraRetirada(),
				retirada.getNomeRecebedor(), retirada.getDocumentoRecebedor(), retirada.getMetodoVerificacao(),
				retirada.getDescricaoOutroMetodo(), retirada.getResponsavel().getId(),
				retirada.getResponsavel().getNome(), itens);
	}
}
