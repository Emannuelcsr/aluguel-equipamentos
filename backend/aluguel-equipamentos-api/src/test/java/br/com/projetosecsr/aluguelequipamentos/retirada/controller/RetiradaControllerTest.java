package br.com.projetosecsr.aluguelequipamentos.retirada.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import br.com.projetosecsr.aluguelequipamentos.autenticacao.seguranca.ConversorAutenticacaoJwt;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.configuracao.ConfiguracaoDeSeguranca;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.erro.TratadorGlobalDeErros;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorAcessoNegado;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorFalhaAutenticacao;
import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.CondicaoFisicaRetirada;
import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.MetodoVerificacaoAutorizacao;
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
import br.com.projetosecsr.aluguelequipamentos.retirada.request.CadastrarRetiradaRequest;
import br.com.projetosecsr.aluguelequipamentos.retirada.response.ItemRetiradaResponse;
import br.com.projetosecsr.aluguelequipamentos.retirada.response.RetiradaResponse;
import br.com.projetosecsr.aluguelequipamentos.retirada.service.RetiradaService;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;

@WebMvcTest(controllers = RetiradaController.class)
@Import({ ConfiguracaoDeSeguranca.class, TratadorGlobalDeErros.class, TratadorFalhaAutenticacao.class,
		TratadorAcessoNegado.class })
public class RetiradaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RetiradaService retiradaService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@MockitoBean
	private ConversorAutenticacaoJwt conversorAutenticacaoJwt;

	@BeforeEach
	void prepararConversor() {

		JwtGrantedAuthoritiesConverter conversorDeAutoridades = new JwtGrantedAuthoritiesConverter();

		conversorDeAutoridades.setAuthoritiesClaimName("perfil");
		conversorDeAutoridades.setAuthorityPrefix("ROLE_");

		JwtAuthenticationConverter conversorPadrao = new JwtAuthenticationConverter();

		conversorPadrao.setJwtGrantedAuthoritiesConverter(conversorDeAutoridades);

		when(conversorAutenticacaoJwt.convert(any(Jwt.class)))
				.thenAnswer(invocacao -> conversorPadrao.convert(invocacao.getArgument(0)));
	}

	@Test
	void deveCadastrarRetiradaComSucesso() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		OffsetDateTime dataHora = OffsetDateTime.parse("2026-10-27T14:00:00-03:00");

		ItemRetiradaResponse itemResposta = new ItemRetiradaResponse(40L, 10L, 20L, "UNI-000020", "Marreta",
				CondicaoFisicaRetirada.SEM_DANOS_APARENTES, null);

		RetiradaResponse respostaDoService = new RetiradaResponse(30L, 2L, dataHora, "João da Silva", "52998224725",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, 5L, "Funcionário Carlos", List.of(itemResposta));

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.cadastrar(any(CadastrarRetiradaRequest.class))).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(30))
				.andExpect(jsonPath("$.reservaId").value(2))
				.andExpect(jsonPath("$.nomeRecebedor").value("João da Silva"))
				.andExpect(jsonPath("$.documentoRecebedor").value("52998224725"))
				.andExpect(jsonPath("$.metodoVerificacao").value("PRESENCIAL"))
				.andExpect(jsonPath("$.usuarioResponsavelId").value(5))
				.andExpect(jsonPath("$.usuarioResponsavelNome").value("Funcionário Carlos"))
				.andExpect(jsonPath("$.itens.length()").value(1))
				.andExpect(jsonPath("$.itens[0].itemReservaId").value(10))
				.andExpect(jsonPath("$.itens[0].condicaoFisica").value("SEM_DANOS_APARENTES"));

		verify(retiradaService).cadastrar(any(CadastrarRetiradaRequest.class));
	}

	@Test
	void deveNegarCadastroRetiradaSemAutenticacao() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		// MOCKS
		// Não precisamos configurar mocks neste cenário.

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isUnauthorized());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveNegarCadastroRetiradaSemPermissao() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtSemPermissao = Jwt.withTokenValue("token-sem-permissao").header("alg", "HS256").subject("5")
				.claim("perfil", "SEM_PERMISSAO").build();

		// MOCKS
		when(jwtDecoder.decode("token-sem-permissao")).thenReturn(jwtSemPermissao);

		when(conversorAutenticacaoJwt.convert(jwtSemPermissao)).thenReturn(
				new JwtAuthenticationToken(jwtSemPermissao, List.of(new SimpleGrantedAuthority("ROLE_SEM_PERMISSAO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-sem-permissao")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isForbidden());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoReservaIdNaoForInformado() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": null,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoDocumentoRecebedorNaoForInformado() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": null,
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoDocumentoRecebedorEstiverVazio() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoDocumentoRecebedorTiverMenosDe11Digitos() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "5299822472",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoDocumentoRecebedorTiverMaisDe11Digitos() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "529982247251",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoDocumentoRecebedorContiverLetras() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "5299822472A",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoNomeRecebedorNaoForInformado() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": null,
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoNomeRecebedorEstiverVazio() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoMetodoVerificacaoNaoForInformado() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": null,
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoNomeRecebedorUltrapassar150Caracteres() throws Exception {

		// PREPARAR
		String nomeMuitoLongo = "A".repeat(151);

		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "%s",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""".formatted(nomeMuitoLongo);

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoDescricaoMetodoUltrapassar500Caracteres() throws Exception {

		// PREPARAR
		String descricaoMuitoLonga = "A".repeat(501);

		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "OUTRO",
				    "descricaoOutroMetodo": "%s",
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""".formatted(descricaoMuitoLonga);

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoListaDeItensEstiverVazia() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoItensNaoForemInformados() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": null,
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoItemReservaIdNaoForInformado() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": null,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoCondicaoFisicaNaoForInformada() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": null,
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoObservacoesUltrapassarem500Caracteres() throws Exception {

		// PREPARAR
		String observacoesMuitoLongas = "A".repeat(501);

		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "COM_OBSERVACOES",
				            "observacoes": "%s"
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""".formatted(observacoesMuitoLongas);

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoAutorizacaoConfirmadaNaoForInformada() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": null,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoAutorizacaoNaoEstiverConfirmada() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": false,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoRecebimentoConfirmadoNaoForInformado() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": null
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoRecebimentoNaoEstiverConfirmado() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": false
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoListaContiverItemNulo() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [null],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoMetodoVerificacaoForInvalido() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "EMAIL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarBadRequestQuandoCondicaoFisicaForInvalida() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "DANIFICADO",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveRetornarConflictQuandoReservaNaoEstiverAprovada() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.cadastrar(any(CadastrarRetiradaRequest.class)))
				.thenThrow(new ReservaNaoAprovadaException());

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.codigo").value("RESERVA_NAO_APROVADA"))
				.andExpect(jsonPath("$.erro").value("Operação de retirada inválida"))
				.andExpect(jsonPath("$.mensagens[0]").isNotEmpty());

		verify(retiradaService).cadastrar(any(CadastrarRetiradaRequest.class));
	}

	@Test
	void deveRetornarConflictQuandoRetiradaEstiverForaDoPeriodo() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.cadastrar(any(CadastrarRetiradaRequest.class)))
				.thenThrow(new RetiradaForaDoPeriodoException());

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.codigo").value("RETIRADA_FORA_DO_PERIODO"))
				.andExpect(jsonPath("$.erro").value("Retirada fora do período permitido"))
				.andExpect(jsonPath("$.mensagens[0]").isNotEmpty());

		verify(retiradaService).cadastrar(any(CadastrarRetiradaRequest.class));
	}

	@Test
	void deveRetornarConflictQuandoClienteEstiverInativoParaRetirada() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.cadastrar(any(CadastrarRetiradaRequest.class)))
				.thenThrow(new ClienteInativoParaRetiradaException());

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.codigo").value("CLIENTE_INATIVO_PARA_RETIRADA"))
				.andExpect(jsonPath("$.erro").value("Cliente inativo para retirada"))
				.andExpect(jsonPath("$.mensagens[0]").isNotEmpty());

		verify(retiradaService).cadastrar(any(CadastrarRetiradaRequest.class));
	}

	@Test
	void deveRetornarConflictQuandoRetiradaJaTiverSidoRealizada() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.cadastrar(any(CadastrarRetiradaRequest.class)))
				.thenThrow(new RetiradaJaRealizadaException());

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.codigo").value("RETIRADA_JA_REALIZADA"))
				.andExpect(jsonPath("$.erro").value("Retirada já realizada"))
				.andExpect(jsonPath("$.mensagens[0]").isNotEmpty());

		verify(retiradaService).cadastrar(any(CadastrarRetiradaRequest.class));
	}

	@Test
	void deveRetornarConflictQuandoUnidadeEstiverInativaParaRetirada() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.cadastrar(any(CadastrarRetiradaRequest.class)))
				.thenThrow(new UnidadeInativaParaRetiradaException());

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.codigo").value("UNIDADE_INATIVA_PARA_RETIRADA"))
				.andExpect(jsonPath("$.erro").value("Unidade inativa para retirada"))
				.andExpect(jsonPath("$.mensagens[0]").isNotEmpty());

		verify(retiradaService).cadastrar(any(CadastrarRetiradaRequest.class));
	}

	@Test
	void deveRetornarConflictQuandoUnidadeEstiverIndisponivelParaRetirada() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.cadastrar(any(CadastrarRetiradaRequest.class)))
				.thenThrow(new UnidadeIndisponivelParaRetiradaException());

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.codigo").value("UNIDADE_INDISPONIVEL_PARA_RETIRADA"))
				.andExpect(jsonPath("$.erro").value("Unidade indisponível para retirada"))
				.andExpect(jsonPath("$.mensagens[0]").isNotEmpty());

		verify(retiradaService).cadastrar(any(CadastrarRetiradaRequest.class));
	}

	@Test
	void deveRetornarBadRequestQuandoItemRetiradaEstiverDuplicado() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        },
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.cadastrar(any(CadastrarRetiradaRequest.class)))
				.thenThrow(new ItemRetiradaDuplicadoException());

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.codigo").value("ITEM_RETIRADA_DUPLICADO"))
				.andExpect(jsonPath("$.erro").value("Item duplicado na retirada"))
				.andExpect(jsonPath("$.mensagens[0]").isNotEmpty());

		verify(retiradaService).cadastrar(any(CadastrarRetiradaRequest.class));
	}

	@Test
	void deveRetornarBadRequestQuandoItensRetiradaForemDivergentes() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.cadastrar(any(CadastrarRetiradaRequest.class)))
				.thenThrow(new ItensRetiradaDivergentesException());

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.codigo").value("ITENS_RETIRADA_DIVERGENTES"))
				.andExpect(jsonPath("$.erro").value("Itens da retirada divergentes"))
				.andExpect(jsonPath("$.mensagens[0]").isNotEmpty());

		verify(retiradaService).cadastrar(any(CadastrarRetiradaRequest.class));
	}

	@Test
	void deveRetornarBadRequestQuandoObservacaoObrigatoriaNaoForInformada() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "COM_OBSERVACOES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.cadastrar(any(CadastrarRetiradaRequest.class)))
				.thenThrow(new ObservacaoObrigatoriaException());

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.codigo").value("OBSERVACAO_OBRIGATORIA"))
				.andExpect(jsonPath("$.erro").value("Observação obrigatória"))
				.andExpect(jsonPath("$.mensagens[0]").isNotEmpty());

		verify(retiradaService).cadastrar(any(CadastrarRetiradaRequest.class));
	}

	@Test
	void deveRetornarBadRequestQuandoDescricaoMetodoVerificacaoNaoForInformada() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "OUTRO",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.cadastrar(any(CadastrarRetiradaRequest.class)))
				.thenThrow(new DescricaoMetodoVerificacaoObrigatoriaException());

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.codigo").value("DESCRICAO_METODO_VERIFICACAO_OBRIGATORIA"))
				.andExpect(jsonPath("$.erro").value("Descrição do método de verificação obrigatória"))
				.andExpect(jsonPath("$.mensagens[0]").isNotEmpty());

		verify(retiradaService).cadastrar(any(CadastrarRetiradaRequest.class));
	}

	@Test
	void deveRetornarBadRequestQuandoConfirmacaoRetiradaForObrigatoria() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				    "reservaId": 2,
				    "documentoRecebedor": "52998224725",
				    "nomeRecebedor": "João da Silva",
				    "metodoVerificacao": "PRESENCIAL",
				    "descricaoOutroMetodo": null,
				    "itens": [
				        {
				            "itemReservaId": 10,
				            "condicaoFisica": "SEM_DANOS_APARENTES",
				            "observacoes": null
				        }
				    ],
				    "autorizacaoConfirmada": true,
				    "recebimentoConfirmado": true
				}
				""";

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.cadastrar(any(CadastrarRetiradaRequest.class)))
				.thenThrow(new ConfirmacaoRetiradaObrigatoriaException());

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/retiradas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.codigo").value("CONFIRMACAO_RETIRADA_OBRIGATORIA"))
				.andExpect(jsonPath("$.erro").value("Confirmação da retirada obrigatória"))
				.andExpect(jsonPath("$.mensagens[0]").isNotEmpty());

		verify(retiradaService).cadastrar(any(CadastrarRetiradaRequest.class));
	}

	@Test
	void deveBuscarRetiradaPorIdComSucesso() throws Exception {

		// PREPARAR
		Long retiradaId = 30L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		OffsetDateTime dataHoraRetirada = OffsetDateTime.parse("2026-10-27T14:00:00-03:00");

		ItemRetiradaResponse itemResposta = new ItemRetiradaResponse(40L, 10L, 20L, "UNI-000020", "Marreta",
				CondicaoFisicaRetirada.SEM_DANOS_APARENTES, null);

		RetiradaResponse respostaDoService = new RetiradaResponse(retiradaId, 2L, dataHoraRetirada, "João da Silva",
				"52998224725", MetodoVerificacaoAutorizacao.PRESENCIAL, null, 5L, "Funcionário Carlos",
				List.of(itemResposta));

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.buscarPorId(retiradaId)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				get("/api/retiradas/{id}", retiradaId).header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(30))
				.andExpect(jsonPath("$.reservaId").value(2))
				.andExpect(jsonPath("$.nomeRecebedor").value("João da Silva"))
				.andExpect(jsonPath("$.documentoRecebedor").value("52998224725"))
				.andExpect(jsonPath("$.metodoVerificacao").value("PRESENCIAL"))
				.andExpect(jsonPath("$.usuarioResponsavelId").value(5))
				.andExpect(jsonPath("$.usuarioResponsavelNome").value("Funcionário Carlos"))
				.andExpect(jsonPath("$.itens.length()").value(1)).andExpect(jsonPath("$.itens[0].id").value(40))
				.andExpect(jsonPath("$.itens[0].itemReservaId").value(10))
				.andExpect(jsonPath("$.itens[0].unidadeEquipamentoId").value(20))
				.andExpect(jsonPath("$.itens[0].codigoUnidade").value("UNI-000020"))
				.andExpect(jsonPath("$.itens[0].equipamentoNome").value("Marreta"))
				.andExpect(jsonPath("$.itens[0].condicaoFisica").value("SEM_DANOS_APARENTES"));

		verify(retiradaService).buscarPorId(retiradaId);
	}

	@Test
	void deveRetornarNotFoundQuandoRetiradaNaoExistir() throws Exception {

		// PREPARAR
		Long retiradaId = 99L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.buscarPorId(retiradaId)).thenThrow(new RetiradaNaoEncontradaException());

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				get("/api/retiradas/{id}", retiradaId).header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.codigo").value("RETIRADA_NAO_ENCONTRADA"))
				.andExpect(jsonPath("$.erro").value("Retirada não encontrada"))
				.andExpect(jsonPath("$.mensagens[0]").isNotEmpty());

		verify(retiradaService).buscarPorId(retiradaId);
	}

	@Test
	void deveNegarConsultaRetiradaSemAutenticacao() throws Exception {

		// PREPARAR
		Long retiradaId = 30L;

		// MOCKS
		// Não precisamos configurar mocks neste cenário.

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(get("/api/retiradas/{id}", retiradaId));

		// VERIFICAR
		resultado.andExpect(status().isUnauthorized());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveNegarConsultaRetiradaSemPermissao() throws Exception {

		// PREPARAR
		Long retiradaId = 30L;

		Jwt jwtSemPermissao = Jwt.withTokenValue("token-sem-permissao").header("alg", "HS256").subject("5")
				.claim("perfil", "SEM_PERMISSAO").build();

		// MOCKS
		when(jwtDecoder.decode("token-sem-permissao")).thenReturn(jwtSemPermissao);

		when(conversorAutenticacaoJwt.convert(jwtSemPermissao)).thenReturn(
				new JwtAuthenticationToken(jwtSemPermissao, List.of(new SimpleGrantedAuthority("ROLE_SEM_PERMISSAO"))));

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				get("/api/retiradas/{id}", retiradaId).header(HttpHeaders.AUTHORIZATION, "Bearer token-sem-permissao"));

		// VERIFICAR
		resultado.andExpect(status().isForbidden());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveListarRetiradasComPaginacao() throws Exception {

		// ==================== PREPARAR ====================

		// 1. JWT do funcionário
		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// 2. Data e hora das retiradas
		OffsetDateTime dataHoraRetirada = OffsetDateTime.parse("2026-10-27T14:00:00-03:00");

		// 3. Itens das retiradas
		ItemRetiradaResponse itemResposta1 = new ItemRetiradaResponse(40L, 10L, 20L, "UNI-000020", "Marreta",
				CondicaoFisicaRetirada.SEM_DANOS_APARENTES, null);

		ItemRetiradaResponse itemResposta2 = new ItemRetiradaResponse(41L, 11L, 21L, "UNI-000021", "Marreta",
				CondicaoFisicaRetirada.SEM_DANOS_APARENTES, null);

		// 4. Primeira retirada
		RetiradaResponse primeiraRetirada = new RetiradaResponse(30L, 2L, dataHoraRetirada, "João da Silva",
				"52998224725", MetodoVerificacaoAutorizacao.PRESENCIAL, null, 5L, "Funcionário Carlos",
				List.of(itemResposta1));

		// 5. Segunda retirada
		RetiradaResponse segundaRetirada = new RetiradaResponse(40L, 3L, dataHoraRetirada, "João da Silva",
				"52998224725", MetodoVerificacaoAutorizacao.PRESENCIAL, null, 5L, "Funcionário Carlos",
				List.of(itemResposta2));

		// 6. Resposta paginada simulada
		PaginaResponse<RetiradaResponse> paginaDeResposta = new PaginaResponse<>(
				List.of(primeiraRetirada, segundaRetirada), 1, 2, 2, 5L, 3, true, false);

		// 7. Configuração de paginação
		Pageable paginacao = PageRequest.of(0, 2);

		// ===================== MOCKS ======================

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.listarPaginado(paginacao)).thenReturn(paginaDeResposta);

		// ==================== EXECUTAR ====================

		ResultActions resultado = mockMvc.perform(get("/api/retiradas").param("page", "0").param("size", "2")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// ==================== VERIFICAR ===================

		// 1. Status HTTP
		resultado.andExpect(status().isOk())

				// 2. Informações da paginação
				.andExpect(jsonPath("$.paginaAtual").value(1)).andExpect(jsonPath("$.tamanho").value(2))
				.andExpect(jsonPath("$.quantidadeElementos").value(2)).andExpect(jsonPath("$.totalElementos").value(5))
				.andExpect(jsonPath("$.totalPaginas").value(3)).andExpect(jsonPath("$.primeiraPagina").value(true))
				.andExpect(jsonPath("$.ultimaPagina").value(false)).andExpect(jsonPath("$.conteudo.length()").value(2))

				// 3. Primeira retirada
				.andExpect(jsonPath("$.conteudo[0].id").value(30))
				.andExpect(jsonPath("$.conteudo[0].reservaId").value(2))
				.andExpect(jsonPath("$.conteudo[0].nomeRecebedor").value("João da Silva"))
				.andExpect(jsonPath("$.conteudo[0].usuarioResponsavelId").value(5))
				.andExpect(jsonPath("$.conteudo[0].itens.length()").value(1))
				.andExpect(jsonPath("$.conteudo[0].itens[0].codigoUnidade").value("UNI-000020"))

				// 4. Segunda retirada
				.andExpect(jsonPath("$.conteudo[1].id").value(40))
				.andExpect(jsonPath("$.conteudo[1].reservaId").value(3))
				.andExpect(jsonPath("$.conteudo[1].nomeRecebedor").value("João da Silva"))
				.andExpect(jsonPath("$.conteudo[1].itens.length()").value(1))
				.andExpect(jsonPath("$.conteudo[1].itens[0].codigoUnidade").value("UNI-000021"));

		// 5. Verificar chamada ao service
		verify(retiradaService).listarPaginado(paginacao);
	}

	@Test
	void deveListarRetiradasComPaginaVazia() throws Exception {

		// ==================== PREPARAR ====================

		// 1. JWT do funcionário
		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("5")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// 2. Configuração da paginação
		Pageable paginacao = PageRequest.of(0, 10);

		// 3. Resposta paginada vazia
		PaginaResponse<RetiradaResponse> paginaVazia = new PaginaResponse<>(List.of(), 1, 10, 0, 0L, 0, true, true);

		// ===================== MOCKS ======================

		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		when(conversorAutenticacaoJwt.convert(jwtFuncionario)).thenReturn(
				new JwtAuthenticationToken(jwtFuncionario, List.of(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"))));

		when(retiradaService.listarPaginado(paginacao)).thenReturn(paginaVazia);

		// ==================== EXECUTAR ====================

		ResultActions resultado = mockMvc.perform(get("/api/retiradas").param("page", "0").param("size", "10")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// ==================== VERIFICAR ===================

		// 1. Status HTTP
		resultado.andExpect(status().isOk())

				// 2. Informações da paginação
				.andExpect(jsonPath("$.paginaAtual").value(1)).andExpect(jsonPath("$.tamanho").value(10))
				.andExpect(jsonPath("$.quantidadeElementos").value(0)).andExpect(jsonPath("$.totalElementos").value(0))
				.andExpect(jsonPath("$.totalPaginas").value(0)).andExpect(jsonPath("$.primeiraPagina").value(true))
				.andExpect(jsonPath("$.ultimaPagina").value(true))

				// 3. Conteúdo vazio
				.andExpect(jsonPath("$.conteudo").isArray()).andExpect(jsonPath("$.conteudo").isEmpty());

		// 4. Verificar chamada ao service
		verify(retiradaService).listarPaginado(paginacao);
	}

	@Test
	void deveNegarListagemRetiradasSemAutenticacao() throws Exception {

		// PREPARAR
		Pageable paginacao = PageRequest.of(0, 10);

		// MOCKS
		// Nenhum mock necessário.

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(get("/api/retiradas").param("page", "0").param("size", "10"));

		// VERIFICAR
		resultado.andExpect(status().isUnauthorized());

		verifyNoInteractions(retiradaService);
	}

	@Test
	void deveNegarListagemRetiradasSemPermissao() throws Exception {

		// ==================== PREPARAR ====================

		Jwt jwtSemPermissao = Jwt.withTokenValue("token-sem-permissao").header("alg", "HS256").subject("5")
				.claim("perfil", "SEM_PERMISSAO").build();

		// ===================== MOCKS ======================

		when(jwtDecoder.decode("token-sem-permissao")).thenReturn(jwtSemPermissao);

		when(conversorAutenticacaoJwt.convert(jwtSemPermissao)).thenReturn(
				new JwtAuthenticationToken(jwtSemPermissao, List.of(new SimpleGrantedAuthority("ROLE_SEM_PERMISSAO"))));

		// ==================== EXECUTAR ====================

		ResultActions resultado = mockMvc.perform(get("/api/retiradas").param("page", "0").param("size", "10")
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-sem-permissao"));

		// ==================== VERIFICAR ===================

		resultado.andExpect(status().isForbidden());

		verifyNoInteractions(retiradaService);
	}

}
