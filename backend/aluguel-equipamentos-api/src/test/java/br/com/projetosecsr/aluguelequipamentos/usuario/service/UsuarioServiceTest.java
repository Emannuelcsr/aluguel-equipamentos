package br.com.projetosecsr.aluguelequipamentos.usuario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.EmailJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.UsuarioNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.repository.UsuarioRepository;
import br.com.projetosecsr.aluguelequipamentos.usuario.request.CadastrarUsuarioRequest;
import br.com.projetosecsr.aluguelequipamentos.usuario.response.UsuarioResponse;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UsuarioService usuarioService;

	@Test
	void deveCadastrarUsuarioQuandoEmailNaoExistir() {

		// PREPARAR
		CadastrarUsuarioRequest request = new CadastrarUsuarioRequest(" Maria Souza ", " MARIa@Empresa.com ",
				"uma-senha-bem-segura", PerfilUsuario.FUNCIONARIO);

		when(usuarioRepository.existsByEmail("maria@empresa.com")).thenReturn(false);

		when(passwordEncoder.encode("uma-senha-bem-segura")).thenReturn("hash-gerado");

		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

		// EXECUTAR
		UsuarioResponse resposta = usuarioService.cadastrar(request);

		// VERIFICAR
		verify(usuarioRepository).existsByEmail("maria@empresa.com");
		verify(passwordEncoder).encode("uma-senha-bem-segura");

		ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);

		verify(usuarioRepository).save(usuarioCaptor.capture());

		Usuario usuarioEnviadoParaSalvar = usuarioCaptor.getValue();

		assertEquals("Maria Souza", usuarioEnviadoParaSalvar.getNome());
		assertEquals("maria@empresa.com", usuarioEnviadoParaSalvar.getEmail());
		assertEquals("hash-gerado", usuarioEnviadoParaSalvar.getSenhaHash());
		assertEquals(PerfilUsuario.FUNCIONARIO, usuarioEnviadoParaSalvar.getPerfil());
		assertTrue(usuarioEnviadoParaSalvar.isAtivo());

		assertEquals("Maria Souza", resposta.nome());
		assertEquals("maria@empresa.com", resposta.email());
		assertEquals(PerfilUsuario.FUNCIONARIO, resposta.perfil());
		assertTrue(resposta.ativo());

	}

	@Test
	void deveRecusarCadastroQuandoEmailJaExistir() {

		// PREPARAR
		CadastrarUsuarioRequest request = new CadastrarUsuarioRequest("Maria Souza", "  MARIA@EMPRESA.COM  ",
				"uma-senha-bem-segura", PerfilUsuario.FUNCIONARIO);

		when(usuarioRepository.existsByEmail("maria@empresa.com")).thenReturn(true);

		// EXECUTAR
		EmailJaCadastradoException excecao = assertThrows(EmailJaCadastradoException.class,
				() -> usuarioService.cadastrar(request));

		// VERIFICAR
		assertEquals("O e-mail informado já está cadastrado.", excecao.getMessage());

		verify(usuarioRepository).existsByEmail("maria@empresa.com");

		verifyNoInteractions(passwordEncoder);

		verify(usuarioRepository, never()).save(any(Usuario.class));

	}

	@Test
	void deveBuscarUsuarioPorIdQuandoUsuarioExistir() {

		// PREPARAR
		Long usuarioId = 10l;
		Usuario usuario = new Usuario("Maria Souza", "maria@empresa.com", "hash-da-senha", PerfilUsuario.FUNCIONARIO);
		ReflectionTestUtils.setField(usuario, "id", usuarioId);

		// MOCKS
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

		// EXECUTAR
		UsuarioResponse resposta = usuarioService.buscarPorId(usuarioId);

		// VERIFICAR
		verify(usuarioRepository).findById(usuarioId);

		verifyNoInteractions(passwordEncoder);

		assertEquals(usuarioId, resposta.id());
		assertEquals("Maria Souza", resposta.nome());
		assertEquals("maria@empresa.com", resposta.email());
		assertEquals(PerfilUsuario.FUNCIONARIO, resposta.perfil());
		assertTrue(resposta.ativo());
	}

	@Test
	void deveLancarExcecaoQuandoUsuarioNaoForEncontradoPorId() {

		// PREPARAR
		Long usuarioId = 999L;

		// MOKS
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

		// EXECUTAR
		UsuarioNaoEncontradoException excecao = assertThrows(UsuarioNaoEncontradoException.class,
				() -> usuarioService.buscarPorId(usuarioId));

		// VERIFICAR
		assertEquals("Usuário não encontrado.", excecao.getMessage());

		verify(usuarioRepository).findById(usuarioId);

		verifyNoInteractions(passwordEncoder);

	}

}
