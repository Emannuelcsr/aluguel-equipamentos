package br.com.projetosecsr.aluguelequipamentos.equipamento.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CadastrarEquipamentoRequest(

		@NotBlank(message = "O nome é obrigatório.") @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres.") String nome,
		@Size(min = 3, max = 500, message = "A descrição deve ter entre 3 e 500 caracteres.") String descricao,
		@NotNull(message = "O valor da diária é obrigatório.") @DecimalMin(value = "1", message = "O valor mínimo é R$1,00") @DecimalMax(value = "100000", message = "O valor máximo é R$100.000,00") BigDecimal valorDiaria,
		@NotNull(message = "A categoria é obrigatória.") Long categoriaId

) {

}
