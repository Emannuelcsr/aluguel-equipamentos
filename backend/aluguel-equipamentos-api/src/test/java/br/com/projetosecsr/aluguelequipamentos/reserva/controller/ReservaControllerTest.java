package br.com.projetosecsr.aluguelequipamentos.reserva.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import br.com.projetosecsr.aluguelequipamentos.reserva.request.CadastrarReservaRequest;
import br.com.projetosecsr.aluguelequipamentos.reserva.response.ItemReservaResponse;
import br.com.projetosecsr.aluguelequipamentos.reserva.response.ReservaResponse;
import br.com.projetosecsr.aluguelequipamentos.reserva.service.ReservaService;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;

@WebMvcTest(controllers = ReservaController.class)
@Import({ ConfiguracaoDeSeguranca.class, TratadorGlobalDeErros.class, TratadorFalhaAutenticacao.class,
		TratadorAcessoNegado.class })
public class ReservaControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@MockitoBean
	private ConversorAutenticacaoJwt conversorAutenticacaoJwt;
	
	
	@MockitoBean
	private ReservaService reservaService;

	@MockitoBean
	private JwtDecoder jwtDecoder;
	
	@BeforeEach
	void prepararConversor() {

	    JwtGrantedAuthoritiesConverter conversorDeAutoridades =
	            new JwtGrantedAuthoritiesConverter();

	    conversorDeAutoridades.setAuthoritiesClaimName("perfil");
	    conversorDeAutoridades.setAuthorityPrefix("ROLE_");

	    JwtAuthenticationConverter conversorPadrao =
	            new JwtAuthenticationConverter();

	    conversorPadrao.setJwtGrantedAuthoritiesConverter(conversorDeAutoridades);

	    when(conversorAutenticacaoJwt.convert(any(Jwt.class)))
	            .thenAnswer(invocacao ->
	                    conversorPadrao.convert(invocacao.getArgument(0)));
	}

	@Test
	void deveCadastrarReservar() throws Exception {

		// PREPARAR
		String corpoRequisicao = """
				{
				  "clienteId": 1,
				  "dataInicial": "2026-10-20",
				  "dataFinal": "2026-10-23",
				  "unidadesIds": [2, 3]
				}
				""";

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-10-15T15:00:00Z");
		List<ItemReservaResponse> itens = List.of();

		ReservaResponse respostaDoService = new ReservaResponse(10L, 1L, "João da Silva", LocalDate.of(2026, 10, 20),
				LocalDate.of(2026, 10, 23), "SOLICITADA", new BigDecimal("1800.00"), itens, data, data);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);
		when(conversorAutenticacaoJwt.convert(jwtAdministrador))
        .thenReturn(new JwtAuthenticationToken(
                jwtAdministrador,
                List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))
        ));


		when(reservaService.cadastrar(any(CadastrarReservaRequest.class))).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(post("/api/reservas").header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador")
						.contentType(MediaType.APPLICATION_JSON).content(corpoRequisicao));

		// VERIFICAR
		resultado.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.clienteId").value(1)).andExpect(jsonPath("$.clienteNome").value("João da Silva"))
				.andExpect(jsonPath("$.dataInicial").value("2026-10-20"))
				.andExpect(jsonPath("$.dataFinal").value("2026-10-23"))
				.andExpect(jsonPath("$.status").value("SOLICITADA")).andExpect(jsonPath("$.valorTotal").value(1800.00));

		verify(jwtDecoder).decode("token-administrador");

		verify(reservaService).cadastrar(any(CadastrarReservaRequest.class));

	}

	@Test
	void deveBuscarReservaPorId() throws Exception {

		// PREPARAR
		Long reservaId = 10L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-10-15T15:00:00Z");
		List<ItemReservaResponse> itens = List.of();

		ReservaResponse respostaDoService = new ReservaResponse(10L, 1L, "João da Silva", LocalDate.of(2026, 10, 20),
				LocalDate.of(2026, 10, 23), "SOLICITADA", new BigDecimal("1800.00"), itens, data, data);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(reservaService.buscarPorId(reservaId)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(
				get("/api/reservas/{id}", reservaId).header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.clienteId").value(1)).andExpect(jsonPath("$.clienteNome").value("João da Silva"))
				.andExpect(jsonPath("$.dataInicial").value("2026-10-20"))
				.andExpect(jsonPath("$.dataFinal").value("2026-10-23"))
				.andExpect(jsonPath("$.status").value("SOLICITADA")).andExpect(jsonPath("$.valorTotal").value(1800.00));

		verify(jwtDecoder).decode("token-administrador");

		verify(reservaService).buscarPorId(reservaId);
	}

	@Test
	void deveListarReservasComPaginacao() throws Exception {

		// PREPARAR
		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-10-15T15:00:00Z");

		ReservaResponse reserva1 = new ReservaResponse(10L, 1L, "João da Silva", LocalDate.of(2026, 10, 20),
				LocalDate.of(2026, 10, 23), "SOLICITADA", new BigDecimal("1800.00"), List.of(), data, data);

		ReservaResponse reserva2 = new ReservaResponse(20L, 1L, "João da Silva", LocalDate.of(2026, 10, 25),
				LocalDate.of(2026, 10, 27), "APROVADA", new BigDecimal("2700.00"), List.of(), data, data);

		PaginaResponse<ReservaResponse> respostaDoService = new PaginaResponse<>(List.of(reserva1, reserva2), 1, 2, 2,
				5L, 3, true, false);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(reservaService.listarPaginado(any(Pageable.class))).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc
				.perform(get("/api/reservas").header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.paginaAtual").value(1))
				.andExpect(jsonPath("$.tamanho").value(2)).andExpect(jsonPath("$.quantidadeElementos").value(2))
				.andExpect(jsonPath("$.totalElementos").value(5)).andExpect(jsonPath("$.totalPaginas").value(3))
				.andExpect(jsonPath("$.primeiraPagina").value(true)).andExpect(jsonPath("$.ultimaPagina").value(false))
				.andExpect(jsonPath("$.conteudo.length()").value(2)).andExpect(jsonPath("$.conteudo[0].id").value(10))
				.andExpect(jsonPath("$.conteudo[0].status").value("SOLICITADA"))
				.andExpect(jsonPath("$.conteudo[1].id").value(20))
				.andExpect(jsonPath("$.conteudo[1].status").value("APROVADA"));

		verify(jwtDecoder).decode("token-administrador");
		verify(reservaService).listarPaginado(any(Pageable.class));
	}

	@Test
	void deveAprovarReserva() throws Exception {

		// PREPARAR
		Long reservaId = 10L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-10-15T15:00:00Z");

		ReservaResponse respostaDoService = new ReservaResponse(reservaId, 1L, "João da Silva",
				LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 23), "APROVADA", new BigDecimal("1800.00"),
				List.of(), data, data);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(reservaService.aprovar(reservaId)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/reservas/{id}/aprovar", reservaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.status").value("APROVADA"));

		verify(jwtDecoder).decode("token-administrador");
		verify(reservaService).aprovar(reservaId);
	}

	@Test
	void deveRejeitarReserva() throws Exception {

		// PREPARAR
		Long reservaId = 10L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-10-15T15:00:00Z");

		ReservaResponse respostaDoService = new ReservaResponse(reservaId, 1L, "João da Silva",
				LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 23), "REJEITADA", new BigDecimal("1800.00"),
				List.of(), data, data);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(reservaService.rejeitar(reservaId)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/reservas/{id}/rejeitar", reservaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.status").value("REJEITADA"));

		verify(jwtDecoder).decode("token-administrador");
		verify(reservaService).rejeitar(reservaId);
	}

	@Test
	void deveCancelarReserva() throws Exception {

		// PREPARAR
		Long reservaId = 10L;

		Jwt jwtAdministrador = Jwt.withTokenValue("token-administrador").header("alg", "HS256").subject("1")
				.claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		Instant data = Instant.parse("2026-10-15T15:00:00Z");

		ReservaResponse respostaDoService = new ReservaResponse(reservaId, 1L, "João da Silva",
				LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 23), "CANCELADA", new BigDecimal("1800.00"),
				List.of(), data, data);

		// MOCKS
		when(jwtDecoder.decode("token-administrador")).thenReturn(jwtAdministrador);

		when(reservaService.cancelar(reservaId)).thenReturn(respostaDoService);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/reservas/{id}/cancelar", reservaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-administrador"));

		// VERIFICAR
		resultado.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.status").value("CANCELADA"));

		verify(jwtDecoder).decode("token-administrador");
		verify(reservaService).cancelar(reservaId);
	}

	@Test
	void deveNegarAprovacaoParaFuncionario() throws Exception {

		// PREPARAR
		Long reservaId = 10L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/reservas/{id}/aprovar", reservaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isForbidden());

		verify(jwtDecoder).decode("token-funcionario");

		verify(reservaService, never()).aprovar(reservaId);
	}

	@Test
	void deveNegarRejeitarParaFuncionario() throws Exception {

		// PREPARAR
		Long reservaId = 10L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/reservas/{id}/rejeitar", reservaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isForbidden());

		verify(jwtDecoder).decode("token-funcionario");

		verify(reservaService, never()).rejeitar(reservaId);
	}

	@Test
	void deveNegarCancelarParaFuncionario() throws Exception {

		// PREPARAR
		Long reservaId = 10L;

		Jwt jwtFuncionario = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("2")
				.claim("perfil", PerfilUsuario.FUNCIONARIO.name()).build();

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwtFuncionario);

		// EXECUTAR
		ResultActions resultado = mockMvc.perform(patch("/api/reservas/{id}/cancelar", reservaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"));

		// VERIFICAR
		resultado.andExpect(status().isForbidden());

		verify(jwtDecoder).decode("token-funcionario");

		verify(reservaService, never()).cancelar(reservaId);
	}

}
