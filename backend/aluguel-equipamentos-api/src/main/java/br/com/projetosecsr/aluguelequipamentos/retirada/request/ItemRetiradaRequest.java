package br.com.projetosecsr.aluguelequipamentos.retirada.request;

import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.CondicaoFisicaRetirada;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ItemRetiradaRequest(

		@NotNull(message = "O item da reserva é obrigatório.") Long itemReservaId,

		@NotNull(message = "A condição física do item é obrigatória.") CondicaoFisicaRetirada condicaoFisica,

		@Size(max = 500, message = "As observações devem ter no máximo 500 caracteres.") String observacoes

) {

}
