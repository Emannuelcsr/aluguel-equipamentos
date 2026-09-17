package br.com.projetosecsr.aluguelequipamentos.autenticacao.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

		@NotBlank(message = "O e-mail é obrigatório.") @Email(message = "O e-mail deve possuir um formato válido.") @Size(max = 254, message = "O e-mail deve possuir no máximo 254 caracteres.") String email,

		@NotBlank(message = "A senha é obrigatória.") String senha

) {

}
