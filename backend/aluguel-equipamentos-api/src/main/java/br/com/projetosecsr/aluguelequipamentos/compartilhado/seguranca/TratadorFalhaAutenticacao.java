package br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.erro.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class TratadorFalhaAutenticacao implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public TratadorFalhaAutenticacao(ObjectMapper objectMapper) {

		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(HttpServletRequest requisicao, HttpServletResponse resposta, AuthenticationException excecao)
			throws IOException {

		String cabecalhoAutorizacao = requisicao.getHeader(HttpHeaders.AUTHORIZATION);

		boolean tokenAusente = cabecalhoAutorizacao == null || cabecalhoAutorizacao.isBlank();

		String mensagem;
		String codigo;

		if (tokenAusente) {

			mensagem = "Token de acesso não informado.";
			codigo = "TOKEN_AUSENTE";

		} else {

			mensagem = "Token de acesso inválido ou expirado.";

			codigo = "TOKEN_INVALIDO";
		}

		Instant momentoDoErro = Instant.now();

		int status = HttpStatus.UNAUTHORIZED.value();

		String descricaoDoErro = "Não autorizado";

		List<String> mensagens = List.of(mensagem);

		String caminho = requisicao.getRequestURI();

		ErroResponse erroResponse = new ErroResponse(momentoDoErro, status, descricaoDoErro, mensagens, caminho,
				codigo);

		resposta.setStatus(status);

		resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);

		resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());

		objectMapper.writeValue(resposta.getOutputStream(), erroResponse);
	}

}
