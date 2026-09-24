package br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.erro.ErroResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class TratadorAcessoNegado implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public TratadorAcessoNegado(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void handle(HttpServletRequest requisicao, HttpServletResponse resposta, AccessDeniedException excecao)
			throws IOException, ServletException {

		Instant momentoDoErro = Instant.now();
		int status = HttpStatus.FORBIDDEN.value();
		String descricaoDoErro = "Acesso negado";
		List<String> mensagens = List.of("Você não possui permissão para acessar este recurso.");
		String caminho = requisicao.getRequestURI();
		String codigo = "ACESSO_NEGADO";

		// -----------
		ErroResponse erroResponse = new ErroResponse(momentoDoErro, status, descricaoDoErro, mensagens, caminho,
				codigo);
		// ------------

		resposta.setStatus(status);
		resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
		resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());

		objectMapper.writeValue(resposta.getOutputStream(), erroResponse);

	}

}
