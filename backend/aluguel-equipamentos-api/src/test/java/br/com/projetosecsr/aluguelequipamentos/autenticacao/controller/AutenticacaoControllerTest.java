package br.com.projetosecsr.aluguelequipamentos.autenticacao.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.projetosecsr.aluguelequipamentos.autenticacao.excecao.CredenciaisInvalidasException;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.excecao.UsuarioInativoException;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.request.LoginRequest;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.response.LoginResponse;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.response.UsuarioAutenticadoResponse;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.service.AutenticacaoService;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.configuracao.ConfiguracaoDeSeguranca;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.erro.TratadorGlobalDeErros;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorAcessoNegado;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorFalhaAutenticacao;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;

@Import({ ConfiguracaoDeSeguranca.class, TratadorGlobalDeErros.class, TratadorFalhaAutenticacao.class,
		TratadorAcessoNegado.class })
@WebMvcTest(controllers = AutenticacaoController.class)
public class AutenticacaoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@MockitoBean
	private AutenticacaoService autenticacaoService;

	@Test
	void deveRealizarLoginQuandoDadosForemValidos() throws Exception {

		Instant expiraEm = Instant.parse("2026-09-17T15:00:00Z");

		UsuarioAutenticadoResponse usuario = new UsuarioAutenticadoResponse(1L, "Administrador", "admin@empresa.com",
				PerfilUsuario.ADMINISTRADOR);

		LoginResponse respostaDoService = new LoginResponse("token-jwt-teste", "Bearer", expiraEm, usuario);

		when(autenticacaoService.autenticar(any(LoginRequest.class))).thenReturn(respostaDoService);
		mockMvc.perform(post("/api/autenticacao/login").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "email": "admin@empresa.com",
				  "senha": "senha-segura-com-15-caracteres"
				}
				"""))

				.andExpect(status().isOk()).andExpect(jsonPath("$.token").value("token-jwt-teste"))
				.andExpect(jsonPath("$.tipo").value("Bearer"))
				.andExpect(jsonPath("$.expiraEm").value("2026-09-17T15:00:00Z"))
				.andExpect(jsonPath("$.usuario.id").value(1))
				.andExpect(jsonPath("$.usuario.nome").value("Administrador"))
				.andExpect(jsonPath("$.usuario.email").value("admin@empresa.com"))
				.andExpect(jsonPath("$.usuario.perfil").value("ADMINISTRADOR"));

	}

	@Test
	void deveRetornarErroQuandoEmailEstiverVazio() throws Exception {

		mockMvc.perform(post("/api/autenticacao/login").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "email": "",
				  "senha": "senha-segura-com-15-caracteres"
				}
				""")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.erro").value("Dados inválidos"))
				.andExpect(jsonPath("$.mensagens[0]").value("O e-mail é obrigatório."))
				.andExpect(jsonPath("$.path").value("/api/autenticacao/login"))
				.andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"));

		verifyNoInteractions(autenticacaoService);
	}

	@Test
	void deveRetornarNaoAutorizadoQuandoCredenciaisForemInvalidas() throws Exception {

		when(autenticacaoService.autenticar(any(LoginRequest.class))).thenThrow(new CredenciaisInvalidasException());

		mockMvc.perform(post("/api/autenticacao/login").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "email": "admin@empresa.com",
				  "senha": "senha-incorreta"
				}
				""")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.erro").value("Não autorizado"))
				.andExpect(jsonPath("$.mensagens[0]").value("E-mail ou senha inválidos."))
				.andExpect(jsonPath("$.path").value("/api/autenticacao/login"))
				.andExpect(jsonPath("$.codigo").value("CREDENCIAIS_INVALIDAS"));
	}

	@Test
	void deveRetornarAcessoNegadoQuandoUsuarioEstiverInativo() throws Exception {

		when(autenticacaoService.autenticar(any(LoginRequest.class))).thenThrow(new UsuarioInativoException());

		mockMvc.perform(post("/api/autenticacao/login").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "email": "admin@empresa.com",
				  "senha": "senha-segura-com-15-caracteres"
				}
				""")).andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.erro").value("Acesso negado"))
				.andExpect(jsonPath("$.mensagens[0]").value("A conta do usuário está inativa."))
				.andExpect(jsonPath("$.path").value("/api/autenticacao/login"))
				.andExpect(jsonPath("$.codigo").value("USUARIO_INATIVO"));
	}

}
