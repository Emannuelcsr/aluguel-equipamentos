package br.com.projetosecsr.aluguelequipamentos.cliente.controller;

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
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.TipoCliente;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.CepNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ClienteJaAtivoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ClienteJaInativoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ClienteNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ConsultaCepIndisponivelException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.DocumentoJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.EmailClienteJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.request.AtualizarClienteRequest;
import br.com.projetosecsr.aluguelequipamentos.cliente.request.CadastrarClienteRequest;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.ClienteResponse;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.ClienteResumoResponse;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.EnderecoCepResponse;
import br.com.projetosecsr.aluguelequipamentos.cliente.service.ClienteService;
import br.com.projetosecsr.aluguelequipamentos.cliente.service.ConsultaCepService;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.configuracao.ConfiguracaoDeSeguranca;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.erro.TratadorGlobalDeErros;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorAcessoNegado;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorFalhaAutenticacao;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.EmailJaCadastradoException;

@WebMvcTest(controllers = ClienteController.class)
@Import({ ConfiguracaoDeSeguranca.class, TratadorGlobalDeErros.class, TratadorFalhaAutenticacao.class,
		TratadorAcessoNegado.class })
public class ClienteControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ClienteService clienteService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@MockitoBean
	private ConsultaCepService consultaCepService;

	@Test
	void deveBuscarClienteQuandoAutenticadoComoFuncionario() throws Exception {

		Long clienteId = 10L;

		Instant data = Instant.parse("2026-10-02T15:00:00Z");

		ClienteResponse respostaDoService = new ClienteResponse(clienteId, TipoCliente.PF, "João da Silva", null,
				"52998224725", "joao@email.com", "47999999999", "88330000", "Rua das Flores", "123", "Apto 202",
				"Centro", "Balneário Camboriú", "SC", true, data, data);

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCK
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);
		when(clienteService.buscarPorId(clienteId)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				get("/api/clientes/{id}", clienteId).header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.nomeRazaoSocial").value("João da Silva"))
				.andExpect(jsonPath("$.email").value("joao@email.com")).andExpect(jsonPath("$.tipo").value("PF"))
				.andExpect(jsonPath("$.ativo").value(true));

		verify(jwtDecoder).decode("token-funcionario");

		verify(clienteService).buscarPorId(clienteId);

	}

	@Test
	void deveRetornarNaoEncontradoQuandoClienteNaoExistir() throws Exception {

		// PREPARAR
		Long clienteId = 999L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(clienteService.buscarPorId(clienteId)).thenThrow(new ClienteNaoEncontradoException());

		// EXECUTAR

		ResultActions resultado = mockMvc.perform(
				get("/api/clientes/{id}", clienteId).header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.erro").value("Cliente não encontrado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Cliente não encontrado."))
				.andExpect(jsonPath("$.path").value("/api/clientes/999"))
				.andExpect(jsonPath("$.codigo").value("CLIENTE_NAO_ENCONTRADO"));

		verify(jwtDecoder).decode("token-funcionario");

		verify(clienteService).buscarPorId(clienteId);

	}

	@Test
	void deveRetornarNaoAutorizadoQuandoTokenNaoForInformado() throws Exception {

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(get("/api/clientes"));

		// VERIFICAR
		resultado.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.erro").value("Não autorizado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Token de acesso não informado."))
				.andExpect(jsonPath("$.path").value("/api/clientes"))
				.andExpect(jsonPath("$.codigo").value("TOKEN_AUSENTE"));

		verifyNoInteractions(clienteService);

	}

	@Test
	void deveRetornarNaoAutorizadoQuandoTokenForInvalido() throws Exception {

		// MOCKS
		when(jwtDecoder.decode("token-invalido")).thenThrow(new BadJwtException("Token inválido"));

		// Executar

		ResultActions resultado = mockMvc
				.perform(get("/api/clientes").header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"));

		// VERIFICAR
		resultado.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.erro").value("Não autorizado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Token de acesso inválido ou expirado."))
				.andExpect(jsonPath("$.path").value("/api/clientes"))
				.andExpect(jsonPath("$.codigo").value("TOKEN_INVALIDO"));

		verify(jwtDecoder).decode("token-invalido");
		verifyNoInteractions(clienteService);
	}

	@Test
	void deveRetornarAcessoNegadoQuandoFuncionarioTentarDesativarCliente() throws Exception {
		// PREPARAR
		Long clienteId = 10L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/clientes/{id}/desativar", clienteId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isForbidden());

		verify(jwtDecoder).decode("token-funcionario");
		verifyNoInteractions(clienteService);
	}

	@Test
	void deveRetornarAcessoNegadoQuandoFuncionarioTentarAtivarCliente() throws Exception {
		// PREPARAR
		Long clienteId = 10L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/clientes/{id}/ativar", clienteId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isForbidden());

		verify(jwtDecoder).decode("token-funcionario");
		verifyNoInteractions(clienteService);
	}

	@Test
	void deveAtivarClienteQuandoAutenticadoComoAdministrador() throws Exception {

		// PREPARAR
		Long clienteId = 10L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-09-28T15:00:00Z");

		ClienteResponse respostaDoService = new ClienteResponse(clienteId, TipoCliente.PF, "João da Silva", null,
				"52998224725", "joao@email.com", "47999999999", "88330000", "Rua das Flores", "123", "Apto 202",
				"Centro", "Balneário Camboriú", "SC", true, data, data);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(clienteService.ativar(clienteId)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/clientes/{id}/ativar", clienteId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.nomeRazaoSocial").value("João da Silva"))
				.andExpect(jsonPath("$.ativo").value(true));

		verify(jwtDecoder).decode("token-administrador");

		verify(clienteService).ativar(clienteId);
	}

	@Test
	void deveDesativarClienteQuandoAutenticadoComoAdministrador() throws Exception {

		// PREPARAR
		Long clienteIdParaDesativar = 10L;
		Long adminAutenticadoId = 1L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256")
				.subject(adminAutenticadoId.toString()).claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-09-28T15:00:00Z");

		ClienteResponse respostaDoService = new ClienteResponse(clienteIdParaDesativar, TipoCliente.PF, "João da Silva",
				null, "52998224725", "joao@email.com", "47999999999", "88330000", "Rua das Flores", "123", "Apto 202",
				"Centro", "Balneário Camboriú", "SC", false, data, data);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);
		when(clienteService.desativar(clienteIdParaDesativar)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/clientes/{id}/desativar", clienteIdParaDesativar)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.ativo").value(false));

		verify(jwtDecoder).decode("token-administrador");
		verify(clienteService).desativar(clienteIdParaDesativar);
	}

	@Test
	void deveRetornarConflitoQuandoTentarAtivarUsuarioJaAtivo() throws Exception {

		// PREPARAR
		Long clienteId = 10L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);
		when(clienteService.ativar(clienteId)).thenThrow(new ClienteJaAtivoException());

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/clientes/{id}/ativar", clienteId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.erro").value("Conflito de situação do cliente"))
				.andExpect(jsonPath("$.mensagens[0]").value("Cliente já está ativo."))
				.andExpect(jsonPath("$.path").value("/api/clientes/10/ativar"))
				.andExpect(jsonPath("$.codigo").value("SITUACAO_CLIENTE_INVALIDA"));

		verify(jwtDecoder).decode("token-administrador");
		verify(clienteService).ativar(clienteId);
	}

	@Test
	void deveRetornarErroQuandoTentarDesativarClienteJaInativo() throws Exception {
		// PREPARAR
		Long clienteId = 10L;
		Long adminAutenticadoId = 1L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256")
				.subject(adminAutenticadoId.toString()).claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);
		when(clienteService.desativar(clienteId)).thenThrow(new ClienteJaInativoException());

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/clientes/{id}/desativar", clienteId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.erro").value("Conflito de situação do cliente"))
				.andExpect(jsonPath("$.mensagens[0]").value("Cliente já está inativo."))
				.andExpect(jsonPath("$.path").value("/api/clientes/10/desativar"))
				.andExpect(jsonPath("$.codigo").value("SITUACAO_CLIENTE_INVALIDA"));

		verify(jwtDecoder).decode("token-administrador");
		verify(clienteService).desativar(clienteId);
	}

	@Test
	void deveCadastrarClienteQuandoAutenticadoComoFuncionario() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				  "tipo": "PF",
				  "nomeRazaoSocial": "João da Silva",
				  "nomeFantasia": null,
				  "documento": "52998224725",
				  "email": "joao@email.com",
				  "telefone": "47999999999",
				  "cep": "88330000",
				  "logradouro": "Rua das Flores",
				  "numero": "123",
				  "complemento": "Apto 202",
				  "bairro": "Centro",
				  "cidade": "Balneário Camboriú",
				  "estado": "SC"
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		Instant data = Instant.parse("2026-09-18T15:00:00Z");

		ClienteResponse respostaDoService = new ClienteResponse(5L, TipoCliente.PF, "João da Silva", null,
				"52998224725", "joao@email.com", "47999999999", "88330000", "Rua das Flores", "123", "Apto 202",
				"Centro", "Balneário Camboriú", "SC", true, data, data);

		// MOCKS

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(clienteService.cadastrar(any(CadastrarClienteRequest.class))).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/clientes").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(5L))
				.andExpect(jsonPath("$.nomeRazaoSocial").value("João da Silva"))
				.andExpect(jsonPath("$.email").value("joao@email.com")).andExpect(jsonPath("$.tipo").value("PF"))
				.andExpect(jsonPath("$.ativo").value(true));

		verify(jwtDecoder).decode("token-funcionario");

		verify(clienteService).cadastrar(any(CadastrarClienteRequest.class));

	}

	@Test
	void deveRetornarErrosAoCadastrarComCamposInvalidos() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				  "tipo": "PF",
				  "nomeRazaoSocial": "",
				  "nomeFantasia": null,
				  "documento": "52998224725",
				  "email": "joaemail.com",
				  "telefone": "47999999",
				  "cep": "88330000",
				  "logradouro": "Rua das Flores",
				  "numero": "123",
				  "complemento": "Apto 202",
				  "bairro": "Centro",
				  "cidade": "Balneário Camboriú",
				  "estado": "SC"
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		ResultActions resultado = mockMvc
				.perform(post("/api/clientes").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.erro").value("Dados inválidos"))
				.andExpect(jsonPath("$.path").value("/api/clientes"))
				.andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"));

		verify(jwtDecoder).decode("token-funcionario");

	}

	@Test
	void deveRetornarErroQuandoDocmuentoJaEstiverCadastradoParaOutroCliente() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				  "tipo": "PF",
				  "nomeRazaoSocial": "João da Silva",
				  "nomeFantasia": null,
				  "documento": "52998224725",
				  "email": "joao@email.com",
				  "telefone": "47999999999",
				  "cep": "88330000",
				  "logradouro": "Rua das Flores",
				  "numero": "123",
				  "complemento": "Apto 202",
				  "bairro": "Centro",
				  "cidade": "Balneário Camboriú",
				  "estado": "SC"
				}
				""";

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(clienteService.cadastrar(any(CadastrarClienteRequest.class)))
				.thenThrow(new DocumentoJaCadastradoException());

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/clientes").header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.erro").value("Conflito de dados do cliente"))
				.andExpect(jsonPath("$.mensagens[0]").value("Documento já cadastrado."))
				.andExpect(jsonPath("$.path").value("/api/clientes"))
				.andExpect(jsonPath("$.codigo").value("DADOS_CLIENTE_JA_CADASTRADOS"));

		verify(jwtDecoder).decode("token-administrador");

		verify(clienteService).cadastrar(any(CadastrarClienteRequest.class));
	}

	@Test
	void deveRetornarErroQuandoEmailJaEstiverCadastradoParaOutroUsuario() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				  "tipo": "PF",
				  "nomeRazaoSocial": "João da Silva",
				  "nomeFantasia": null,
				  "documento": "52998224725",
				  "email": "joao@email.com",
				  "telefone": "47999999999",
				  "cep": "88330000",
				  "logradouro": "Rua das Flores",
				  "numero": "123",
				  "complemento": "Apto 202",
				  "bairro": "Centro",
				  "cidade": "Balneário Camboriú",
				  "estado": "SC"
				}
				""";

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(clienteService.cadastrar(any(CadastrarClienteRequest.class))).thenThrow(new EmailJaCadastradoException());

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/clientes").header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));
		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.erro").value("Conflito de dados"))
				.andExpect(jsonPath("$.mensagens[0]").value("O e-mail informado já está cadastrado."))
				.andExpect(jsonPath("$.path").value("/api/clientes"))
				.andExpect(jsonPath("$.codigo").value("EMAIL_JA_CADASTRADO"));

		verify(jwtDecoder).decode("token-administrador");

		verify(clienteService).cadastrar(any(CadastrarClienteRequest.class));
	}

	@Test
	void deveAtualizarClienteQuandoAutenticadoComoFuncionarioEDadosForemValidos() throws Exception {
		// PREPARAR
		Long clienteId = 10L;

		// PREPARAR
		String corpoRequisicao = """
				{

				  "nomeRazaoSocial": "João da Silva",
				  "nomeFantasia": null,
				  "email": "joao@email.com",
				  "telefone": "47999999999",
				  "cep": "88330000",
				  "logradouro": "Rua das Flores",
				  "numero": "123",
				  "complemento": "Apto 202",
				  "bairro": "Centro",
				  "cidade": "Balneário Camboriú",
				  "estado": "SC"
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		Instant data = Instant.parse("2026-09-18T15:00:00Z");

		Instant dataAtualizacao = Instant.parse("2026-09-28T15:00:00Z");

		ClienteResponse respostaDoService = new ClienteResponse(10L, TipoCliente.PF, "João da Silva atualizado", null,
				"52998224725", "joaoAtualizado@email.com", "47999999999", "88330000", "Rua das Flores", "123",
				"Apto 202", "Centro", "Balneário Camboriú", "SC", true, data, dataAtualizacao);

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);
		when(clienteService.atualizar(any(AtualizarClienteRequest.class), eq(clienteId))).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				put("/api/clientes/{id}", clienteId).header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.nomeRazaoSocial").value("João da Silva atualizado"))
				.andExpect(jsonPath("$.email").value("joaoAtualizado@email.com"))
				.andExpect(jsonPath("$.tipo").value("PF")).andExpect(jsonPath("$.ativo").value(true));

		verify(jwtDecoder).decode("token-funcionario");

		verify(clienteService).atualizar(any(AtualizarClienteRequest.class), eq(clienteId));
	}

	@Test
	void deveRetornarNaoEncontradoQuandoTentarAtualizarClienteInexistente() throws Exception {

		// PREPARAR
		Long clienteIdInexistente = 999L;

		String corpoRequisicao = """
				{
				  "nomeRazaoSocial": "Maria Atualizada",
				  "nomeFantasia": null,
				  "email": "maria.nova@empresa.com",
				  "telefone": "47999999999",
				  "cep": "88330000",
				  "logradouro": "Rua Nova",
				  "numero": "456",
				  "complemento": "Casa 2",
				  "bairro": "Centro",
				  "cidade": "Balneário Camboriú",
				  "estado": "SC"
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(clienteService.atualizar(any(AtualizarClienteRequest.class), eq(clienteIdInexistente)))
				.thenThrow(new ClienteNaoEncontradoException());

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(put("/api/clientes/{id}", clienteIdInexistente)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario").contentType(MediaType.APPLICATION_JSON)
				.content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.erro").value("Cliente não encontrado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Cliente não encontrado."))
				.andExpect(jsonPath("$.path").value("/api/clientes/999"))
				.andExpect(jsonPath("$.codigo").value("CLIENTE_NAO_ENCONTRADO"));

		verify(jwtDecoder).decode("token-funcionario");

		verify(clienteService).atualizar(any(AtualizarClienteRequest.class), eq(clienteIdInexistente));
	}

	@Test
	void deveRetornarErroQuandoEmailJaEstiverCadastradoParaOutroCliente() throws Exception {

		// PREPARAR
		Long clienteId = 10L;

		String corpoRequisicao = """
				{
				  "nomeRazaoSocial": "Maria Atualizada",
				  "nomeFantasia": null,
				  "email": "maria.nova@empresa.com",
				  "telefone": "47999999999",
				  "cep": "88330000",
				  "logradouro": "Rua Nova",
				  "numero": "456",
				  "complemento": "Casa 2",
				  "bairro": "Centro",
				  "cidade": "Balneário Camboriú",
				  "estado": "SC"
				}
				""";

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(clienteService.atualizar(any(AtualizarClienteRequest.class), eq(clienteId)))
				.thenThrow(new EmailClienteJaCadastradoException());

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				put("/api/clientes/{id}", clienteId).header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.erro").value("Conflito de dados do cliente"))
				.andExpect(jsonPath("$.mensagens[0]").value("E-mail já cadastrado para outro cliente."))
				.andExpect(jsonPath("$.codigo").value("DADOS_CLIENTE_JA_CADASTRADOS"));

		verify(jwtDecoder).decode("token-administrador");

		verify(clienteService).atualizar(any(AtualizarClienteRequest.class), eq(clienteId));
	}

	@Test
	void deveRetornarErrosAoAtualizarComCamposInvalidos() throws Exception {

		// PREPARAR
		Long clienteId = 10L;

		String corpoRequisicao = """
				{
				  "nomeRazaoSocial": "",
				  "nomeFantasia": null,
				  "email": "joaemail.com",
				  "telefone": "47999999",
				  "cep": "88330000",
				  "logradouro": "Rua das Flores",
				  "numero": "123",
				  "complemento": "Apto 202",
				  "bairro": "Centro",
				  "cidade": "Balneário Camboriú",
				  "estado": "SC"
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		ResultActions resultado = mockMvc.perform(
				put("/api/clientes/{id}", clienteId).header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.erro").value("Dados inválidos"))
				.andExpect(jsonPath("$.path").value("/api/clientes/10"))
				.andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"));

		verify(jwtDecoder).decode("token-funcionario");
		verifyNoInteractions(clienteService);

	}

	@Test
	void deveListarClientesComPaginacaoQuandoAutenticadoComoFuncionario() throws Exception {

		// PREPARAR
		Long clienteId1 = 1L;
		Long clienteId2 = 2L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		ClienteResumoResponse cliente1 = new ClienteResumoResponse(clienteId1, TipoCliente.PF, "João da Silva", null,
				"52998224725", "joao@email.com", "47999999999", true);

		ClienteResumoResponse cliente2 = new ClienteResumoResponse(clienteId2, TipoCliente.PF, "Maria da Silva", null,
				"23869297018", "maria@email.com", "47988887777", true);

		PaginaResponse<ClienteResumoResponse> respostaDoService = new PaginaResponse<>(List.of(cliente1, cliente2), 1,
				2, 2, 5L, 3, true, false);

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(clienteService.listarPaginado(any(Pageable.class))).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(get("/api/clientes").param("page", "1").param("size", "2")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.conteudo.length()").value(2))

				.andExpect(jsonPath("$.conteudo[0].id").value(1))
				.andExpect(jsonPath("$.conteudo[0].nomeRazaoSocial").value("João da Silva"))
				.andExpect(jsonPath("$.conteudo[0].tipo").value("PF"))
				.andExpect(jsonPath("$.conteudo[0].email").value("joao@email.com"))
				.andExpect(jsonPath("$.conteudo[0].ativo").value(true))

				.andExpect(jsonPath("$.conteudo[1].id").value(2))
				.andExpect(jsonPath("$.conteudo[1].nomeRazaoSocial").value("Maria da Silva"))
				.andExpect(jsonPath("$.conteudo[1].tipo").value("PF"))
				.andExpect(jsonPath("$.conteudo[1].email").value("maria@email.com"))
				.andExpect(jsonPath("$.conteudo[1].ativo").value(true))

				.andExpect(jsonPath("$.paginaAtual").value(1)).andExpect(jsonPath("$.tamanho").value(2))
				.andExpect(jsonPath("$.quantidadeElementos").value(2)).andExpect(jsonPath("$.totalElementos").value(5))
				.andExpect(jsonPath("$.totalPaginas").value(3)).andExpect(jsonPath("$.primeiraPagina").value(true))
				.andExpect(jsonPath("$.ultimaPagina").value(false));

		verify(jwtDecoder).decode("token-funcionario");

		ArgumentCaptor<Pageable> paginacaoCaptor = ArgumentCaptor.forClass(Pageable.class);

		verify(clienteService).listarPaginado(paginacaoCaptor.capture());

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

		when(clienteService.listarPaginado(any(Pageable.class)))
				.thenThrow(new PaginaInvalidaException("A página informada não existe"));

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(get("/api/clientes").param("page", "4").param("size", "2")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.erro").value("Parâmetros de paginação inválidos"))
				.andExpect(jsonPath("$.mensagens[0]").value("A página informada não existe"))
				.andExpect(jsonPath("$.path").value("/api/clientes"))
				.andExpect(jsonPath("$.codigo").value("PAGINA_NAO_ENCONTRADA"));

		verify(jwtDecoder).decode("token-funcionario");

		verify(clienteService).listarPaginado(any(Pageable.class));
	}

	@Test
	void deveConsultarCepQuandoAutenticadoComoFuncionario() throws Exception {

		// PREPARAR
		String cep = "88330000";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		EnderecoCepResponse respostaDoService = new EnderecoCepResponse("88330-000", "Rua das Flores", "", "Centro",
				"Balneário Camboriú", "SC");

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(consultaCepService.consultar(cep)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				get("/api/clientes/cep/{cep}", cep).header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.cep").value("88330-000"))
				.andExpect(jsonPath("$.logradouro").value("Rua das Flores"))
				.andExpect(jsonPath("$.complemento").value("")).andExpect(jsonPath("$.bairro").value("Centro"))
				.andExpect(jsonPath("$.cidade").value("Balneário Camboriú"))
				.andExpect(jsonPath("$.estado").value("SC"));

		verify(jwtDecoder).decode("token-funcionario");

		verify(consultaCepService).consultar(cep);
	}

	@Test
	void deveRetornarNaoEncontradoQuandoCepNaoExistir() throws Exception {

		// PREPARAR
		String cep = "00000000";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(consultaCepService.consultar(cep)).thenThrow(new CepNaoEncontradoException());

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				get("/api/clientes/cep/{cep}", cep).header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.erro").value("CEP não encontrado"))
				.andExpect(jsonPath("$.mensagens[0]").value("CEP não encontrado."))
				.andExpect(jsonPath("$.path").value("/api/clientes/cep/00000000"))
				.andExpect(jsonPath("$.codigo").value("CEP_NAO_ENCONTRADO"));

		verify(jwtDecoder).decode("token-funcionario");
		verify(consultaCepService).consultar(cep);
	}

	@Test
	void deveRetornarServicoIndisponivelQuandoConsultaCepFalhar() throws Exception {

		// PREPARAR
		String cep = "88330000";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(consultaCepService.consultar(cep)).thenThrow(new ConsultaCepIndisponivelException());

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				get("/api/clientes/cep/{cep}", cep).header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.status").value(503))
				.andExpect(jsonPath("$.erro").value("Serviço de consulta de CEP indisponível"))
				.andExpect(jsonPath("$.mensagens[0]").value("Serviço de consulta de CEP indisponível."))
				.andExpect(jsonPath("$.path").value("/api/clientes/cep/88330000"))
				.andExpect(jsonPath("$.codigo").value("CONSULTA_CEP_INDISPONIVEL"));

		verify(jwtDecoder).decode("token-funcionario");
		verify(consultaCepService).consultar(cep);
	}
}
