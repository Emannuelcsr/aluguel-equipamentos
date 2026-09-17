package br.com.projetosecsr.aluguelequipamentos.autenticacao.token;

import java.time.Instant;

public record TokenGerado(

		String valor, Instant expiraEm

) {

}
