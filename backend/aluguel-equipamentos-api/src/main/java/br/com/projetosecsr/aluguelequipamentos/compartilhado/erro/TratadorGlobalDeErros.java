package br.com.projetosecsr.aluguelequipamentos.compartilhado.erro;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.projetosecsr.aluguelequipamentos.autenticacao.excecao.CredenciaisInvalidasException;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.excecao.UsuarioInativoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.EmailJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.UsuarioNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class TratadorGlobalDeErros {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroResponse> tratarDadosInvalidos(MethodArgumentNotValidException excecao,
			HttpServletRequest requisicao) {

		List<String> mensagens = excecao.getBindingResult().getFieldErrors().stream()
				.map(erro -> erro.getDefaultMessage()).toList();

		ErroResponse resposta = new ErroResponse(

				Instant.now(), HttpStatus.BAD_REQUEST.value(), "Dados inválidos", mensagens, requisicao.getRequestURI(),
				"DADOS_INVALIDOS");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);

	}

	@ExceptionHandler(EmailJaCadastradoException.class)
	public ResponseEntity<ErroResponse> tratarEmailJaCadastrado(EmailJaCadastradoException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(

				Instant.now(), HttpStatus.CONFLICT.value(), "Conflito de dados", List.of(excecao.getMessage()),
				requisicao.getRequestURI(), "EMAIL_JA_CADASTRADO");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler(CredenciaisInvalidasException.class)
	public ResponseEntity<ErroResponse> tratarCredenciaisInvalidas(CredenciaisInvalidasException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(

				Instant.now(), HttpStatus.UNAUTHORIZED.value(), "Não autorizado", List.of(excecao.getMessage()),
				requisicao.getRequestURI(), "CREDENCIAIS_INVALIDAS");

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resposta);
	}

	@ExceptionHandler(UsuarioInativoException.class)
	public ResponseEntity<ErroResponse> tratarUsuarioInativo(UsuarioInativoException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.FORBIDDEN.value(), "Acesso negado",
				List.of(excecao.getMessage()), requisicao.getRequestURI(), "USUARIO_INATIVO");

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resposta);

	}

	@ExceptionHandler(UsuarioNaoEncontradoException.class)
	public ResponseEntity<ErroResponse> tratarUsuarioNaoEncontrado(UsuarioNaoEncontradoException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.NOT_FOUND.value(), "Recurso não encontrado",
				List.of(excecao.getMessage()), requisicao.getRequestURI(), "USUARIO_NAO_ENCONTRADO");

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
	}

}
