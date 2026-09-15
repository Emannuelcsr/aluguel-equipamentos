package br.com.projetosecsr.aluguelequipamentos.usuario.inicializacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.repository.UsuarioRepository;
import br.com.projetosecsr.aluguelequipamentos.usuario.request.CadastrarUsuarioRequest;
import br.com.projetosecsr.aluguelequipamentos.usuario.service.UsuarioService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@ExtendWith(MockitoExtension.class)
public class InicializadorAdministradorTest {

	@Mock
	private UsuarioService usuarioService;

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private Validator validator;

	@Mock
	private ConstraintViolation<CadastrarUsuarioRequest> violacao;

	private InicializadorAdministrador inicializador;

	@Test
	void deveCadastrarAdministradorQuandoEmailNaoExistir() {

		when(validator.validate(any(CadastrarUsuarioRequest.class))).thenReturn(Set.of());

		when(usuarioRepository.findByEmail("admin@empresa.com")).thenReturn(Optional.empty());

		inicializador.run(null);

		ArgumentCaptor<CadastrarUsuarioRequest> requestCaptor = ArgumentCaptor.forClass(CadastrarUsuarioRequest.class);

		verify(usuarioRepository).findByEmail("admin@empresa.com");
		verify(usuarioService).cadastrar(requestCaptor.capture());

		CadastrarUsuarioRequest requestEnviado = requestCaptor.getValue();

		assertEquals(PerfilUsuario.ADMINISTRADOR, requestEnviado.perfil());
	}

	@Test
	void naoDeveCadastrarNovamenteQuandoAdministradorJaExistir() {

		when(validator.validate(any(CadastrarUsuarioRequest.class))).thenReturn(Set.of());

		Usuario administradorExistente = new Usuario("Administrador", "admin@empresa.com", "hash-simulado",
				PerfilUsuario.ADMINISTRADOR);

		when(usuarioRepository.findByEmail("admin@empresa.com")).thenReturn(Optional.of(administradorExistente));

		inicializador.run(null);

		verify(usuarioRepository).findByEmail("admin@empresa.com");
		verify(usuarioService, never()).cadastrar(any(CadastrarUsuarioRequest.class));
	}

	@Test
	void deveFalharQuandoEmailPertencerAUmFuncionario() {

		when(validator.validate(any(CadastrarUsuarioRequest.class))).thenReturn(Set.of());

		Usuario funcionarioExistente = new Usuario("Funcionário", "admin@empresa.com", "hash-simulado",
				PerfilUsuario.FUNCIONARIO);

		when(usuarioRepository.findByEmail("admin@empresa.com")).thenReturn(Optional.of(funcionarioExistente));

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> inicializador.run(null));

		assertEquals("O e-mail do administrador inicial pertence a um funcionário.", exception.getMessage());

		verify(usuarioRepository).findByEmail("admin@empresa.com");

		verify(usuarioService, never()).cadastrar(any(CadastrarUsuarioRequest.class));
	}

	@Test
	void deveFalharQuandoConfiguracaoForInvalida() {

		inicializador = new InicializadorAdministrador(usuarioService, usuarioRepository, validator, "Administrador",
				"admin@empresa.com", "curta");

		when(validator.validate(any(CadastrarUsuarioRequest.class))).thenReturn(Set.of(violacao));

		when(violacao.getMessage()).thenReturn("A senha deve ter entre 15 e 64 caracteres.");

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> inicializador.run(null));

		assertEquals("Configuração do administrador inicial inválida: " + "A senha deve ter entre 15 e 64 caracteres.",
				exception.getMessage());

		verifyNoInteractions(usuarioRepository, usuarioService);
	}

	@BeforeEach
	void preparar() {

		inicializador = new InicializadorAdministrador(usuarioService, usuarioRepository, validator, "Administrador",
				" ADMIN@EMPRESA.COM ", "uma-senha-segura-123");
	}

}
