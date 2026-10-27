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
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.ClienteInativoParaReservaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.ConflitoDeReservaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.DataFinalInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.DataInicialInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.OperacaoReservaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.ReservaNaoEncontradaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.UnidadeDuplicadaNaReservaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.ClienteInativoParaRetiradaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.ConfirmacaoRetiradaObrigatoriaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.DescricaoMetodoVerificacaoObrigatoriaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.ItemRetiradaDuplicadoException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.ItensRetiradaDivergentesException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.ObservacaoObrigatoriaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.ReservaNaoAprovadaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.RetiradaForaDoPeriodoException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.RetiradaJaRealizadaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.RetiradaNaoEncontradaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.UnidadeInativaParaRetiradaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.UnidadeIndisponivelParaRetiradaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.OperacaoInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoJaAtivaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoJaInativaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeIndisponivelParaReservaException;
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

	@ExceptionHandler(UnidadeEquipamentoNaoEncontradoException.class)
	public ResponseEntity<ErroResponse> tratarUnidadeEquipamentoNaoEncontrado(
			UnidadeEquipamentoNaoEncontradoException excecao, HttpServletRequest request) {

		ErroResponse erro = new ErroResponse(Instant.now(), HttpStatus.NOT_FOUND.value(),
				"Unidade de equipamento não encontrada", List.of(excecao.getMessage()), request.getRequestURI(),
				"UNIDADE_EQUIPAMENTO_NAO_ENCONTRADA");

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
	}

	@ExceptionHandler({ UnidadeEquipamentoJaAtivaException.class, UnidadeEquipamentoJaInativaException.class })
	public ResponseEntity<ErroResponse> tratarSituacaoUnidadeEquipamentoInvalida(RuntimeException excecao,
			HttpServletRequest request) {

		ErroResponse erro = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(),
				"Conflito de situação da unidade de equipamento", List.of(excecao.getMessage()),
				request.getRequestURI(), "SITUACAO_UNIDADE_EQUIPAMENTO_INVALIDA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
	}

	@ExceptionHandler(OperacaoInvalidaException.class)
	public ResponseEntity<ErroResponse> tratarOperacaoInvalidaUnidadeEquipamento(OperacaoInvalidaException excecao,
			HttpServletRequest request) {

		ErroResponse erro = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(),
				"Operação inválida para a unidade de equipamento", List.of(excecao.getMessage()),
				request.getRequestURI(), "OPERACAO_UNIDADE_EQUIPAMENTO_INVALIDA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
	}

	@ExceptionHandler(ReservaNaoEncontradaException.class)
	public ResponseEntity<ErroResponse> tratarReservaNaoEncontrado(ReservaNaoEncontradaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.NOT_FOUND.value(), "Reserva não encontrada",
				List.of(excecao.getMessage()), requisicao.getRequestURI(), "RESERVA_NAO_ENCONTRADA");

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
	}

	@ExceptionHandler(ClienteInativoParaReservaException.class)
	public ResponseEntity<ErroResponse> tratarClienteInativoParaReserva(ClienteInativoParaReservaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.FORBIDDEN.value(), "Acesso negado",
				List.of(excecao.getMessage()), requisicao.getRequestURI(), "CLIENTE_INATIVO");

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resposta);

	}

	@ExceptionHandler(DataInicialInvalidaException.class)
	public ResponseEntity<ErroResponse> tratarDataInicialParaReserva(DataInicialInvalidaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Data inicial inválida",
				List.of(excecao.getMessage()), requisicao.getRequestURI(), "DATA_INICIAL_INVALIDA");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);

	}

	@ExceptionHandler(DataFinalInvalidaException.class)
	public ResponseEntity<ErroResponse> tratarDataFinalParaReserva(DataFinalInvalidaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Data final inválida",
				List.of(excecao.getMessage()), requisicao.getRequestURI(), "DATA_FINAL_INVALIDA");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);

	}

	@ExceptionHandler(UnidadeDuplicadaNaReservaException.class)
	public ResponseEntity<ErroResponse> tratarUnidadeDuplicadaNaReserva(UnidadeDuplicadaNaReservaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(),
				"Unidade duplicada na reserva", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"UNIDADE_DUPLICADA_NA_RESERVA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler(ConflitoDeReservaException.class)
	public ResponseEntity<ErroResponse> tratarConflitoDeReserva(ConflitoDeReservaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(), "Conflito de reserva",
				List.of(excecao.getMessage()), requisicao.getRequestURI(), "CONFLITO_DE_RESERVA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler(UnidadeIndisponivelParaReservaException.class)
	public ResponseEntity<ErroResponse> tratarUnidadeIndisponivelParaReserva(
			UnidadeIndisponivelParaReservaException excecao, HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(),
				"Unidade indisponível para reserva", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"UNIDADE_INDISPONIVEL_PARA_RESERVA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler(OperacaoReservaInvalidaException.class)
	public ResponseEntity<ErroResponse> tratarOperacaoReservaInvalida(OperacaoReservaInvalidaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(),
				"Operação inválida para reserva", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"OPERACAO_RESERVA_INVALIDA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler(ReservaNaoAprovadaException.class)
	public ResponseEntity<ErroResponse> tratarReservaNaoAprovada(ReservaNaoAprovadaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(),
				"Operação de retirada inválida", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"RESERVA_NAO_APROVADA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler(RetiradaForaDoPeriodoException.class)
	public ResponseEntity<ErroResponse> tratarRetiradaForaDoPeriodo(RetiradaForaDoPeriodoException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(),
				"Retirada fora do período permitido", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"RETIRADA_FORA_DO_PERIODO");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler(ClienteInativoParaRetiradaException.class)
	public ResponseEntity<ErroResponse> tratarClienteInativoParaRetirada(ClienteInativoParaRetiradaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(),
				"Cliente inativo para retirada", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"CLIENTE_INATIVO_PARA_RETIRADA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler(RetiradaJaRealizadaException.class)
	public ResponseEntity<ErroResponse> tratarRetiradaJaRealizada(RetiradaJaRealizadaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(), "Retirada já realizada",
				List.of(excecao.getMessage()), requisicao.getRequestURI(), "RETIRADA_JA_REALIZADA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler(UnidadeInativaParaRetiradaException.class)
	public ResponseEntity<ErroResponse> tratarUnidadeInativaParaRetirada(UnidadeInativaParaRetiradaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(),
				"Unidade inativa para retirada", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"UNIDADE_INATIVA_PARA_RETIRADA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler(UnidadeIndisponivelParaRetiradaException.class)
	public ResponseEntity<ErroResponse> tratarUnidadeIndisponivelParaRetirada(
			UnidadeIndisponivelParaRetiradaException excecao, HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.CONFLICT.value(),
				"Unidade indisponível para retirada", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"UNIDADE_INDISPONIVEL_PARA_RETIRADA");

		return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	}

	@ExceptionHandler(ItemRetiradaDuplicadoException.class)
	public ResponseEntity<ErroResponse> tratarItemRetiradaDuplicado(ItemRetiradaDuplicadoException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
				"Item duplicado na retirada", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"ITEM_RETIRADA_DUPLICADO");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
	}

	@ExceptionHandler(ItensRetiradaDivergentesException.class)
	public ResponseEntity<ErroResponse> tratarItensRetiradaDivergentes(ItensRetiradaDivergentesException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
				"Itens da retirada divergentes", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"ITENS_RETIRADA_DIVERGENTES");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
	}

	@ExceptionHandler(ObservacaoObrigatoriaException.class)
	public ResponseEntity<ErroResponse> tratarObservacaoObrigatoria(ObservacaoObrigatoriaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
				"Observação obrigatória", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"OBSERVACAO_OBRIGATORIA");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
	}

	@ExceptionHandler(DescricaoMetodoVerificacaoObrigatoriaException.class)
	public ResponseEntity<ErroResponse> tratarDescricaoMetodoVerificacaoObrigatoria(
			DescricaoMetodoVerificacaoObrigatoriaException excecao, HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
				"Descrição do método de verificação obrigatória", List.of(excecao.getMessage()),
				requisicao.getRequestURI(), "DESCRICAO_METODO_VERIFICACAO_OBRIGATORIA");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
	}

	@ExceptionHandler(ConfirmacaoRetiradaObrigatoriaException.class)
	public ResponseEntity<ErroResponse> tratarConfirmacaoRetiradaObrigatoria(
			ConfirmacaoRetiradaObrigatoriaException excecao, HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
				"Confirmação da retirada obrigatória", List.of(excecao.getMessage()), requisicao.getRequestURI(),
				"CONFIRMACAO_RETIRADA_OBRIGATORIA");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
	}

	@ExceptionHandler(RetiradaNaoEncontradaException.class)
	public ResponseEntity<ErroResponse> tratarRetiradaNaoEncontrada(RetiradaNaoEncontradaException excecao,
			HttpServletRequest requisicao) {

		ErroResponse resposta = new ErroResponse(Instant.now(), HttpStatus.NOT_FOUND.value(), "Retirada não encontrada",
				List.of(excecao.getMessage()), requisicao.getRequestURI(), "RETIRADA_NAO_ENCONTRADA");

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
	}

}
