package br.com.projetosecsr.aluguelequipamentos.usuario.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.configuracao.ConfiguracaoDeSeguranca;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.erro.TratadorGlobalDeErros;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorAcessoNegado;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorFalhaAutenticacao;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.EmailJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.UsuarioNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.request.AtualizarUsuarioRequest;
import br.com.projetosecsr.aluguelequipamentos.usuario.request.CadastrarUsuarioRequest;
import br.com.projetosecsr.aluguelequipamentos.usuario.response.UsuarioResponse;
import br.com.projetosecsr.aluguelequipamentos.usuario.service.UsuarioService;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = UsuarioController.class)
@Import({ ConfiguracaoDeSeguranca.class, TratadorGlobalDeErros.class, TratadorFalhaAutenticacao.class,
		TratadorAcessoNegado.class })
public class UsuarioControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UsuarioService usuarioService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void deveRetornarNaoAutorizadoQuandoTokenNaoForInformado() throws Exception {
		// PREPARAR
		String corpoRequisicao = """
				{
				  "nome": "Funcionário Teste",
				  "email": "funcionario@empresa.com",
				  "senha": "senha-segura-com-15-caracteres",
				  "perfil": "FUNCIONARIO"
				}
				""";

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.erro").value("Não autorizado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Token de acesso não informado."))
				.andExpect(jsonPath("$.path").value("/api/usuarios"))
				.andExpect(jsonPath("$.codigo").value("TOKEN_AUSENTE"));

		verifyNoInteractions(usuarioService);

	}

	@Test
	void deveRetornarNaoAutorizadoQuandoTokenForInvalido() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				  "nome": "Funcionário Teste",
				  "email": "funcionario@empresa.com",
				  "senha": "senha-segura-com-15-caracteres",
				  "perfil": "FUNCIONARIO"
				}
				""";

		// MOCKS
		when(jwtDecoder.decode("token-invalido")).thenThrow(new BadJwtException("Token inválido"));

		// Executar

		ResultActions resultado = mockMvc
				.perform(post("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.erro").value("Não autorizado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Token de acesso inválido ou expirado."))
				.andExpect(jsonPath("$.path").value("/api/usuarios"))
				.andExpect(jsonPath("$.codigo").value("TOKEN_INVALIDO"));

		verify(jwtDecoder).decode("token-invalido");
		verifyNoInteractions(usuarioService);
	}

	@Test
	void deveRetornarAcessoNegadoQuandoUsuarioForFuncionario() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				  "nome": "Funcionário Teste",
				  "email": "funcionario@empresa.com",
				  "senha": "senha-segura-com-15-caracteres",
				  "perfil": "FUNCIONARIO"
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR

		ResultActions resultado = mockMvc
				.perform(post("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.erro").value("Acesso negado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Você não possui permissão para acessar este recurso."))
				.andExpect(jsonPath("$.path").value("/api/usuarios"))
				.andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));

		verify(jwtDecoder).decode("token-funcionario");
		verifyNoInteractions(usuarioService);

	}

	@Test
	void deveCadastrarUsuarioQuandoAutenticadoComoAdministrador() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				  "nome": "Funcionário Teste",
				  "email": "funcionario@empresa.com",
				  "senha": "senha-segura-com-15-caracteres",
				  "perfil": "FUNCIONARIO"
				}
				""";

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-09-18T15:00:00Z");

		UsuarioResponse respostaDoService = new UsuarioResponse(10L, "Funcionário Teste", "funcionario@empresa.com",
				PerfilUsuario.FUNCIONARIO, true, data, data);

		// MOCKS

		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(usuarioService.cadastrar(any(CadastrarUsuarioRequest.class))).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/usuarios").header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.nome").value("Funcionário Teste"))
				.andExpect(jsonPath("$.email").value("funcionario@empresa.com"))
				.andExpect(jsonPath("$.perfil").value("FUNCIONARIO")).andExpect(jsonPath("$.ativo").value(true))
				.andExpect(jsonPath("$.senha").doesNotExist());

		verify(jwtDecoder).decode("token-administrador");

		verify(usuarioService).cadastrar(any(CadastrarUsuarioRequest.class));

	}

	@Test
	void deveBuscarUsuarioPorIdQuandoAutenticadoComoAdministrador() throws Exception {

		// PREPARAR
		Long usuarioId = 10L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-09-24T15:00:00Z");

		UsuarioResponse respostaDoService = new UsuarioResponse(usuarioId, "Maria Souza", "maria@empresa.com",
				PerfilUsuario.FUNCIONARIO, true, data, data);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(usuarioService.buscarPorId(usuarioId)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				get("/api/usuarios/{id}", usuarioId).header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.nome").value("Maria Souza"))
				.andExpect(jsonPath("$.email").value("maria@empresa.com"))
				.andExpect(jsonPath("$.perfil").value("FUNCIONARIO")).andExpect(jsonPath("$.ativo").value(true))
				.andExpect(jsonPath("$.senha").doesNotExist());

		verify(jwtDecoder).decode("token-administrador");

		verify(usuarioService).buscarPorId(usuarioId);
	}

	@Test
	void deveRetornarNaoEncontradoQuandoUsuarioNaoExistir() throws Exception {

		// PREPARAR
		Long usuarioId = 999L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		// MOCKS

		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(usuarioService.buscarPorId(usuarioId)).thenThrow(new UsuarioNaoEncontradoException());

		// EXECUTAR

		ResultActions resultado = mockMvc.perform(
				get("/api/usuarios/{id}", usuarioId).header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Usuário não encontrado."))
				.andExpect(jsonPath("$.path").value("/api/usuarios/999"))
				.andExpect(jsonPath("$.codigo").value("USUARIO_NAO_ENCONTRADO"));

		verify(jwtDecoder).decode("token-administrador");

		verify(usuarioService).buscarPorId(usuarioId);

		verify(usuarioService).buscarPorId(usuarioId);

	}

	@Test
	void deveRetornarAcessoNegadoQuandoFuncionarioBuscarUsuarioPorId() throws Exception {

		// PREPARAR
		Long usuarioId = 10L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				get("/api/usuarios/{id}", usuarioId).header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.erro").value("Acesso negado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Você não possui permissão para acessar este recurso."))
				.andExpect(jsonPath("$.path").value("/api/usuarios/10"))
				.andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));

		verify(jwtDecoder).decode("token-funcionario");

		verifyNoInteractions(usuarioService);
	}

	@Test
	void deveListarUsuariosComPaginacaoQuandoAutenticadoComoAdministrador() throws Exception {

		// PREPARAR
		Long usuarioId = 1L;
		Long usuarioId2 = 2L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-09-24T15:00:00Z");

		UsuarioResponse usuarioAdministradorResponse = new UsuarioResponse(usuarioId, "Administrador Teste",
				"administrador@empresa.com", PerfilUsuario.ADMINISTRADOR, true, data, data);

		UsuarioResponse usuarioFuncionarioResponse = new UsuarioResponse(usuarioId2, "Funcionario Teste",
				"funcionario@empresa.com", PerfilUsuario.FUNCIONARIO, true, data, data);

		PaginaResponse<UsuarioResponse> respostaDoService = new PaginaResponse<>(
				List.of(usuarioAdministradorResponse, usuarioFuncionarioResponse), 1, 2, 2, 5L, 3, true, false);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(usuarioService.listarPaginado(any(Pageable.class))).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(get("/api/usuarios").param("page", "1").param("size", "2")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.conteudo.length()").value(2))
				.andExpect(jsonPath("$.conteudo[0].id").value(1))
				.andExpect(jsonPath("$.conteudo[0].nome").value("Administrador Teste"))
				.andExpect(jsonPath("$.conteudo[0].perfil").value("ADMINISTRADOR"))
				.andExpect(jsonPath("$.conteudo[0].senha").doesNotExist())
				.andExpect(jsonPath("$.conteudo[1].id").value(2))
				.andExpect(jsonPath("$.conteudo[1].nome").value("Funcionario Teste"))
				.andExpect(jsonPath("$.conteudo[1].perfil").value("FUNCIONARIO"))
				.andExpect(jsonPath("$.conteudo[1].senha").doesNotExist()).andExpect(jsonPath("$.paginaAtual").value(1))
				.andExpect(jsonPath("$.tamanho").value(2)).andExpect(jsonPath("$.quantidadeElementos").value(2))
				.andExpect(jsonPath("$.totalElementos").value(5)).andExpect(jsonPath("$.totalPaginas").value(3))
				.andExpect(jsonPath("$.primeiraPagina").value(true)).andExpect(jsonPath("$.ultimaPagina").value(false));

		verify(jwtDecoder).decode("token-administrador");

		ArgumentCaptor<Pageable> paginacaoCaptor = ArgumentCaptor.forClass(Pageable.class);

		verify(usuarioService).listarPaginado(paginacaoCaptor.capture());

		Pageable paginacaoRecebida = paginacaoCaptor.getValue();

		assertEquals(0, paginacaoRecebida.getPageNumber());
		assertEquals(2, paginacaoRecebida.getPageSize());

	}

	@Test
	void deveAtualizarUsuarioQuandoAutenticadoComoAdministradorEDadosForemValidos() throws Exception {
		// PREPARAR
		Long usuarioId = 10L;

		String corpoRequisicao = """
				{
				  "nome": "Maria Atualizada",
				  "email": "maria.nova@empresa.com",
				  "perfil": "FUNCIONARIO"
				}
				""";

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant dataAtualizacao = Instant.parse("2026-09-28T15:00:00Z");

		UsuarioResponse respostaDoService = new UsuarioResponse(usuarioId, "Maria Atualizada", "maria.nova@empresa.com",
				PerfilUsuario.FUNCIONARIO, true, dataAtualizacao, dataAtualizacao);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);
		when(usuarioService.atualizar(eq(usuarioId), any(AtualizarUsuarioRequest.class))).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				put("/api/usuarios/{id}", usuarioId).header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.nome").value("Maria Atualizada"))
				.andExpect(jsonPath("$.email").value("maria.nova@empresa.com"))
				.andExpect(jsonPath("$.perfil").value("FUNCIONARIO")).andExpect(jsonPath("$.ativo").value(true))
				.andExpect(jsonPath("$.senha").doesNotExist());

		verify(jwtDecoder).decode("token-administrador");

		verify(usuarioService).atualizar(eq(usuarioId), any(AtualizarUsuarioRequest.class));
	}

	@Test
	void deveRetornarNaoEncontradoQuandoTentarAtualizarUsuarioInexistente() throws Exception {

		// PREPARAR
		Long usuarioIdInexistente = 999L;

		String corpoRequisicao = """
				{
				  "nome": "Maria Atualizada",
				  "email": "maria.nova@empresa.com",
				  "perfil": "FUNCIONARIO"
				}
				""";

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(usuarioService.atualizar(eq(usuarioIdInexistente), any(AtualizarUsuarioRequest.class)))
				.thenThrow(new UsuarioNaoEncontradoException());

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(put("/api/usuarios/{id}", usuarioIdInexistente)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador").contentType(MediaType.APPLICATION_JSON)
				.content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Usuário não encontrado."))
				.andExpect(jsonPath("$.path").value("/api/usuarios/999"))
				.andExpect(jsonPath("$.codigo").value("USUARIO_NAO_ENCONTRADO"));

		verify(jwtDecoder).decode("token-administrador");

		verify(usuarioService).atualizar(eq(usuarioIdInexistente), any(AtualizarUsuarioRequest.class));
	}

	@Test
	void deveRetornarErroQuandoEmailJaEstiverCadastradoParaOutroUsuario() throws Exception {

		// PREPARAR
		Long usuarioId = 10L;

		String corpoRequisicao = """
				{
				  "nome": "Maria Souza",
				  "email": "email.existente@empresa.com",
				  "perfil": "FUNCIONARIO"
				}
				""";

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(usuarioService.atualizar(eq(usuarioId), any(AtualizarUsuarioRequest.class)))
				.thenThrow(new EmailJaCadastradoException());

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				put("/api/usuarios/{id}", usuarioId).header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.erro").value("Conflito de dados"))
				.andExpect(jsonPath("$.mensagens[0]").value("O e-mail informado já está cadastrado."))
				.andExpect(jsonPath("$.path").value("/api/usuarios/10"))
				.andExpect(jsonPath("$.codigo").value("EMAIL_JA_CADASTRADO"));

		verify(jwtDecoder).decode("token-administrador");

		verify(usuarioService).atualizar(eq(usuarioId), any(AtualizarUsuarioRequest.class));
	}

	@Test
	void deveRetornarAcessoNegadoQuandoFuncionarioTentarAtualizarUsuario() throws Exception {

		// PREPARAR
		Long usuarioId = 10L;

		String corpoRequisicao = """
				{
				  "nome": "Maria Atualizada",
				  "email": "maria.nova@empresa.com",
				  "perfil": "FUNCIONARIO"
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				put("/api/usuarios/{id}", usuarioId).header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.erro").value("Acesso negado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Você não possui permissão para acessar este recurso."))
				.andExpect(jsonPath("$.path").value("/api/usuarios/10"))
				.andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));

		verify(jwtDecoder).decode("token-funcionario");

		verifyNoInteractions(usuarioService);
	}

	@Test
	void deveRetornarBadRequestQuandoDadosDaRequisicaoForemInvalidos() throws Exception {

		// PREPARAR
		Long usuarioId = 10L;

		// JSON com nome em branco e e-mail sem formato válido
		String corpoRequisicaoInvalido = """
				{
				  "nome": "   ",
				  "email": "email-invalido-sem-arroba",
				  "perfil": null
				}
				""";

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				put("/api/usuarios/{id}", usuarioId).header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicaoInvalido));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.erro").value("Dados inválidos")).andExpect(jsonPath("$.mensagens").isArray())
				.andExpect(jsonPath("$.path").value("/api/usuarios/10"))
				.andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"));

		verify(jwtDecoder).decode("token-administrador");

		verifyNoInteractions(usuarioService);
	}

}
