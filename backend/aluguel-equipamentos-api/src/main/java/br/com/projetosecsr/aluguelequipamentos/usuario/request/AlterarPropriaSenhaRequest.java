package br.com.projetosecsr.aluguelequipamentos.usuario.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarPropriaSenhaRequest(

		@NotBlank(message = "A senha atual é obrigatória.") String senhaAtual,

		@NotBlank(message = "A nova senha é obrigatória.") @Size(min = 15, max = 64, message = "A nova senha deve ter entre 15 e 64 caracteres.") String senhaNova,

		@NotBlank(message = "A confirmação da nova senha é obrigatória.") @Size(min = 15, max = 64, message = "A confirmação da nova senha deve ter entre 15 e 64 caracteres.") String confirmacaoNovaSenha

) {

}
