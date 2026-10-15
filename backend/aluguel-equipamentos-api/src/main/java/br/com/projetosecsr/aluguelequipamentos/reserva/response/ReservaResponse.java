package br.com.projetosecsr.aluguelequipamentos.reserva.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.Reserva;

public record ReservaResponse(

		Long id, Long clienteId, String clienteNome, LocalDate dataInicial, LocalDate dataFinal, String status,
		BigDecimal valorTotal, List<ItemReservaResponse> itens, Instant dataCriacao, Instant dataAtualizacao

) {

	public static ReservaResponse de(Reserva reserva) {

		List<ItemReservaResponse> itens = reserva.getItens().stream().map(ItemReservaResponse::de).toList();

		return new ReservaResponse(reserva.getId(), reserva.getCliente().getId(),
				reserva.getCliente().getNomeRazaoSocial(), reserva.getDataInicial(), reserva.getDataFinal(),
				reserva.getStatus().name(), reserva.getValorTotal(), itens, reserva.getDataCriacao(),
				reserva.getDataAtualizacao());
	}
}