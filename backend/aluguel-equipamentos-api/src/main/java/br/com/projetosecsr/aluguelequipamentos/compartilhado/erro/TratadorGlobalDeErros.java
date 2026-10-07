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
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaJaAtivaException;
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaJaCadastradaException;
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaJaInativaException;
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.CepNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ClienteJaAtivoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ClienteJaInativoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ClienteNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ConsultaCepIndisponivelException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.DocumentoIncompativelComTipoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.DocumentoInvalidoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.DocumentoJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.EmailClienteJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.EstadoInvalidoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.NomeFantasiaNaoPermitidoException;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.excecao.EquipamentoJaAtivoException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.excecao.EquipamentoJaInativoException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.excecao.EquipamentoNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.AutodesativacaoNaoPermitidaException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.ConfirmacaoSenhaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.EmailJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.NovaSenhaIgualAtualException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.RedefinicaoPropriaSenhaNaoPermitidaException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.SenhaAtualIncorretaException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.UsuarioJaAtivoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.UsuarioJaInativoException;
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

	@ExceptionHandler(PaginaInvalidaException.class)
	public ResponseEntity<ErroResponse> tratarPaginaInvalida(PaginaInvalidaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
				"Parâmetros de paginação inválidos", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"PAGINA_NAO_ENCONTRADA");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
	}

	@ExceptionHandler({ UsuarioJaAtivoException.class, UsuarioJaInativoException.class })
	public ResponseEntity<ErroResponse> tratarSituacaoUsuarioInvalida(RuntimeException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(),
				"Conflito de situação do usuário", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"SITUACAO_USUARIO_INVALIDA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler({ AutodesativacaoNaoPermitidaException.class })
	public ResponseEntity<ErroResponse> tratarAutodesativacaoNaoPermitida(AutodesativacaoNaoPermitidaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.FORBIDDEN.value(), "Acesso negado",
				List.of(excecao.getMessage()), requisicao.getRequestURI(), "AUTODESATIVACAO_NAO_PERMITIDA");

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resposta);
	}

	@ExceptionHandler({ SenhaAtualIncorretaException.class, ConfirmacaoSenhaInvalidaException.class,
			NovaSenhaIgualAtualException.class })
	public ResponseEntity<ErroResponse> tratarAlteracaoDeSenhaInvalida(RuntimeException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
				"Alteração de senha inválida", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"ALTERACAO_SENHA_INVALIDA");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
	}

	@ExceptionHandler({ RedefinicaoPropriaSenhaNaoPermitidaException.class })
	public ResponseEntity<ErroResponse> tratarRedefinicaoPropriaSenhaNaoPermitida(
			RedefinicaoPropriaSenhaNaoPermitidaException excecao, HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.FORBIDDEN.value(), "Acesso negado",
				List.of(excecao.getMessage()), requisicao.getRequestURI(), "REDEFINICAO_PROPRIA_SENHA_NAO_PERMITIDA");

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resposta);
	}

	@ExceptionHandler({ ClienteNaoEncontradoException.class })
	public ResponseEntity<ErroResponse> tratarClienteNaoEncontrado(ClienteNaoEncontradoException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.NOT_FOUND.value(), "Cliente não encontrado",
				List.of(excecao.getMessage()), requisicao.getRequestURI(), "CLIENTE_NAO_ENCONTRADO");

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
	}

	@ExceptionHandler({ DocumentoJaCadastradoException.class, EmailClienteJaCadastradoException.class })
	public ResponseEntity<ErroResponse> tratarDadosClienteJaCadastrados(RuntimeException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(),
				"Conflito de dados do cliente", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"DADOS_CLIENTE_JA_CADASTRADOS");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler({ ClienteJaAtivoException.class, ClienteJaInativoException.class })
	public ResponseEntity<ErroResponse> tratarSituacaoClienteInvalida(RuntimeException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(),
				"Conflito de situação do cliente", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"SITUACAO_CLIENTE_INVALIDA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler({ DocumentoInvalidoException.class, DocumentoIncompativelComTipoException.class,
			NomeFantasiaNaoPermitidoException.class, EstadoInvalidoException.class })
	public ResponseEntity<ErroResponse> tratarDadosClienteInvalidos(RuntimeException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
				"Dados do cliente inválidos", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"DADOS_CLIENTE_INVALIDOS");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
	}

	@ExceptionHandler(CepNaoEncontradoException.class)
	public ResponseEntity<ErroResponse> tratarCepNaoEncontrado(CepNaoEncontradoException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.NOT_FOUND.value(), "CEP não encontrado",
				List.of(excecao.getMessage()), requisicao.getRequestURI(), "CEP_NAO_ENCONTRADO");

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
	}

	@ExceptionHandler(ConsultaCepIndisponivelException.class)
	public ResponseEntity<ErroResponse> tratarConsultaCepIndisponivel(ConsultaCepIndisponivelException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.SERVICE_UNAVAILABLE.value(),
				"Serviço de consulta de CEP indisponível", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"CONSULTA_CEP_INDISPONIVEL");

		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(resposta);
	}

	@ExceptionHandler({ CategoriaNaoEncontradoException.class })
	public ResponseEntity<ErroResponse> tratarCategoriaNaoEncontrado(CategoriaNaoEncontradoException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.NOT_FOUND.value(),
				"Categoria não encontrada", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"CATEGORIA_NAO_ENCONTRADO");

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
	}

	@ExceptionHandler({ CategoriaJaCadastradaException.class })
	public ResponseEntity<ErroResponse> tratarDadosCategoriaJaCadastrados(CategoriaJaCadastradaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(), "Categoria ja cadastrada",
				List.of(excecao.getMessage()), requisicao.getRequestURI(), "CATEGORIA_JA_CADASTRADA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler({ CategoriaJaAtivaException.class, CategoriaJaInativaException.class })
	public ResponseEntity<ErroResponse> tratarSituacaoCategoriaInvalida(RuntimeException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(),
				"Conflito de situação da categoria", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"SITUACAO_CATEGORIA_INVALIDA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler({ EquipamentoJaAtivoException.class, EquipamentoJaInativoException.class })
	public ResponseEntity<ErroResponse> tratarSituacaoEquipamentoInvalida(RuntimeException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(),
				"Conflito de situação do equipamento", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"SITUACAO_EQUIPAMENTO_INVALIDA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler(EquipamentoNaoEncontradoException.class)
	public ResponseEntity<ErroResponse> tratarEquipamentoNaoEncontrado(EquipamentoNaoEncontradoException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.NOT_FOUND.value(),
				"Equipamento não encontrado", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"EQUIPAMENTO_NAO_ENCONTRADO");

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
	}
}
