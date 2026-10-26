package br.com.projetosecsr.aluguelequipamentos.retirada.request;

import java.util.List;

import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.MetodoVerificacaoAutorizacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastrarRetiradaRequest(

		@NotNull(message = "A reserva é obrigatória.") Long reservaId,

		@NotBlank(message = "O documento do recebedor é obrigatório.") @Pattern(regexp = "\\d{11}", message = "O CPF do recebedor deve conter 11 dígitos.") String documentoRecebedor,

		@NotBlank(message = "O nome do recebedor é obrigatório.") @Size(max = 150, message = "O nome do recebedor deve ter no máximo 150 caracteres.") String nomeRecebedor,

		@NotNull(message = "O método de verificação da autorização é obrigatório.") MetodoVerificacaoAutorizacao metodoVerificacao,

		@Size(max = 500, message = "A descrição do método deve ter no máximo 500 caracteres.") String descricaoOutroMetodo,

		@NotEmpty(message = "A retirada deve conter pelo menos um item.") List<@NotNull @Valid ItemRetiradaRequest> itens,

		@NotNull(message = "A confirmação da autorização é obrigatória.") @AssertTrue(message = "A autorização deve estar confirmada.") Boolean autorizacaoConfirmada,

		@NotNull(message = "A confirmação do recebimento é obrigatória.") @AssertTrue(message = "O recebimento deve estar confirmado.") Boolean recebimentoConfirmado

) {

}
