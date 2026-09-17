package br.com.projetosecsr.aluguelequipamentos.autenticacao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.projetosecsr.aluguelequipamentos.autenticacao.excecao.CredenciaisInvalidasException;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.excecao.UsuarioInativoException;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.request.LoginRequest;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.response.LoginResponse;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.token.ServicoToken;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.token.TokenGerado;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
public class AutenticacaoServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private ServicoToken servicoToken;

	@InjectMocks
	private AutenticacaoService autenticacaoService;

	@Test
	void deveAutenticarUsuarioAtivoComCredenciaisCorretar() {

		// Preparar

		LoginRequest request = new LoginRequest(" ADMIN@EMPRESA.COM ", "senha-segura");

		Usuario usuario = mock(Usuario.class);

		Instant expiraEm = Instant.parse("2026-09-17T15:30:00Z");

		TokenGerado tokenGerado = new TokenGerado("token-assinado", expiraEm);

		// MOCKS

		when(usuario.getId()).thenReturn(10L);

		when(usuario.getNome()).thenReturn("Administrador");

		when(usuario.getEmail()).thenReturn("admin@empresa.com");

		when(usuario.getSenhaHash()).thenReturn("hash-armazenado");

		when(usuario.getPerfil()).thenReturn(PerfilUsuario.ADMINISTRADOR);

		when(usuario.isAtivo()).thenReturn(true);

		when(usuarioRepository.findByEmail("admin@empresa.com")).thenReturn(Optional.of(usuario));

		when(passwordEncoder.matches("senha-segura", "hash-armazenado")).thenReturn(true);

		when(servicoToken.gerar(usuario)).thenReturn(tokenGerado);

		// EXECUTAR

		LoginResponse response = autenticacaoService.autenticar(request);

		assertEquals("token-assinado", response.token());

		assertEquals("Bearer", response.tipo());

		assertEquals(expiraEm, response.expiraEm());

		assertEquals(10L, response.usuario().id());

		assertEquals("Administrador", response.usuario().nome());

		assertEquals("admin@empresa.com", response.usuario().email());

		assertEquals(PerfilUsuario.ADMINISTRADOR, response.usuario().perfil());

		verify(usuarioRepository).findByEmail("admin@empresa.com");

		verify(passwordEncoder).matches("senha-segura", "hash-armazenado");

		verify(servicoToken).gerar(usuario);

	}

	@Test
	void deveRejeitarLoginQuandoEmailNaoExistir() {

		// PREPARAR

		LoginRequest request = new LoginRequest("inexistente@empresa.com", "qualquer-senha");

		// MOCKS

		when(usuarioRepository.findByEmail("inexistente@empresa.com")).thenReturn(Optional.empty());

		// EXECUTAR

		CredenciaisInvalidasException excecao = assertThrows(CredenciaisInvalidasException.class,
				() -> autenticacaoService.autenticar(request));

		// VERIFICAR
		assertEquals("E-mail ou senha inválidos.", excecao.getMessage());

		verify(usuarioRepository).findByEmail("inexistente@empresa.com");

		verifyNoInteractions(passwordEncoder, servicoToken);

	}

	@Test
	void deveRejeitarLoginQuandoSenhaForIncorreta() {

		LoginRequest request = new LoginRequest("admin@empresa.com", "senha-errada");

		Usuario usuario = mock(Usuario.class);

		when(usuario.getSenhaHash()).thenReturn("hash-armazenado");

		when(usuarioRepository.findByEmail("admin@empresa.com")).thenReturn(Optional.of(usuario));

		when(passwordEncoder.matches("senha-errada", "hash-armazenado")).thenReturn(false);

		// EXECUTAR

		CredenciaisInvalidasException excecao =
				assertThrows(
						CredenciaisInvalidasException.class,
						() -> autenticacaoService.autenticar(request)
				);

		// VERIFICAR

		assertEquals(
				"E-mail ou senha inválidos.",
				excecao.getMessage()
		);

		verify(usuarioRepository)
				.findByEmail("admin@empresa.com");

		verify(passwordEncoder).matches(
				"senha-errada",
				"hash-armazenado"
		);

		verify(usuario, never()).isAtivo();

		verifyNoInteractions(servicoToken);

	}


	@Test
	void deveRejeitarLoginQuandoUsuarioEstiverInativo() {

		// PREPARAR

		LoginRequest request = new LoginRequest(
				"admin@empresa.com",
				"senha-correta"
		);

		Usuario usuario = mock(Usuario.class);

		// MOCKS

		when(usuario.getSenhaHash())
				.thenReturn("hash-armazenado");

		when(usuario.isAtivo()).thenReturn(false);

		when(usuarioRepository.findByEmail(
				"admin@empresa.com"
		)).thenReturn(Optional.of(usuario));

		when(passwordEncoder.matches(
				"senha-correta",
				"hash-armazenado"
		)).thenReturn(true);

		// EXECUTAR

		UsuarioInativoException excecao =
				assertThrows(
						UsuarioInativoException.class,
						() -> autenticacaoService.autenticar(request)
				);

		// VERIFICAR

		assertEquals(
				"A conta do usuário está inativa.",
				excecao.getMessage()
		);

		verify(usuarioRepository)
				.findByEmail("admin@empresa.com");

		verify(passwordEncoder).matches(
				"senha-correta",
				"hash-armazenado"
		);

		verify(usuario).isAtivo();

		verifyNoInteractions(servicoToken);
	}

}
