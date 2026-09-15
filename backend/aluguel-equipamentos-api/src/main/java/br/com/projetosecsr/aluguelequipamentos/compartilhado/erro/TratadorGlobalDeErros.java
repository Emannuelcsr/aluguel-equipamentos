package br.com.projetosecsr.aluguelequipamentos.compartilhado.erro;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.EmailJaCadastradoException;
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

}
