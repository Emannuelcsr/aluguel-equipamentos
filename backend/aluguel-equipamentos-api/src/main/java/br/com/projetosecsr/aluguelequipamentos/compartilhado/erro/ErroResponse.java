package br.com.projetosecsr.aluguelequipamentos.compartilhado.erro;

import java.time.Instant;
import java.util.List;

public record ErroResponse(
		
		Instant timestamp,
		int status,
		String erro,
		List<String> mensagens,
		String path,
		String codigo
		
		
		) {

}
