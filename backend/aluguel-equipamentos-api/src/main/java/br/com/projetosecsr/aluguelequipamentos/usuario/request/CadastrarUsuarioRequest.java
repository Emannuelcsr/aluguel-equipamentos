package br.com.projetosecsr.aluguelequipamentos.usuario.request;

import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CadastrarUsuarioRequest(

		@NotBlank(message = "O nome é obrigatório.") @Size(min = 3, max = 150, message = "O nome deve ter entre 3 e 150 caracteres.") String nome,

		@NotBlank(message = "O e-mail é obrigatório.") @Email(message = "O e-mail deve possuir um formato válido.") @Size(max = 254, message = "O e-mail deve possuir no máximo 254 caracteres.") String email,

		@NotBlank(message = "A senha é obrigatória.") @Size(min = 15, max = 64, message = "A senha deve ter entre 15 e 64 caracteres.") String senha,

		@NotNull(message = "O perfil é obrigatório.") PerfilUsuario perfil) {

}
