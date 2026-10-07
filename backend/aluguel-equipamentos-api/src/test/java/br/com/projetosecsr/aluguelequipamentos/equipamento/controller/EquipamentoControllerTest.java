package br.com.projetosecsr.aluguelequipamentos.equipamento.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.configuracao.ConfiguracaoDeSeguranca;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.erro.TratadorGlobalDeErros;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorAcessoNegado;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorFalhaAutenticacao;
import br.com.projetosecsr.aluguelequipamentos.equipamento.excecao.EquipamentoJaAtivoException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.excecao.EquipamentoJaInativoException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.excecao.EquipamentoNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.request.AtualizarEquipamentoRequest;
import br.com.projetosecsr.aluguelequipamentos.equipamento.request.CadastrarEquipamentoRequest;
import br.com.projetosecsr.aluguelequipamentos.equipamento.response.EquipamentoResponse;
import br.com.projetosecsr.aluguelequipamentos.equipamento.service.EquipamentoService;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;

@WebMvcTest(controllers = EquipamentoController.class)
@Import({ ConfiguracaoDeSeguranca.class, TratadorGlobalDeErros.class, TratadorFalhaAutenticacao.class,
		TratadorAcessoNegado.class })
public class EquipamentoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EquipamentoService equipamentoService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void deveCadastrarEquipamentoQuandoAutenticadoComoAdministrador() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				  "nome": "KKKK",
				  "descricao": "AIAIAI iiii",
				  "valorDiaria": 300.00,
				  "categoriaId": 1
				}
				""";

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-09-18T15:00:00Z");

		EquipamentoResponse respostaDoService = new EquipamentoResponse(10L, "KKKK", "AIAIAI iiii", 1L, "lllllll",
				new BigDecimal("300.00"), true, data, data);

		// MOCKS

		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(equipamentoService.cadastrar(any(CadastrarEquipamentoRequest.class))).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/equipamentos").header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.nome").value("KKKK")).andExpect(jsonPath("$.descricao").value("AIAIAI iiii"))
				.andExpect(jsonPath("$.valorDiaria").value(300.00)).andExpect(jsonPath("$.categoriaId").value(1))
				.andExpect(jsonPath("$.categoriaNome").value("lllllll")).andExpect(jsonPath("$.ativo").value(true));

		verify(jwtDecoder).decode("token-administrador");

		verify(equipamentoService).cadastrar(any(CadastrarEquipamentoRequest.class));

	}

	@Test
	void deveRetornarAcessoNegadoQuandoFuncionarioTentarCadastrarEquipamento() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				  "nome": "KKKK",
				  "descricao": "AIAIAI iiii",
				  "valorDiaria": 300.00,
				  "categoriaId": 1
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/equipamentos").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.erro").value("Acesso negado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Você não possui permissão para acessar este recurso."))
				.andExpect(jsonPath("$.path").value("/api/equipamentos"))
				.andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));

		verify(jwtDecoder).decode("token-funcionario");

		verify(equipamentoService, never()).cadastrar(any(CadastrarEquipamentoRequest.class));
	}

	@Test
	void deveBuscarEquipamentoPorIdQuandoAutenticadoComoFuncionario() throws Exception {

		// PREPARAR
		Long equipamentoId = 10L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		Instant data = Instant.parse("2026-09-18T15:00:00Z");

		EquipamentoResponse respostaDoService = new EquipamentoResponse(equipamentoId, "Marreta",
				"Marreta com cabo de madeira", 1L, "FERRAMENTAS", new BigDecimal("300.00"), true, data, data);

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(equipamentoService.buscarPorId(equipamentoId)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(get("/api/equipamentos/{id}", equipamentoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.nome").value("Marreta"))
				.andExpect(jsonPath("$.descricao").value("Marreta com cabo de madeira"))
				.andExpect(jsonPath("$.valorDiaria").value(300.00)).andExpect(jsonPath("$.categoriaId").value(1))
				.andExpect(jsonPath("$.categoriaNome").value("FERRAMENTAS")).andExpect(jsonPath("$.ativo").value(true));

		verify(jwtDecoder).decode("token-funcionario");
		verify(equipamentoService).buscarPorId(equipamentoId);
	}

	@Test
	void deveRetornarNaoEncontradoQuandoEquipamentoNaoExistir() throws Exception {

		// PREPARAR
		Long equipamentoId = 999L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(equipamentoService.buscarPorId(equipamentoId)).thenThrow(new EquipamentoNaoEncontradoException());

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(get("/api/equipamentos/{id}", equipamentoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.erro").value("Equipamento não encontrado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Equipamento não encontrado."))
				.andExpect(jsonPath("$.path").value("/api/equipamentos/999"))
				.andExpect(jsonPath("$.codigo").value("EQUIPAMENTO_NAO_ENCONTRADO"));

		verify(jwtDecoder).decode("token-funcionario");
		verify(equipamentoService).buscarPorId(equipamentoId);
	}

	@Test
	void deveListarEquipamentosComPaginacaoQuandoAutenticadoComoFuncionario() throws Exception {

		// PREPARAR
		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		Instant data = Instant.parse("2026-09-18T15:00:00Z");

		EquipamentoResponse equipamento1 = new EquipamentoResponse(10L, "Marreta", "Marreta com cabo de madeira", 1L,
				"FERRAMENTAS", new BigDecimal("50.00"), true, data, data);

		EquipamentoResponse equipamento2 = new EquipamentoResponse(20L, "Furadeira", "Furadeira elétrica", 1L,
				"FERRAMENTAS", new BigDecimal("80.00"), true, data, data);

		PaginaResponse<EquipamentoResponse> paginaDoService = new PaginaResponse<>(List.of(equipamento1, equipamento2),
				1, 2, 2, 5L, 3, true, false);

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(equipamentoService.listarPaginado(any(Pageable.class))).thenReturn(paginaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(get("/api/equipamentos").param("page", "1").param("size", "2")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.conteudo.length()").value(2))

				.andExpect(jsonPath("$.conteudo[0].id").value(10))
				.andExpect(jsonPath("$.conteudo[0].nome").value("Marreta"))
				.andExpect(jsonPath("$.conteudo[0].valorDiaria").value(50.00))
				.andExpect(jsonPath("$.conteudo[0].categoriaId").value(1))
				.andExpect(jsonPath("$.conteudo[0].categoriaNome").value("FERRAMENTAS"))

				.andExpect(jsonPath("$.conteudo[1].id").value(20))
				.andExpect(jsonPath("$.conteudo[1].nome").value("Furadeira"))
				.andExpect(jsonPath("$.conteudo[1].valorDiaria").value(80.00))

				.andExpect(jsonPath("$.paginaAtual").value(1)).andExpect(jsonPath("$.tamanho").value(2))
				.andExpect(jsonPath("$.quantidadeElementos").value(2)).andExpect(jsonPath("$.totalElementos").value(5))
				.andExpect(jsonPath("$.totalPaginas").value(3)).andExpect(jsonPath("$.primeiraPagina").value(true))
				.andExpect(jsonPath("$.ultimaPagina").value(false));

		verify(jwtDecoder).decode("token-funcionario");

		ArgumentCaptor<Pageable> paginacaoCaptor = ArgumentCaptor.forClass(Pageable.class);

		verify(equipamentoService).listarPaginado(paginacaoCaptor.capture());

		Pageable paginacaoRecebida = paginacaoCaptor.getValue();

		assertEquals(0, paginacaoRecebida.getPageNumber());
		assertEquals(2, paginacaoRecebida.getPageSize());
	}

	@Test
	void deveRetornarBadRequestQuandoPaginaSolicitadaNaoExistir() throws Exception {

		// PREPARAR
		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(equipamentoService.listarPaginado(any(Pageable.class)))
				.thenThrow(new PaginaInvalidaException("A página informada não existe"));

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(get("/api/equipamentos").param("page", "4").param("size", "2")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.erro").value("Parâmetros de paginação inválidos"))
				.andExpect(jsonPath("$.mensagens[0]").value("A página informada não existe"))
				.andExpect(jsonPath("$.path").value("/api/equipamentos"))
				.andExpect(jsonPath("$.codigo").value("PAGINA_NAO_ENCONTRADA"));

		verify(jwtDecoder).decode("token-funcionario");
		verify(equipamentoService).listarPaginado(any(Pageable.class));
	}

	@Test
	void deveAtualizarEquipamentoQuandoAutenticadoComoAdministrador() throws Exception {

		// PREPARAR
		Long equipamentoId = 10L;

		String corpoRequisicao = """
				{
				  "nome": "Marreta Atualizada",
				  "descricao": "Marreta reforçada",
				  "valorDiaria": 350.00,
				  "categoriaId": 1
				}
				""";

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-09-18T15:00:00Z");

		EquipamentoResponse respostaDoService = new EquipamentoResponse(equipamentoId, "Marreta Atualizada",
				"Marreta reforçada", 1L, "FERRAMENTAS", new BigDecimal("350.00"), true, data, data);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(equipamentoService.atualizar(eq(equipamentoId), any(AtualizarEquipamentoRequest.class)))
				.thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(put("/api/equipamentos/{id}", equipamentoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador").contentType(MediaType.APPLICATION_JSON)
				.content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.nome").value("Marreta Atualizada"))
				.andExpect(jsonPath("$.descricao").value("Marreta reforçada"))
				.andExpect(jsonPath("$.valorDiaria").value(350.00)).andExpect(jsonPath("$.categoriaId").value(1))
				.andExpect(jsonPath("$.categoriaNome").value("FERRAMENTAS")).andExpect(jsonPath("$.ativo").value(true));

		verify(jwtDecoder).decode("token-administrador");

		verify(equipamentoService).atualizar(eq(equipamentoId), any(AtualizarEquipamentoRequest.class));
	}

	@Test
	void deveRetornarAcessoNegadoQuandoFuncionarioTentarAtualizarEquipamento() throws Exception {

		// PREPARAR
		Long equipamentoId = 10L;

		String corpoRequisicao = """
				{
				  "nome": "Marreta Atualizada",
				  "descricao": "Marreta reforçada",
				  "valorDiaria": 350.00,
				  "categoriaId": 1
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(put("/api/equipamentos/{id}", equipamentoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario").contentType(MediaType.APPLICATION_JSON)
				.content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.erro").value("Acesso negado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Você não possui permissão para acessar este recurso."))
				.andExpect(jsonPath("$.path").value("/api/equipamentos/10"))
				.andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));

		verify(jwtDecoder).decode("token-funcionario");

		verify(equipamentoService, never()).atualizar(eq(equipamentoId), any(AtualizarEquipamentoRequest.class));
	}

	@Test
	void deveAtivarEquipamentoQuandoAutenticadoComoAdministrador() throws Exception {

		// PREPARAR
		Long equipamentoId = 10L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-09-18T15:00:00Z");

		EquipamentoResponse respostaDoService = new EquipamentoResponse(equipamentoId, "Marreta", "Marreta reforçada",
				1L, "FERRAMENTAS", new BigDecimal("350.00"), true, data, data);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(equipamentoService.ativar(equipamentoId)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/equipamentos/{id}/ativar", equipamentoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.ativo").value(true));

		verify(jwtDecoder).decode("token-administrador");
		verify(equipamentoService).ativar(equipamentoId);
	}

	@Test
	void deveDesativarEquipamentoQuandoAutenticadoComoAdministrador() throws Exception {

		// PREPARAR
		Long equipamentoId = 10L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-09-18T15:00:00Z");

		EquipamentoResponse respostaDoService = new EquipamentoResponse(equipamentoId, "Marreta", "Marreta reforçada",
				1L, "FERRAMENTAS", new BigDecimal("350.00"), false, data, data);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(equipamentoService.desativar(equipamentoId)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/equipamentos/{id}/desativar", equipamentoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.ativo").value(false));

		verify(jwtDecoder).decode("token-administrador");
		verify(equipamentoService).desativar(equipamentoId);
	}

	@Test
	void deveRetornarAcessoNegadoQuandoFuncionarioTentarAtivarEquipamento() throws Exception {

		// PREPARAR
		Long equipamentoId = 10L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/equipamentos/{id}/ativar", equipamentoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isForbidden());

		verify(jwtDecoder).decode("token-funcionario");
		verifyNoInteractions(equipamentoService);
	}

	@Test
	void deveRetornarAcessoNegadoQuandoFuncionarioTentarDesativarEquipamento() throws Exception {

		// PREPARAR
		Long equipamentoId = 10L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/equipamentos/{id}/desativar", equipamentoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isForbidden());

		verify(jwtDecoder).decode("token-funcionario");
		verifyNoInteractions(equipamentoService);
	}

	@Test
	void deveRetornarConflitoQuandoTentarAtivarEquipamentoJaAtivo() throws Exception {

		// PREPARAR
		Long equipamentoId = 10L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(equipamentoService.ativar(equipamentoId)).thenThrow(new EquipamentoJaAtivoException());

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/equipamentos/{id}/ativar", equipamentoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.erro").value("Conflito de situação do equipamento"))
				.andExpect(jsonPath("$.mensagens[0]").value("O equipamento já está ativo."))
				.andExpect(jsonPath("$.path").value("/api/equipamentos/10/ativar"))
				.andExpect(jsonPath("$.codigo").value("SITUACAO_EQUIPAMENTO_INVALIDA"));

		verify(jwtDecoder).decode("token-administrador");
		verify(equipamentoService).ativar(equipamentoId);
	}

	@Test
	void deveRetornarConflitoQuandoTentarDesativarEquipamentoJaInativo() throws Exception {

		// PREPARAR
		Long equipamentoId = 10L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(equipamentoService.desativar(equipamentoId)).thenThrow(new EquipamentoJaInativoException());

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/equipamentos/{id}/desativar", equipamentoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.erro").value("Conflito de situação do equipamento"))
				.andExpect(jsonPath("$.mensagens[0]").value("O equipamento já está inativo."))
				.andExpect(jsonPath("$.path").value("/api/equipamentos/10/desativar"))
				.andExpect(jsonPath("$.codigo").value("SITUACAO_EQUIPAMENTO_INVALIDA"));

		verify(jwtDecoder).decode("token-administrador");
		verify(equipamentoService).desativar(equipamentoId);
	}

}
