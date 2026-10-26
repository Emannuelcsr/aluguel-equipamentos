
package br.com.projetosecsr.aluguelequipamentos.autenticacao.seguranca;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.configuracao.ConfiguracaoDeSeguranca;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorAcessoNegado;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorFalhaAutenticacao;
import br.com.projetosecsr.aluguelequipamentos.reserva.controller.ReservaController;
import br.com.projetosecsr.aluguelequipamentos.reserva.service.ReservaService;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.repository.UsuarioRepository;

@WebMvcTest(controllers = ReservaController.class)
@Import({ ConfiguracaoDeSeguranca.class, ConversorAutenticacaoJwt.class, TratadorFalhaAutenticacao.class,
		TratadorAcessoNegado.class })
class ConversorAutenticacaoJwtIntegracaoTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@MockitoBean
	private UsuarioRepository usuarioRepository;

	@MockitoBean
	private ReservaService reservaService;

	@Test
	void deveRetornar401QuandoUsuarioDoJwtEstiverInativo() throws Exception {

		// REGRA:
		// Um usuário desativado não pode acessar endpoints protegidos.

		// CENÁRIO:
		// O JWT é válido, mas o usuário foi desativado.

		// PREPARAR
		Jwt jwt = Jwt.withTokenValue("token-funcionario").header("alg", "HS256").subject("15")
				.claim("perfil", "FUNCIONARIO").build();

		Usuario usuario = mock(Usuario.class);

		// MOCKS
		when(jwtDecoder.decode("token-funcionario")).thenReturn(jwt);

		when(usuarioRepository.findById(15L)).thenReturn(Optional.of(usuario));

		when(usuario.isAtivo()).thenReturn(false);

		// EXECUTAR E VERIFICAR
		mockMvc.perform(get("/api/reservas").header(HttpHeaders.AUTHORIZATION, "Bearer token-funcionario"))
				.andExpect(status().isUnauthorized());

		verify(jwtDecoder).decode("token-funcionario");
		verify(usuarioRepository).findById(15L);
		verify(reservaService, never()).listarPaginado(any());
	}

	@Test
	void deveRetornar401QuandoUsuarioDoJwtNaoExistir() throws Exception {

		// PREPARAR
		Jwt jwt = Jwt.withTokenValue("token-inexistente").header("alg", "HS256").subject("99")
				.claim("perfil", "FUNCIONARIO").build();

		// MOCKS
		when(jwtDecoder.decode("token-inexistente")).thenReturn(jwt);

		when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

		// EXECUTAR E VERIFICAR
		mockMvc.perform(get("/api/reservas").header(HttpHeaders.AUTHORIZATION, "Bearer token-inexistente"))
				.andExpect(status().isUnauthorized());

		verify(jwtDecoder).decode("token-inexistente");
		verify(usuarioRepository).findById(99L);
		verifyNoInteractions(reservaService);
	}

	@Test
	void deveRetornar403QuandoPerfilAtualNaoPermitirAprovacao() throws Exception {

		// PREPARAR
		Jwt jwt = Jwt.withTokenValue("token-administrador-antigo").header("alg", "HS256").subject("15")
				.claim("perfil", "ADMINISTRADOR").build();

		Usuario usuario = mock(Usuario.class);

		// MOCKS
		when(jwtDecoder.decode("token-administrador-antigo")).thenReturn(jwt);

		when(usuarioRepository.findById(15L)).thenReturn(Optional.of(usuario));

		when(usuario.isAtivo()).thenReturn(true);

		when(usuario.getPerfil()).thenReturn(PerfilUsuario.FUNCIONARIO);

		// EXECUTAR E VERIFICAR
		mockMvc.perform(patch("/api/reservas/{id}/aprovar", 10L).header(HttpHeaders.AUTHORIZATION,
				"Bearer token-administrador-antigo")).andExpect(status().isForbidden());

		verify(jwtDecoder).decode("token-administrador-antigo");
		verify(usuarioRepository).findById(15L);

		verify(reservaService, never()).aprovar(10L);
	}

}
