
package br.com.projetosecsr.aluguelequipamentos.autenticacao.seguranca;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ConversorAutenticacaoJwtTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@InjectMocks
	private ConversorAutenticacaoJwt conversor;

	@Test
	void deveRejeitarAutenticacaoQuandoUsuarioNaoExistir() {

		// PREPARAR
		Jwt jwt = criarJwt("15", "FUNCIONARIO");

		// MOCKS
		when(usuarioRepository.findById(15L)).thenReturn(Optional.empty());

		// EXECUTAR E VERIFICAR
		assertThrows(BadCredentialsException.class, () -> conversor.convert(jwt));

		verify(usuarioRepository).findById(15L);
	}

	@Test
	void deveRejeitarAutenticacaoQuandoUsuarioEstiverInativo() {

		// PREPARAR
		Jwt jwt = criarJwt("15", "FUNCIONARIO");

		Usuario usuario = mock(Usuario.class);

		// MOCKS
		when(usuarioRepository.findById(15L)).thenReturn(Optional.of(usuario));

		when(usuario.isAtivo()).thenReturn(false);

		// EXECUTAR E VERIFICAR
		assertThrows(BadCredentialsException.class, () -> conversor.convert(jwt));

		verify(usuarioRepository).findById(15L);
	}

	@Test
	void deveRejeitarAutenticacaoQuandoSubjectNaoForNumerico() {

		// PREPARAR
		Jwt jwt = criarJwt("abc", "FUNCIONARIO");

		// EXECUTAR E VERIFICAR
		assertThrows(BadCredentialsException.class, () -> conversor.convert(jwt));

		verifyNoInteractions(usuarioRepository);
	}

	@Test
	void deveAutenticarUsuarioAtivoComSeuPerfil() {

		// PREPARAR
		Jwt jwt = criarJwt("15", "FUNCIONARIO");

		Usuario usuario = mock(Usuario.class);

		// MOCKS
		when(usuarioRepository.findById(15L)).thenReturn(Optional.of(usuario));

		when(usuario.isAtivo()).thenReturn(true);
		when(usuario.getPerfil()).thenReturn(PerfilUsuario.FUNCIONARIO);

		// EXECUTAR
		AbstractAuthenticationToken resultado = conversor.convert(jwt);

		// VERIFICAR
		assertInstanceOf(JwtAuthenticationToken.class, resultado);
		assertTrue(resultado.isAuthenticated());
		assertEquals("15", resultado.getName());

		assertEquals(1, resultado.getAuthorities().size());

		assertEquals("ROLE_FUNCIONARIO", resultado.getAuthorities().iterator().next().getAuthority());
	}

	@Test
	void deveUtilizarPerfilAtualDoBancoEmVezDoPerfilDoJwt() {

		// PREPARAR
		Jwt jwt = criarJwt("15", "ADMINISTRADOR");

		Usuario usuario = mock(Usuario.class);

		// MOCKS
		when(usuarioRepository.findById(15L)).thenReturn(Optional.of(usuario));

		when(usuario.isAtivo()).thenReturn(true);
		when(usuario.getPerfil()).thenReturn(PerfilUsuario.FUNCIONARIO);

		// EXECUTAR
		AbstractAuthenticationToken resultado = conversor.convert(jwt);

		// VERIFICAR
		assertEquals("ROLE_FUNCIONARIO", resultado.getAuthorities().iterator().next().getAuthority());
	}

	private Jwt criarJwt(String usuarioId, String perfil) {

		return Jwt.withTokenValue("token-teste").header("alg", "HS256").subject(usuarioId).claim("perfil", perfil)
				.build();
	}
}
