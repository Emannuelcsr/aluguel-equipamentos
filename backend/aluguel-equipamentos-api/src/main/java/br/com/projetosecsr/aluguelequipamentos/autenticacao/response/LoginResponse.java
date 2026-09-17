package br.com.projetosecsr.aluguelequipamentos.autenticacao.response;

import java.time.Instant;

public record LoginResponse(

		String token, String tipo, Instant expiraEm, UsuarioAutenticadoResponse usuario

) {

}
