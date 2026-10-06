package br.com.projetosecsr.aluguelequipamentos.categoria.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaJaAtivaException;
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaJaInativaException;
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.categoria.request.AtualizarCategoriaRequest;
import br.com.projetosecsr.aluguelequipamentos.categoria.request.CadastrarCategoriaRequest;
import br.com.projetosecsr.aluguelequipamentos.categoria.response.CategoriaResponse;
import br.com.projetosecsr.aluguelequipamentos.categoria.service.CategoriaService;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.configuracao.ConfiguracaoDeSeguranca;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.erro.TratadorGlobalDeErros;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorAcessoNegado;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorFalhaAutenticacao;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;

@WebMvcTest(controllers = CategoriaController.class)
@Import({ ConfiguracaoDeSeguranca.class, TratadorGlobalDeErros.class, TratadorFalhaAutenticacao.class,
		TratadorAcessoNegado.class })
public class CategoriaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CategoriaService categoriaService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void deveCadastrarCategoriaQuandoAutenticadoComoAdministrador() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				  "nome": "Empilhadeira",
				  "descricao": "Empilhadeira amarela"
				}
				""";

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-09-18T15:00:00Z");

		CategoriaResponse respostaDoService = new CategoriaResponse(10L, "Empilhadeira", "Empilhadeira amarela", true,
				data, data);

		// MOCKS

		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(categoriaService.cadastrar(any(CadastrarCategoriaRequest.class))).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/categorias").header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.nome").value("Empilhadeira"))
				.andExpect(jsonPath("$.descricao").value("Empilhadeira amarela"))
				.andExpect(jsonPath("$.ativo").value("true"));

		verify(jwtDecoder).decode("token-administrador");

		verify(categoriaService).cadastrar(any(CadastrarCategoriaRequest.class));

	}

	@Test
	void deveNegarCadastroDeCategoriaQuandoAutenticadoComoFuncionario() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				  "nome": "Empilhadeira",
				  "descricao": "Empilhadeira amarela"
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/categorias").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.erro").value("Acesso negado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Você não possui permissão para acessar este recurso."))
				.andExpect(jsonPath("$.path").value("/api/categorias"))
				.andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
		;

		verify(jwtDecoder).decode("token-funcionario");

		verifyNoInteractions(categoriaService);

	}

	@Test
	void deveBuscarCategoriaPorIdQuandoAutenticadoComoAdministrador() throws Exception {

		// PREPARAR
		Long categoriaId = 10L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-09-24T15:00:00Z");

		CategoriaResponse respostaDoService = new CategoriaResponse(10L, "Empilhadeira", "Empilhadeira amarela", true,
				data, data);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(categoriaService.buscarPorId(categoriaId)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(get("/api/categorias/{id}", categoriaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.nome").value("Empilhadeira"))
				.andExpect(jsonPath("$.descricao").value("Empilhadeira amarela"))
				.andExpect(jsonPath("$.ativo").value(true));

		verify(jwtDecoder).decode("token-administrador");

		verify(categoriaService).buscarPorId(categoriaId);
	}

	@Test
	void deveBuscarCategoriaPorIdQuandoAutenticadoComoFuncionario() throws Exception {

		// PREPARAR
		Long categoriaId = 10L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		Instant data = Instant.parse("2026-09-24T15:00:00Z");

		CategoriaResponse respostaDoService = new CategoriaResponse(10L, "Empilhadeira", "Empilhadeira amarela", true,
				data, data);

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(categoriaService.buscarPorId(categoriaId)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				get("/api/categorias/{id}", categoriaId).header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.nome").value("Empilhadeira"))
				.andExpect(jsonPath("$.descricao").value("Empilhadeira amarela"))
				.andExpect(jsonPath("$.ativo").value(true));

		verify(jwtDecoder).decode("token-funcionario");

		verify(categoriaService).buscarPorId(categoriaId);
	}

	@Test
	void deveRetornarNaoEncontradoQuandoCategoriaNaoExistir() throws Exception {

		// PREPARAR
		Long categoriaId = 999L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		// MOCKS

		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(categoriaService.buscarPorId(categoriaId)).thenThrow(new CategoriaNaoEncontradoException());

		// EXECUTAR

		ResultActions resultado = mockMvc.perform(get("/api/categorias/{id}", categoriaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.erro").value("Categoria não encontrada"))
				.andExpect(jsonPath("$.mensagens[0]").value("Categoria não encontrada."))
				.andExpect(jsonPath("$.path").value("/api/categorias/999"))
				.andExpect(jsonPath("$.codigo").value("CATEGORIA_NAO_ENCONTRADO"));

		verify(jwtDecoder).decode("token-administrador");

		verify(categoriaService).buscarPorId(categoriaId);

	}

	@Test
	void deveListarCategoriasComPaginacaoQuandoAutenticadoComoAdministrador() throws Exception {

		// PREPARAR
		Long categoriaId = 1L;
		Long categoriaId2 = 2L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-09-24T15:00:00Z");

		CategoriaResponse respostaDoService = new CategoriaResponse(categoriaId, "Empilhadeira", "Empilhadeira amarela",
				true, data, data);

		CategoriaResponse respostaDoServices = new CategoriaResponse(categoriaId2, "Empilhadeiras",
				"Empilhadeira amarelas", true, data, data);

		PaginaResponse<CategoriaResponse> respostasDoService = new PaginaResponse<>(
				List.of(respostaDoService, respostaDoServices), 1, 2, 2, 5L, 3, true, false);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(categoriaService.listarPaginado(any(Pageable.class))).thenReturn(respostasDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(get("/api/categorias").param("page", "1").param("size", "2")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.conteudo.length()").value(2))
				.andExpect(jsonPath("$.conteudo[0].id").value(categoriaId))
				.andExpect(jsonPath("$.conteudo[0].nome").value("Empilhadeira"))
				.andExpect(jsonPath("$.conteudo[0].descricao").value("Empilhadeira amarela"))
				.andExpect(jsonPath("$.conteudo[1].id").value(categoriaId2))
				.andExpect(jsonPath("$.conteudo[1].nome").value("Empilhadeiras"))
				.andExpect(jsonPath("$.conteudo[1].descricao").value("Empilhadeira amarelas"))
				.andExpect(jsonPath("$.paginaAtual").value(1)).andExpect(jsonPath("$.tamanho").value(2))
				.andExpect(jsonPath("$.quantidadeElementos").value(2)).andExpect(jsonPath("$.totalElementos").value(5))
				.andExpect(jsonPath("$.totalPaginas").value(3)).andExpect(jsonPath("$.primeiraPagina").value(true))
				.andExpect(jsonPath("$.ultimaPagina").value(false));

		verify(jwtDecoder).decode("token-administrador");

		ArgumentCaptor<Pageable> paginacaoCaptor = ArgumentCaptor.forClass(Pageable.class);

		verify(categoriaService).listarPaginado(paginacaoCaptor.capture());

		Pageable paginacaoRecebida = paginacaoCaptor.getValue();

		assertEquals(0, paginacaoRecebida.getPageNumber());
		assertEquals(2, paginacaoRecebida.getPageSize());

	}

	@Test
	void deveRetornarBadRequestQuandoPaginaSolicitadaNaoExistir() throws Exception {

		// PREPARAR
		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(categoriaService.listarPaginado(any(Pageable.class)))
				.thenThrow(new PaginaInvalidaException("A página informada não existe"));

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(get("/api/categorias").param("page", "4").param("size", "2")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.erro").value("Parâmetros de paginação inválidos"))
				.andExpect(jsonPath("$.mensagens[0]").value("A página informada não existe"))
				.andExpect(jsonPath("$.path").value("/api/categorias"))
				.andExpect(jsonPath("$.codigo").value("PAGINA_NAO_ENCONTRADA"));

		verify(jwtDecoder).decode("token-funcionario");

		verify(categoriaService).listarPaginado(any(Pageable.class));
	}

	@Test
	void deveAtualizarCategoriaQuandoAutenticadoComoAdministradorEDadosForemValidos() throws Exception {
		// PREPARAR
		Long catId = 10L;

		String corpoRequisicao = """
				{
				  "nome": "Empilhadeiras",
				  "descricao": "Empilhadseira amarela"
				}
				""";

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant dataAtualizacao = Instant.parse("2026-09-28T15:00:00Z");

		CategoriaResponse respostaDoService = new CategoriaResponse(catId, "Empilhadeiras", "Empilhadseira amarela",
				true, dataAtualizacao, dataAtualizacao);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);
		when(categoriaService.atualizar(any(AtualizarCategoriaRequest.class), eq(catId))).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				put("/api/categorias/{id}", catId).header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.nome").value("Empilhadeiras"))
				.andExpect(jsonPath("$.descricao").value("Empilhadseira amarela"))
				.andExpect(jsonPath("$.ativo").value(true));

		verify(jwtDecoder).decode("token-administrador");

		verify(categoriaService).atualizar(any(AtualizarCategoriaRequest.class), eq(catId));
	}

	@Test
	void deveRetornarAcessoNegadoQuandoFuncionarioTentarAtualizarCategoria() throws Exception {

		// PREPARAR
		Long catId = 10L;

		String corpoRequisicao = """
				{
				  "nome": "Empilhadeiras",
				  "descricao": "Empilhadseira amarela"
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				put("/api/categorias/{id}", catId).header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.erro").value("Acesso negado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Você não possui permissão para acessar este recurso."))
				.andExpect(jsonPath("$.path").value("/api/categorias/10"))
				.andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));

		verify(jwtDecoder).decode("token-funcionario");

		verifyNoInteractions(categoriaService);
	}

	@Test
	void deveAtivarCategoriaQuandoAutenticadoComoAdministrador() throws Exception {

		// PREPARAR
		Long categoriaId = 10L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-09-28T15:00:00Z");

		CategoriaResponse respostaDoService = new CategoriaResponse(categoriaId, "Empilhadeiras",
				"Empilhadseira amarela", true, data, data);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(categoriaService.ativar(categoriaId)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/categorias/{id}/ativar", categoriaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.nome").value("Empilhadeiras")).andExpect(jsonPath("$.ativo").value(true))
				.andExpect(jsonPath("$.descricao").value("Empilhadseira amarela"));

		verify(jwtDecoder).decode("token-administrador");

		verify(categoriaService).ativar(categoriaId);
	}

	@Test
	void deveDesativarCategoriaQuandoAutenticadoComoAdministrador() throws Exception {

		// PREPARAR
		Long categoriaIdParaDesativar = 10L;
		Long adminAutenticadoId = 1L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256")
				.subject(adminAutenticadoId.toString()).claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-09-28T15:00:00Z");

		CategoriaResponse respostaDoService = new CategoriaResponse(categoriaIdParaDesativar, "Empilhadeiras",
				"Empilhadseira amarela", false, data, data);
		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);
		when(categoriaService.desativar(categoriaIdParaDesativar)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/categorias/{id}/desativar", categoriaIdParaDesativar)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.ativo").value(false));

		verify(jwtDecoder).decode("token-administrador");
		verify(categoriaService).desativar(categoriaIdParaDesativar);
	}

	@Test
	void deveRetornarAcessoNegadoQuandoFuncionarioTentarDesativarCategoria() throws Exception {
		// PREPARAR
		Long categoriaId = 10L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/categorias/{id}/desativar", categoriaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isForbidden());

		verify(jwtDecoder).decode("token-funcionario");
		verifyNoInteractions(categoriaService);
	}

	@Test
	void deveRetornarAcessoNegadoQuandoFuncionarioTentarAtivarCategoria() throws Exception {
		// PREPARAR
		Long categoriaId = 10L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/categorias/{id}/ativar", categoriaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isForbidden());

		verify(jwtDecoder).decode("token-funcionario");
		verifyNoInteractions(categoriaService);
	}

	@Test
	void deveRetornarErroQuandoTentarDesativarCategoriaJaInativa() throws Exception {
		// PREPARAR
		Long categoriaId = 10L;
		Long adminAutenticadoId = 1L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256")
				.subject(adminAutenticadoId.toString()).claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);
		when(categoriaService.desativar(categoriaId)).thenThrow(new CategoriaJaInativaException());

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/categorias/{id}/desativar", categoriaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.erro").value("Conflito de situação da categoria"))
				.andExpect(jsonPath("$.mensagens[0]").value("Categoria já está inativa."))
				.andExpect(jsonPath("$.path").value("/api/categorias/10/desativar"))
				.andExpect(jsonPath("$.codigo").value("SITUACAO_CATEGORIA_INVALIDA"));

		verify(jwtDecoder).decode("token-administrador");
		verify(categoriaService).desativar(categoriaId);
	}

	@Test
	void deveRetornarConflitoQuandoTentarAtivarCategoriaJaAtiva() throws Exception {

		// PREPARAR
		Long categoriaId = 10L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);
		when(categoriaService.ativar(categoriaId)).thenThrow(new CategoriaJaAtivaException());

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/categorias/{id}/ativar", categoriaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.erro").value("Conflito de situação da categoria"))
				.andExpect(jsonPath("$.mensagens[0]").value("Categoria já está ativa."))
				.andExpect(jsonPath("$.path").value("/api/categorias/10/ativar"))
				.andExpect(jsonPath("$.codigo").value("SITUACAO_CATEGORIA_INVALIDA"));

		verify(jwtDecoder).decode("token-administrador");
		verify(categoriaService).ativar(categoriaId);
	}

}