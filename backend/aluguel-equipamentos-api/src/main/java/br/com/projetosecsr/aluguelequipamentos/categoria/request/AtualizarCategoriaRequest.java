package br.com.projetosecsr.aluguelequipamentos.categoria.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarCategoriaRequest(

		@NotBlank @Size(min = 3, max = 100) String nome,

		@Size(max = 255) String descricao

) {

}
