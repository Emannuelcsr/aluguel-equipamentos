package br.com.projetosecsr.aluguelequipamentos.usuario.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaUsuarioRequest(

		@NotBlank(message = "A nova senha é obrigatória.") @Size(min = 15, max = 64, message = "A nova senha deve ter entre 15 e 64 caracteres.") String novaSenha,

		@NotBlank(message = "A confirmação da nova senha é obrigatória.") @Size(min = 15, max = 64, message = "A confirmação da nova senha deve ter entre 15 e 64 caracteres.") String confirmacaoNovaSenha

) {

}