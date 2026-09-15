package br.com.projetosecsr.aluguelequipamentos.usuario.response;

import java.time.Instant;

import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;

public record UsuarioResponse(

		Long id, String nome, String email, PerfilUsuario perfil, boolean ativo, Instant dataCriacao,
		Instant dataAtualizacao

) {

	public static UsuarioResponse de(Usuario usuario) {

		return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil(),
				usuario.isAtivo(), usuario.getDataCriacao(), usuario.getDataAtualizacao());
	}

}
