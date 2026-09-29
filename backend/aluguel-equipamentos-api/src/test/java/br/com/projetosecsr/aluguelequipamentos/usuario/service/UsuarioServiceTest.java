package br.com.projetosecsr.aluguelequipamentos.usuario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.AutodesativacaoNaoPermitidaException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.ConfirmacaoSenhaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.EmailJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.NovaSenhaIgualAtualException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.RedefinicaoPropriaSenhaNaoPermitidaException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.SenhaAtualIncorretaException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.UsuarioJaAtivoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.UsuarioJaInativoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.UsuarioNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.repository.UsuarioRepository;
import br.com.projetosecsr.aluguelequipamentos.usuario.request.AlterarPropriaSenhaRequest;
import br.com.projetosecsr.aluguelequipamentos.usuario.request.AtualizarUsuarioRequest;
import br.com.projetosecsr.aluguelequipamentos.usuario.request.CadastrarUsuarioRequest;
import br.com.projetosecsr.aluguelequipamentos.usuario.request.RedefinirSenhaUsuarioRequest;
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

	@Test
	void deveListarUsuariosComPaginacao() {

		// PREPARAR
		Long idUser1 = 1L;
		Long idUser2 = 2L;

		Pageable paginacao = PageRequest.of(0, 2);

		Usuario usuarioAdministrador = new Usuario("Administrador Teste", "administrador@empresa.com",
				"hash-administrador", PerfilUsuario.ADMINISTRADOR);
		ReflectionTestUtils.setField(usuarioAdministrador, "id", idUser1);

		Usuario usuarioFuncionario = new Usuario("Funcionario Teste", "funcionario@empresa.com", "hash-funcionario",
				PerfilUsuario.FUNCIONARIO);
		ReflectionTestUtils.setField(usuarioFuncionario, "id", idUser2);

		Page<Usuario> paginaDeUsuarios = new PageImpl<>(List.of(usuarioAdministrador, usuarioFuncionario), paginacao,
				5);

		// MOCKS
		when(usuarioRepository.findAll(paginacao)).thenReturn(paginaDeUsuarios);

		// EXECUTAR
		PaginaResponse<UsuarioResponse> resposta = usuarioService.listarPaginado(paginacao);

		// VERIFICAR
		assertEquals(1, resposta.paginaAtual());
		assertEquals(2, resposta.tamanho());
		assertEquals(2, resposta.quantidadeElementos());
		assertEquals(5L, resposta.totalElementos());
		assertEquals(3, resposta.totalPaginas());
		assertTrue(resposta.primeiraPagina());
		assertFalse(resposta.ultimaPagina());
		assertEquals(2, resposta.conteudo().size());

		UsuarioResponse user1 = resposta.conteudo().get(0);

		assertEquals(idUser1, user1.id());
		assertEquals("Administrador Teste", user1.nome());
		assertEquals(PerfilUsuario.ADMINISTRADOR, user1.perfil());

		UsuarioResponse user2 = resposta.conteudo().get(1);

		assertEquals(idUser2, user2.id());
		assertEquals("Funcionario Teste", user2.nome());
		assertEquals(PerfilUsuario.FUNCIONARIO, user2.perfil());

		verifyNoInteractions(passwordEncoder);

		verify(usuarioRepository).findAll(paginacao);

	}

	@Test
	void deveLancarExcecaoQuandoPaginaSolicitadaNaoExistir() {

		// PREPARAR
		Pageable paginacao = PageRequest.of(3, 2);

		Page<Usuario> paginaVazia = new PageImpl<>(List.of(), paginacao, 5);

		// MOCKS
		when(usuarioRepository.findAll(paginacao)).thenReturn(paginaVazia);

		PaginaInvalidaException excecao = assertThrows(PaginaInvalidaException.class,
				() -> usuarioService.listarPaginado(paginacao));

		assertEquals("A página informada não existe", excecao.getMessage());

		verify(usuarioRepository).findAll(paginacao);

		verifyNoInteractions(passwordEncoder);

	}

	@Test
	void deveAtualizarUsuarioQuandoDadosForemValidos() {

		// PREPARAR
		Long idUser1 = 1L;

		Usuario usuario = new Usuario("Maria Souza", "maria@empresa.com", "hash-original", PerfilUsuario.ADMINISTRADOR);
		ReflectionTestUtils.setField(usuario, "id", idUser1);

		AtualizarUsuarioRequest usuarioAtualizado = new AtualizarUsuarioRequest(" Maria Atualizada ",
				" MARIA.NOVA@Empresa.com ", PerfilUsuario.FUNCIONARIO);

		// MOCKS
		when(usuarioRepository.findById(idUser1)).thenReturn(Optional.of(usuario));
		when(usuarioRepository.existsByEmailAndIdNot("maria.nova@empresa.com", idUser1)).thenReturn(false);

		// EXECUTAR
		UsuarioResponse resposta = usuarioService.atualizar(idUser1, usuarioAtualizado);

		// VERIFICAR
		verify(usuarioRepository).findById(idUser1);
		verify(usuarioRepository).existsByEmailAndIdNot("maria.nova@empresa.com", idUser1);

		assertNotNull(resposta);
		assertEquals(idUser1, resposta.id());
		assertEquals("Maria Atualizada", resposta.nome());
		assertEquals("maria.nova@empresa.com", resposta.email());
		assertEquals(PerfilUsuario.FUNCIONARIO, resposta.perfil());

		verifyNoMoreInteractions(usuarioRepository);

	}

	@Test
	void deveLancarExcecaoQuandoUsuarioNaoExistir() {
		// PREPARAR
		Long idInexistente = 99L;
		AtualizarUsuarioRequest request = new AtualizarUsuarioRequest("Maria", "maria@email.com",
				PerfilUsuario.FUNCIONARIO);

		when(usuarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

		// EXECUTAR & VERIFICAR
		assertThrows(UsuarioNaoEncontradoException.class, () -> usuarioService.atualizar(idInexistente, request));

		verify(usuarioRepository).findById(idInexistente);
		verifyNoMoreInteractions(usuarioRepository);
	}

	@Test
	void deveLancarExcecaoQuandoEmailPertencerAOutroUsuario() {
		// PREPARAR
		Long idUser = 1L;
		Usuario usuario = new Usuario("Maria", "maria@email.com", "hash", PerfilUsuario.ADMINISTRADOR);
		AtualizarUsuarioRequest request = new AtualizarUsuarioRequest("Maria", "existente@email.com",
				PerfilUsuario.ADMINISTRADOR);
		// CORREÇÃO AQUI: Definir o ID na entidade usuario
		ReflectionTestUtils.setField(usuario, "id", idUser);

		when(usuarioRepository.findById(idUser)).thenReturn(Optional.of(usuario));
		when(usuarioRepository.existsByEmailAndIdNot("existente@email.com", idUser)).thenReturn(true);

		// EXECUTAR & VERIFICAR
		assertThrows(EmailJaCadastradoException.class, () -> usuarioService.atualizar(idUser, request));

		verify(usuarioRepository).findById(idUser);
		verify(usuarioRepository).existsByEmailAndIdNot("existente@email.com", idUser);
	}

	@Test
	void deveAtivarUsuarioQuandoEstiverInativo() {
		// PREPARAR
		Long idUser = 1L;
		Usuario usuario = new Usuario("Maria", "maria@email.com", "hash", PerfilUsuario.ADMINISTRADOR);
		ReflectionTestUtils.setField(usuario, "id", idUser);
		usuario.desativar();

		// MOCKS
		when(usuarioRepository.findById(idUser)).thenReturn(Optional.of(usuario));

		// EXECUTAR
		UsuarioResponse resposta = usuarioService.ativar(idUser);

		// VERIFICAR
		verify(usuarioRepository).findById(idUser);

		assertTrue(usuario.isAtivo());
		assertTrue(resposta.ativo());
		assertEquals(idUser, resposta.id());

	}

	@Test
	void deveLancarExcecaoQuandoUsuarioJaEstiverAtivo() {
		// PREPARAR
		Long idUser = 1L;
		Usuario usuario = new Usuario("Maria", "maria@email.com", "hash", PerfilUsuario.ADMINISTRADOR);
		ReflectionTestUtils.setField(usuario, "id", idUser);

		// MOCKS
		when(usuarioRepository.findById(idUser)).thenReturn(Optional.of(usuario));

		// EXECUTAR
		UsuarioJaAtivoException excecao = assertThrows(UsuarioJaAtivoException.class,
				() -> usuarioService.ativar(idUser));

		// VERIFICAR
		assertEquals("O usuário já está ativo.", excecao.getMessage());

		verify(usuarioRepository).findById(idUser);

		assertTrue(usuario.isAtivo());

	}

	@Test
	void deveLancarExcecaoQuandoUsuarioJaEstiverInativo() {
		// PREPARAR
		Long idUser = 1L;
		Long idAdmin = 2L;

		Usuario usuario = new Usuario("Maria", "maria@email.com", "hash", PerfilUsuario.ADMINISTRADOR);
		ReflectionTestUtils.setField(usuario, "id", idUser);
		usuario.desativar();

		// MOCKS
		when(usuarioRepository.findById(idUser)).thenReturn(Optional.of(usuario));

		// EXECUTAR
		UsuarioJaInativoException excecao = assertThrows(UsuarioJaInativoException.class,
				() -> usuarioService.desativar(idUser, idAdmin));

		// VERIFICAR
		assertEquals("O usuário já está inativo.", excecao.getMessage());

		verify(usuarioRepository).findById(idUser);

		assertFalse(usuario.isAtivo());

	}

	@Test
	void deveDesativarUsuarioComSucesso() {
		// PREPARAR
		Long idUser = 1L;
		Long idAdminLogado = 2L;

		Usuario usuario = new Usuario("Maria", "maria@email.com", "hash", PerfilUsuario.ADMINISTRADOR);
		ReflectionTestUtils.setField(usuario, "id", idUser);

		// MOCKS
		when(usuarioRepository.findById(idUser)).thenReturn(Optional.of(usuario));

		// EXECUTAR
		UsuarioResponse resposta = usuarioService.desativar(idUser, idAdminLogado);

		// VERIFICAR
		assertNotNull(resposta);
		assertEquals(idUser, resposta.id());
		assertFalse(resposta.ativo());
		assertFalse(usuario.isAtivo());

		verify(usuarioRepository).findById(idUser);
		verifyNoMoreInteractions(usuarioRepository);
	}

	@Test
	void deveLancarExcecaoQuandoUsuarioTentarDesativarASiMesmo() {
		// PREPARAR
		Long idUser = 1L;
		Long idAdminLogado = 1L; // Mesmo ID!

		Usuario usuario = new Usuario("Maria", "maria@email.com", "hash", PerfilUsuario.ADMINISTRADOR);
		ReflectionTestUtils.setField(usuario, "id", idUser);

		// MOCKS
		when(usuarioRepository.findById(idUser)).thenReturn(Optional.of(usuario));

		// EXECUTAR
		AutodesativacaoNaoPermitidaException excecao = assertThrows(AutodesativacaoNaoPermitidaException.class,
				() -> usuarioService.desativar(idUser, idAdminLogado));

		// VERIFICAR
		assertEquals("Não é permitido desativar a própria conta.", excecao.getMessage());

		verify(usuarioRepository).findById(idUser);
		assertTrue(usuario.isAtivo()); // Permanece ativo
		verifyNoMoreInteractions(usuarioRepository);
	}

	@Test
	void deveAlterarPropriaSenhaQuandoDadosForemValidos() {
		// PREPARAR
		Long idUser = 1L;
		Usuario usuario = new Usuario("Maria", "maria@email.com", "hash", PerfilUsuario.ADMINISTRADOR);
		ReflectionTestUtils.setField(usuario, "id", idUser);
		String senhaAtual = "senha-atual-correta";
		String novaSenha = "nova-senha-segura";
		String confirmaNovaSenha = "nova-senha-segura";

		AlterarPropriaSenhaRequest novaSenhaAtualizada = new AlterarPropriaSenhaRequest(senhaAtual, novaSenha,
				confirmaNovaSenha);

		// MOCKS
		when(usuarioRepository.findById(idUser)).thenReturn(Optional.of(usuario));

		when(passwordEncoder.matches(senhaAtual, "hash")).thenReturn(true);

		when(passwordEncoder.matches(novaSenha, "hash")).thenReturn(false);

		when(passwordEncoder.encode(novaSenha)).thenReturn("novo-hash");

		// EXECUTAR
		usuarioService.alterarPropriaSenha(idUser, novaSenhaAtualizada);

		// VERIFICAR
		verify(usuarioRepository).findById(idUser);

		verify(passwordEncoder).matches(senhaAtual, "hash");

		verify(passwordEncoder).matches(novaSenha, "hash");

		verify(passwordEncoder).encode(novaSenha);

		assertEquals("novo-hash", usuario.getSenhaHash());

	}

	@Test
	void deveLancarExcecaoQuandoSenhaAtualEstiverIncorreta() {

		// PREPARAR
		Long usuarioId = 1L;
		Usuario usuarioEncontradoNoBanco = new Usuario("Maria", "maria@email.com", "hash-atual",
				PerfilUsuario.ADMINISTRADOR);
		ReflectionTestUtils.setField(usuarioEncontradoNoBanco, "id", usuarioId);

		String senhaAtualIncorreta = "senha-atual-errada";
		String novaSenha = "nova-senha-segura";
		String confirmacaoNovaSenha = "nova-senha-segura";

		AlterarPropriaSenhaRequest novaSenhaAtualizada = new AlterarPropriaSenhaRequest(senhaAtualIncorreta, novaSenha,
				confirmacaoNovaSenha);

		// MOCKS
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioEncontradoNoBanco));

		when(passwordEncoder.matches(senhaAtualIncorreta, "hash-atual")).thenReturn(false);

		SenhaAtualIncorretaException excecao = assertThrows(SenhaAtualIncorretaException.class,
				() -> usuarioService.alterarPropriaSenha(usuarioId, novaSenhaAtualizada));

		// VERIFICA
		assertEquals("A senha atual está incorreta.", excecao.getMessage());
		verify(usuarioRepository).findById(usuarioId);
		verify(passwordEncoder).matches(senhaAtualIncorreta, "hash-atual");
		verify(passwordEncoder, never()).encode(anyString());
		assertEquals("hash-atual", usuarioEncontradoNoBanco.getSenhaHash());

	}

	@Test
	void deveLancarExcecaoQuandoConfirmacaoNovaSenhaForDiferente() {

		// PREPARAR
		Long usuarioId = 1L;
		Usuario usuarioEncontradoNoBanco = new Usuario("Maria", "maria@email.com", "hash-atual",
				PerfilUsuario.ADMINISTRADOR);
		ReflectionTestUtils.setField(usuarioEncontradoNoBanco, "id", usuarioId);

		String senhaAtual = "senha-atual-correta";
		String novaSenha = "nova-senha-segura";
		String confirmacaoNovaSenha = "outra-senha-diferente";

		AlterarPropriaSenhaRequest novaSenhaAtualizada = new AlterarPropriaSenhaRequest(senhaAtual, novaSenha,
				confirmacaoNovaSenha);

		// MOCKS
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioEncontradoNoBanco));
		when(passwordEncoder.matches(senhaAtual, "hash-atual")).thenReturn(true);

		ConfirmacaoSenhaInvalidaException excecao = assertThrows(ConfirmacaoSenhaInvalidaException.class,
				() -> usuarioService.alterarPropriaSenha(usuarioId, novaSenhaAtualizada));

		// VERIFICAR
		assertEquals("A confirmação da nova senha não corresponde à nova senha.", excecao.getMessage());
		verify(usuarioRepository).findById(usuarioId);
		verify(passwordEncoder).matches(senhaAtual, "hash-atual");
		verify(passwordEncoder, never()).encode(anyString());
		assertEquals("hash-atual", usuarioEncontradoNoBanco.getSenhaHash());
	}

	@Test
	void deveLancarExcecaoQuandoNovaSenhaForIgualAtual() {

		// PREPARAR
		Long usuarioId = 1L;
		Usuario usuarioEncontradoNoBanco = new Usuario("Maria", "maria@email.com", "hash-atual",
				PerfilUsuario.ADMINISTRADOR);
		ReflectionTestUtils.setField(usuarioEncontradoNoBanco, "id", usuarioId);

		String senhaAtual = "senha-atual-correta";
		String novaSenha = "senha-atual-correta";
		String confirmacaoNovaSenha = "senha-atual-correta";

		AlterarPropriaSenhaRequest novaSenhaAtualizada = new AlterarPropriaSenhaRequest(senhaAtual, novaSenha,
				confirmacaoNovaSenha);

		// MOCKS
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioEncontradoNoBanco));
		when(passwordEncoder.matches(senhaAtual, "hash-atual")).thenReturn(true);

		// EXECUTAR
		NovaSenhaIgualAtualException excecao = assertThrows(NovaSenhaIgualAtualException.class,
				() -> usuarioService.alterarPropriaSenha(usuarioId, novaSenhaAtualizada));

		// VERIFICAR
		assertEquals("A nova senha deve ser diferente da senha atual.", excecao.getMessage());
		verify(usuarioRepository).findById(usuarioId);
		verify(passwordEncoder, times(2)).matches(senhaAtual, "hash-atual");
		verify(passwordEncoder, never()).encode(anyString());
		assertEquals("hash-atual", usuarioEncontradoNoBanco.getSenhaHash());

	}

	@Test
	void deveRedefinirSenhaDeOutroUsuarioQuandoDadosForemValidos() {

		// PREPARAR
		Long usuarioId = 2L;
		Long administradorAutenticadoId = 1L;

		Usuario usuarioEncontradoNoBanco = new Usuario("Maria", "maria@email.com", "hash-atual",
				PerfilUsuario.FUNCIONARIO);

		ReflectionTestUtils.setField(usuarioEncontradoNoBanco, "id", usuarioId);

		String novaSenha = "nova-senha-segura";

		RedefinirSenhaUsuarioRequest request = new RedefinirSenhaUsuarioRequest(novaSenha, novaSenha);

		// MOCKS
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioEncontradoNoBanco));

		when(passwordEncoder.encode(novaSenha)).thenReturn("novo-hash");

		// EXECUTAR
		usuarioService.redefinirSenha(usuarioId, administradorAutenticadoId, request);

		// VERIFICAR
		verify(usuarioRepository).findById(usuarioId);

		verify(passwordEncoder).encode(novaSenha);

		verify(passwordEncoder, never()).matches(anyString(), anyString());

		assertEquals("novo-hash", usuarioEncontradoNoBanco.getSenhaHash());
	}

	@Test
	void deveLancarExcecaoQuandoAdministradorTentarRedefinirPropriaSenha() {

		// PREPARAR
		Long administradorAutenticadoId = 1L;

		Usuario administradorEncontradoNoBanco = new Usuario("Administrador", "administrador@email.com", "hash-atual",
				PerfilUsuario.ADMINISTRADOR);

		ReflectionTestUtils.setField(administradorEncontradoNoBanco, "id", administradorAutenticadoId);

		RedefinirSenhaUsuarioRequest request = new RedefinirSenhaUsuarioRequest("nova-senha-segura",
				"nova-senha-segura");

		// MOCKS
		when(usuarioRepository.findById(administradorAutenticadoId))
				.thenReturn(Optional.of(administradorEncontradoNoBanco));

		// EXECUTAR
		RedefinicaoPropriaSenhaNaoPermitidaException excecao = assertThrows(
				RedefinicaoPropriaSenhaNaoPermitidaException.class,
				() -> usuarioService.redefinirSenha(administradorAutenticadoId, administradorAutenticadoId, request));

		// VERIFICAR
		assertEquals("Não é permitido redefinir a própria senha por esta operação.", excecao.getMessage());

		verify(usuarioRepository).findById(administradorAutenticadoId);

		verify(passwordEncoder, never()).encode(anyString());

		verify(passwordEncoder, never()).matches(anyString(), anyString());

		assertEquals("hash-atual", administradorEncontradoNoBanco.getSenhaHash());
	}

	@Test
	void deveLancarExcecaoQuandoConfirmacaoDaRedefinicaoForDiferente() {

		// PREPARAR
		Long usuarioId = 2L;
		Long administradorAutenticadoId = 1L;

		Usuario usuarioEncontradoNoBanco = new Usuario("Maria", "maria@email.com", "hash-atual",
				PerfilUsuario.FUNCIONARIO);

		ReflectionTestUtils.setField(usuarioEncontradoNoBanco, "id", usuarioId);

		RedefinirSenhaUsuarioRequest request = new RedefinirSenhaUsuarioRequest("nova-senha-segura",
				"outra-senha-diferente");

		// MOCKS
		when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioEncontradoNoBanco));

		// EXECUTAR
		ConfirmacaoSenhaInvalidaException excecao = assertThrows(ConfirmacaoSenhaInvalidaException.class,
				() -> usuarioService.redefinirSenha(usuarioId, administradorAutenticadoId, request));

		// VERIFICAR
		assertEquals("A confirmação da nova senha não corresponde à nova senha.", excecao.getMessage());

		verify(usuarioRepository).findById(usuarioId);

		verify(passwordEncoder, never()).encode(anyString());

		verify(passwordEncoder, never()).matches(anyString(), anyString());

		assertEquals("hash-atual", usuarioEncontradoNoBanco.getSenhaHash());
	}

}