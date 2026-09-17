package br.com.projetosecsr.aluguelequipamentos.autenticacao.response;

import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;

public record UsuarioAutenticadoResponse(

		Long id, String nome, String email, PerfilUsuario perfil

) {

	public static UsuarioAutenticadoResponse de(Usuario usuario) {

		return new UsuarioAutenticadoResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
				usuario.getPerfil());

	}

}
