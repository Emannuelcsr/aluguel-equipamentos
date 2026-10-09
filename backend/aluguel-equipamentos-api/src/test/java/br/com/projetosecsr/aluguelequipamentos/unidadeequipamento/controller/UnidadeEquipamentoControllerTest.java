package br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.configuracao.ConfiguracaoDeSeguranca;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.erro.TratadorGlobalDeErros;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorAcessoNegado;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorFalhaAutenticacao;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.request.CadastrarUnidadeEquipamentoRequest;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.response.UnidadeEquipamentoResponse;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.service.UnidadeEquipamentoService;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;

import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import java.util.List;

import org.springframework.data.domain.Pageable;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.OperacaoInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoJaAtivaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoJaInativaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoNaoEncontradoException;

@WebMvcTest(controllers = UnidadeEquipamentoController.class)
@Import({ ConfiguracaoDeSeguranca.class, TratadorGlobalDeErros.class, TratadorFalhaAutenticacao.class,
		TratadorAcessoNegado.class })
public class UnidadeEquipamentoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UnidadeEquipamentoService unidadeEquipamentoService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void deveCadastrarUnidadeEquipamentoQuandoAutenticadoComoAdministrador() throws Exception {

		// PREPARAR
		Long equipamentoId = 2L;

		String corpoRequisicao = """
				{
				  "equipamentoId": 2
				}
				""";

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-10-09T15:00:00Z");

		UnidadeEquipamentoResponse respostaDoService = new UnidadeEquipamentoResponse(3L, "UNI-000003", equipamentoId,
				"Marreta", "DISPONIVEL", true, data, data);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(unidadeEquipamentoService.cadastrar(any(CadastrarUnidadeEquipamentoRequest.class)))
				.thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				post("/api/unidades-equipamento").header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(3))
				.andExpect(jsonPath("$.codigo").value("UNI-000003")).andExpect(jsonPath("$.equipamentoId").value(2))
				.andExpect(jsonPath("$.equipamentoNome").value("Marreta"))
				.andExpect(jsonPath("$.status").value("DISPONIVEL")).andExpect(jsonPath("$.ativo").value(true));

		verify(jwtDecoder).decode("token-administrador");

		verify(unidadeEquipamentoService).cadastrar(any(CadastrarUnidadeEquipamentoRequest.class));
	}

	@Test
	void deveRetornarAcessoNegadoQuandoFuncionarioTentarCadastrarUnidadeEquipamento() throws Exception {

		String corpoRequisicao = """
				{
				  "equipamentoId": 2
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		ResultActions resultado = mockMvc
				.perform(post("/api/unidades-equipamento").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		resultado.andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));

		verify(jwtDecoder).decode("token-funcionario");
		verify(unidadeEquipamentoService, never()).cadastrar(any(CadastrarUnidadeEquipamentoRequest.class));
	}

	@Test
	void deveBuscarUnidadePorIdQuandoAutenticadoComoFuncionario() throws Exception {

		Long unidadeId = 3L;
		Instant data = Instant.parse("2026-10-09T15:00:00Z");

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		UnidadeEquipamentoResponse respostaDoService = new UnidadeEquipamentoResponse(unidadeId, "UNI-000003", 2L,
				"Marreta", "DISPONIVEL", true, data, data);

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(unidadeEquipamentoService.buscarPorId(unidadeId)).thenReturn(respostaDoService);

		ResultActions resultado = mockMvc.perform(get("/api/unidades-equipamento/{id}", unidadeId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(3))
				.andExpect(jsonPath("$.codigo").value("UNI-000003")).andExpect(jsonPath("$.equipamentoId").value(2))
				.andExpect(jsonPath("$.equipamentoNome").value("Marreta"))
				.andExpect(jsonPath("$.status").value("DISPONIVEL")).andExpect(jsonPath("$.ativo").value(true));

		verify(jwtDecoder).decode("token-funcionario");
		verify(unidadeEquipamentoService).buscarPorId(unidadeId);
	}

	@Test
	void deveRetornarNaoEncontradoQuandoUnidadeNaoExistir() throws Exception {

		Long unidadeId = 999L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(unidadeEquipamentoService.buscarPorId(unidadeId))
				.thenThrow(new UnidadeEquipamentoNaoEncontradoException());

		ResultActions resultado = mockMvc.perform(get("/api/unidades-equipamento/{id}", unidadeId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		resultado.andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.erro").value("Unidade de equipamento não encontrada"))
				.andExpect(jsonPath("$.mensagens[0]").value("Unidade de equipamento não encontrada."))
				.andExpect(jsonPath("$.codigo").value("UNIDADE_EQUIPAMENTO_NAO_ENCONTRADA"));

		verify(unidadeEquipamentoService).buscarPorId(unidadeId);
	}

	@Test
	void deveListarUnidadesComPaginacaoQuandoAutenticadoComoFuncionario() throws Exception {

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		Instant data = Instant.parse("2026-10-09T15:00:00Z");

		UnidadeEquipamentoResponse unidade1 = new UnidadeEquipamentoResponse(3L, "UNI-000003", 10L, "Marreta",
				"DISPONIVEL", true, data, data);

		UnidadeEquipamentoResponse unidade2 = new UnidadeEquipamentoResponse(4L, "UNI-000004", 20L, "Furadeira",
				"EM_MANUTENCAO", true, data, data);

		PaginaResponse<UnidadeEquipamentoResponse> pagina = new PaginaResponse<>(List.of(unidade1, unidade2), 1, 2, 2,
				5L, 3, true, false);

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(unidadeEquipamentoService.listarPaginado(any(Pageable.class))).thenReturn(pagina);

		ResultActions resultado = mockMvc.perform(get("/api/unidades-equipamento").param("page", "1").param("size", "2")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.conteudo.length()").value(2))
				.andExpect(jsonPath("$.conteudo[0].id").value(3))
				.andExpect(jsonPath("$.conteudo[0].codigo").value("UNI-000003"))
				.andExpect(jsonPath("$.conteudo[1].id").value(4))
				.andExpect(jsonPath("$.conteudo[1].status").value("EM_MANUTENCAO"))
				.andExpect(jsonPath("$.paginaAtual").value(1)).andExpect(jsonPath("$.tamanho").value(2))
				.andExpect(jsonPath("$.totalElementos").value(5)).andExpect(jsonPath("$.totalPaginas").value(3));

		verify(unidadeEquipamentoService).listarPaginado(any(Pageable.class));
	}

	@Test
	void deveRetornarBadRequestQuandoPaginaNaoExistir() throws Exception {

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(unidadeEquipamentoService.listarPaginado(any(Pageable.class)))
				.thenThrow(new PaginaInvalidaException("A página informada não existe"));

		ResultActions resultado = mockMvc.perform(get("/api/unidades-equipamento").param("page", "4").param("size", "2")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		resultado.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.codigo").value("PAGINA_NAO_ENCONTRADA"));

		verify(unidadeEquipamentoService).listarPaginado(any(Pageable.class));
	}

	@Test
	void deveAtivarUnidadeQuandoAutenticadoComoAdministrador() throws Exception {

		Long unidadeId = 3L;
		Instant data = Instant.parse("2026-10-09T15:00:00Z");

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		UnidadeEquipamentoResponse resposta = new UnidadeEquipamentoResponse(unidadeId, "UNI-000003", 2L, "Marreta",
				"DISPONIVEL", true, data, data);

		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(unidadeEquipamentoService.ativar(unidadeId)).thenReturn(resposta);

		mockMvc.perform(patch("/api/unidades-equipamento/{id}/ativar", unidadeId).header(HttpHeaders.AUTHORIZATION,
				"Bearer token-administrador")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(3))
				.andExpect(jsonPath("$.ativo").value(true));

		verify(unidadeEquipamentoService).ativar(unidadeId);
	}

	@Test
	void deveDesativarUnidadeQuandoAutenticadoComoAdministrador() throws Exception {

		Long unidadeId = 3L;
		Instant data = Instant.parse("2026-10-09T15:00:00Z");

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		UnidadeEquipamentoResponse resposta = new UnidadeEquipamentoResponse(unidadeId, "UNI-000003", 2L, "Marreta",
				"DISPONIVEL", false, data, data);

		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(unidadeEquipamentoService.desativar(unidadeId)).thenReturn(resposta);

		mockMvc.perform(patch("/api/unidades-equipamento/{id}/desativar", unidadeId).header(HttpHeaders.AUTHORIZATION,
				"Bearer token-administrador")).andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));

		verify(unidadeEquipamentoService).desativar(unidadeId);
	}

	@Test
	void deveRetornarAcessoNegadoQuandoFuncionarioTentarAtivarUnidade() throws Exception {

		Long unidadeId = 3L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		mockMvc.perform(patch("/api/unidades-equipamento/{id}/ativar", unidadeId).header(HttpHeaders.AUTHORIZATION,
				"Bearer token-funcionario")).andExpect(status().isForbidden());

		verify(unidadeEquipamentoService, never()).ativar(unidadeId);
	}

	@Test
	void deveRetornarAcessoNegadoQuandoFuncionarioTentarDesativarUnidade() throws Exception {

		Long unidadeId = 3L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		mockMvc.perform(patch("/api/unidades-equipamento/{id}/desativar", unidadeId).header(HttpHeaders.AUTHORIZATION,
				"Bearer token-funcionario")).andExpect(status().isForbidden());

		verify(unidadeEquipamentoService, never()).desativar(unidadeId);
	}

	@Test
	void deveEnviarUnidadeParaManutencaoQuandoAutenticadoComoFuncionario() throws Exception {

		Long unidadeId = 3L;
		Instant data = Instant.parse("2026-10-09T15:00:00Z");

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		UnidadeEquipamentoResponse resposta = new UnidadeEquipamentoResponse(unidadeId, "UNI-000003", 2L, "Marreta",
				"EM_MANUTENCAO", true, data, data);

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(unidadeEquipamentoService.enviarParaManutencao(unidadeId)).thenReturn(resposta);

		mockMvc.perform(patch("/api/unidades-equipamento/{id}/manutencao", unidadeId).header(HttpHeaders.AUTHORIZATION,
				"Bearer token-funcionario")).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("EM_MANUTENCAO"));

		verify(unidadeEquipamentoService).enviarParaManutencao(unidadeId);
	}

	@Test
	void deveLiberarUnidadeDaManutencaoQuandoAutenticadoComoFuncionario() throws Exception {

		Long unidadeId = 3L;
		Instant data = Instant.parse("2026-10-09T15:00:00Z");

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		UnidadeEquipamentoResponse resposta = new UnidadeEquipamentoResponse(unidadeId, "UNI-000003", 2L, "Marreta",
				"DISPONIVEL", true, data, data);

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(unidadeEquipamentoService.liberarDaManutencao(unidadeId)).thenReturn(resposta);

		mockMvc.perform(patch("/api/unidades-equipamento/{id}/liberar-manutencao", unidadeId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")).andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DISPONIVEL"));

		verify(unidadeEquipamentoService).liberarDaManutencao(unidadeId);
	}

	@Test
	void deveRetornarConflitoQuandoOperacaoDeManutencaoForInvalida() throws Exception {

		Long unidadeId = 3L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(unidadeEquipamentoService.enviarParaManutencao(unidadeId)).thenThrow(new OperacaoInvalidaException(
				"A unidade só pode ser enviada para manutenção quando estiver disponível."));

		mockMvc.perform(patch("/api/unidades-equipamento/{id}/manutencao", unidadeId).header(HttpHeaders.AUTHORIZATION,
				"Bearer token-funcionario")).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.erro").value("Operação inválida para a unidade de equipamento"))
				.andExpect(jsonPath("$.codigo").value("OPERACAO_UNIDADE_EQUIPAMENTO_INVALIDA"));

		verify(unidadeEquipamentoService).enviarParaManutencao(unidadeId);
	}

	@Test
	void deveRetornarConflitoQuandoTentarAtivarUnidadeJaAtiva() throws Exception {

		Long unidadeId = 3L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(unidadeEquipamentoService.ativar(unidadeId)).thenThrow(new UnidadeEquipamentoJaAtivaException());

		mockMvc.perform(patch("/api/unidades-equipamento/{id}/ativar", unidadeId).header(HttpHeaders.AUTHORIZATION,
				"Bearer token-administrador")).andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.codigo").value("SITUACAO_UNIDADE_EQUIPAMENTO_INVALIDA"));

		verify(unidadeEquipamentoService).ativar(unidadeId);
	}

	@Test
	void deveRetornarConflitoQuandoTentarDesativarUnidadeJaInativa() throws Exception {

		Long unidadeId = 3L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(unidadeEquipamentoService.desativar(unidadeId)).thenThrow(new UnidadeEquipamentoJaInativaException());

		mockMvc.perform(patch("/api/unidades-equipamento/{id}/desativar", unidadeId).header(HttpHeaders.AUTHORIZATION,
				"Bearer token-administrador")).andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.codigo").value("SITUACAO_UNIDADE_EQUIPAMENTO_INVALIDA"));

		verify(unidadeEquipamentoService).desativar(unidadeId);
	}

}
