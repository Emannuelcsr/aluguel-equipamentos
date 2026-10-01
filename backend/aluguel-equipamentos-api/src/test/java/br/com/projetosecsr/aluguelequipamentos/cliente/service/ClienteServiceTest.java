package br.com.projetosecsr.aluguelequipamentos.cliente.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
import org.springframework.test.util.ReflectionTestUtils;

import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.Cliente;
import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.TipoCliente;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ClienteJaAtivoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ClienteJaInativoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ClienteNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.DocumentoJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.EmailClienteJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.EstadoInvalidoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.NomeFantasiaNaoPermitidoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.repository.ClienteRepository;
import br.com.projetosecsr.aluguelequipamentos.cliente.request.AtualizarClienteRequest;
import br.com.projetosecsr.aluguelequipamentos.cliente.request.CadastrarClienteRequest;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.ClienteResponse;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.ClienteResumoResponse;
import br.com.projetosecsr.aluguelequipamentos.cliente.validacao.ValidadorCliente;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.UsuarioJaInativoException;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {

	@Mock
	private ClienteRepository clienteRepository;

	@Mock
	private ValidadorCliente validadorCliente;

	@InjectMocks
	private ClienteService clienteService;

	@Test
	void deveCadastrarCliente() {

		// PREPARAR
		CadastrarClienteRequest request = new CadastrarClienteRequest(TipoCliente.PF, "João da Silva", null,
				"52998224725", "joao@email.com", "47999999999", "88330000", "Rua das Flores", "123", "Apto 202",
				"Centro", "Balneário Camboriú", "SC");

		when(clienteRepository.existsByDocumento("52998224725")).thenReturn(false);
		when(clienteRepository.existsByEmail("joao@email.com")).thenReturn(false);
		when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

		// EXECUTAR
		ClienteResponse resposta = clienteService.cadastrar(request);

		// VERIFICAR
		verify(clienteRepository).existsByEmail("joao@email.com");
		verify(clienteRepository).existsByDocumento("52998224725");

		ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);

		verify(clienteRepository).save(clienteCaptor.capture());

		Cliente clienteEnviadoParaSalvar = clienteCaptor.getValue();

		assertEquals("João da Silva", clienteEnviadoParaSalvar.getNomeRazaoSocial());
		assertEquals("joao@email.com", clienteEnviadoParaSalvar.getEmail());
		assertEquals(TipoCliente.PF, clienteEnviadoParaSalvar.getTipo());
		assertTrue(clienteEnviadoParaSalvar.isAtivo());

		assertEquals("João da Silva", resposta.nomeRazaoSocial());
		assertEquals("joao@email.com", resposta.email());
		assertEquals(TipoCliente.PF, resposta.tipo());
		assertTrue(resposta.ativo());

	}

	@Test
	void deveLancarExcecaoQuandoDocumentoJaEstiverCadastrado() {

		// PREPARAR
		CadastrarClienteRequest request = new CadastrarClienteRequest(TipoCliente.PF, "João da Silva", null,
				"52998224725", "joao@email.com", "47999999999", "88330000", "Rua das Flores", "123", "Apto 202",
				"Centro", "Balneário Camboriú", "SC");

		when(clienteRepository.existsByDocumento("52998224725")).thenReturn(true);

		// EXECUTAR
		DocumentoJaCadastradoException excecao = assertThrows(DocumentoJaCadastradoException.class,
				() -> clienteService.cadastrar(request));

		// VERIFICAR
		assertEquals("Documento já cadastrado.", excecao.getMessage());

		verify(clienteRepository).existsByDocumento("52998224725");

		verify(clienteRepository, never()).existsByEmail(anyString());

		verify(clienteRepository, never()).save(any(Cliente.class));

	}

	@Test
	void deveLancarExcecaoQuandoEmailJaEstiverCadastrado() {

		// PREPARAR
		CadastrarClienteRequest request = new CadastrarClienteRequest(TipoCliente.PF, "João da Silva", null,
				"52998224725", "joao@email.com", "47999999999", "88330000", "Rua das Flores", "123", "Apto 202",
				"Centro", "Balneário Camboriú", "SC");

		when(clienteRepository.existsByDocumento("52998224725")).thenReturn(false);
		when(clienteRepository.existsByEmail("joao@email.com")).thenReturn(true);

		// EXECUTAR
		EmailClienteJaCadastradoException excecao = assertThrows(EmailClienteJaCadastradoException.class,
				() -> clienteService.cadastrar(request));

		// VERIFICAR
		assertEquals("E-mail já cadastrado para outro cliente.", excecao.getMessage());

		verify(clienteRepository).existsByDocumento("52998224725");

		verify(clienteRepository).existsByEmail("joao@email.com");

		verify(clienteRepository, never()).save(any(Cliente.class));

		verifyNoMoreInteractions(clienteRepository);

	}

	@Test
	void deveBuscarClientePorIdQuandoClienteExistir() {

		// PREPARAR
		Long clienteId = 1l;
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");
		ReflectionTestUtils.setField(cliente, "id", clienteId);

		// MOCKS
		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

		// EXECUTAR
		ClienteResponse resposta = clienteService.buscarPorId(clienteId);

		// VERIFICAR
		verify(clienteRepository).findById(clienteId);

		assertEquals(clienteId, resposta.id());
		assertEquals("João da Silva", resposta.nomeRazaoSocial());
		assertEquals("joao@email.com", resposta.email());
		assertEquals(TipoCliente.PF, resposta.tipo());
		assertTrue(resposta.ativo());
	}

	@Test
	void deveLancarExcecaoQuandoClienteNaoExistir() {

		// PREPARAR
		Long clienteId = 1l;

		// MOCKS
		when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

		ClienteNaoEncontradoException excecao = assertThrows(ClienteNaoEncontradoException.class,
				() -> clienteService.buscarPorId(clienteId));

		// VERIFICAR

		verify(clienteRepository).findById(clienteId);
		assertEquals("Cliente não encontrado.", excecao.getMessage());

		verifyNoMoreInteractions(clienteRepository);

	}

	@Test
	void deveListarClientesComPaginacao() {

		// PREPARAR
		Long idCliente1 = 1L;
		Long idCliente2 = 2L;

		Pageable paginacao = PageRequest.of(0, 2);

		Cliente cliente1 = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");
		ReflectionTestUtils.setField(cliente1, "id", idCliente1);

		Cliente cliente2 = new Cliente(TipoCliente.PF, "Joãozin da Silva", null, "52998224715", "joaoa@email.com",
				"47999999s99", "883300f0", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");
		ReflectionTestUtils.setField(cliente2, "id", idCliente2);

		Page<Cliente> paginaDeClientes = new PageImpl<>(List.of(cliente1, cliente2), paginacao, 5);

		// MOCKS
		when(clienteRepository.findAll(paginacao)).thenReturn(paginaDeClientes);

		// EXECUTAR
		PaginaResponse<ClienteResumoResponse> resposta = clienteService.listarPaginado(paginacao);

		// VERIFICAR
		assertEquals(1, resposta.paginaAtual());
		assertEquals(2, resposta.tamanho());
		assertEquals(2, resposta.quantidadeElementos());
		assertEquals(5L, resposta.totalElementos());
		assertEquals(3, resposta.totalPaginas());
		assertTrue(resposta.primeiraPagina());
		assertFalse(resposta.ultimaPagina());
		assertEquals(2, resposta.conteudo().size());

		ClienteResumoResponse user1 = resposta.conteudo().get(0);

		assertEquals(idCliente1, user1.id());
		assertEquals("João da Silva", user1.nomeRazaoSocial());
		assertEquals(TipoCliente.PF, user1.tipo());

		ClienteResumoResponse user2 = resposta.conteudo().get(1);

		assertEquals(idCliente2, user2.id());
		assertEquals("Joãozin da Silva", user2.nomeRazaoSocial());
		assertEquals(TipoCliente.PF, user1.tipo());

		verify(clienteRepository).findAll(paginacao);

	}

	@Test
	void deveLancarExcecaoQuandoPaginaSolicitadaNaoExistir() {

		// PREPARAR
		Pageable paginacao = PageRequest.of(3, 2);

		Page<Cliente> paginaVazia = new PageImpl<>(List.of(), paginacao, 5);

		// MOCKS
		when(clienteRepository.findAll(paginacao)).thenReturn(paginaVazia);

		PaginaInvalidaException excecao = assertThrows(PaginaInvalidaException.class,
				() -> clienteService.listarPaginado(paginacao));

		assertEquals("A página informada não existe", excecao.getMessage());

		verify(clienteRepository).findAll(paginacao);

	}

	@Test
	void deveAtualizarClienteQuandoDadosForemValidos() {

		Long idCliente1 = 1L;
		Cliente cliente1 = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");
		ReflectionTestUtils.setField(cliente1, "id", idCliente1);

		AtualizarClienteRequest request = new AtualizarClienteRequest("João da Silva Atualizado", null,
				"joao.atualizado@email.com", "47988887777", "88330000", "Rua Nova", "456", "Casa 2", "Centro",
				"Balneário Camboriú", "SC");

		when(clienteRepository.findById(idCliente1)).thenReturn(Optional.of(cliente1));
		when(clienteRepository.existsByEmailAndIdNot("joao.atualizado@email.com", idCliente1)).thenReturn(false);

		// EXECUTAR
		ClienteResponse resposta = clienteService.atualizar(request, idCliente1);

		// VERIFICAR
		assertNotNull(resposta);
		assertEquals(idCliente1, resposta.id());
		assertEquals("João da Silva Atualizado", resposta.nomeRazaoSocial());
		assertEquals("joao.atualizado@email.com", resposta.email());
		assertEquals("47988887777", resposta.telefone());
		assertEquals("88330000", resposta.cep());
		assertEquals("Rua Nova", resposta.logradouro());
		assertEquals("456", resposta.numero());
		assertEquals("Casa 2", resposta.complemento());
		assertEquals("Centro", resposta.bairro());
		assertEquals("Balneário Camboriú", resposta.cidade());
		assertEquals("SC", resposta.estado());
		assertEquals(TipoCliente.PF, resposta.tipo());
		assertEquals("52998224725", resposta.documento());
		assertTrue(resposta.ativo());
		verify(clienteRepository).findById(idCliente1);

		verify(clienteRepository).existsByEmailAndIdNot("joao.atualizado@email.com", idCliente1);

		verify(validadorCliente).validarNomeFantasia(TipoCliente.PF, null);

		verify(validadorCliente).validarEstado("SC");

		verifyNoMoreInteractions(clienteRepository);

	}

	@Test
	void deveLancarExcecaoQuandoClienteNaoExistirAoAtualizar() {
		// PREPARAR
		Long idInexistente = 99L;
		AtualizarClienteRequest request = new AtualizarClienteRequest("João da Silva Atualizado", null,
				"joao.atualizado@email.com", "47988887777", "88330000", "Rua Nova", "456", "Casa 2", "Centro",
				"Balneário Camboriú", "SC");

		when(clienteRepository.findById(idInexistente)).thenReturn(Optional.empty());

		// EXECUTAR & VERIFICAR
		ClienteNaoEncontradoException excecao = assertThrows(ClienteNaoEncontradoException.class,
				() -> clienteService.atualizar(request, idInexistente));
		assertEquals("Cliente não encontrado.", excecao.getMessage());

		verify(clienteRepository).findById(idInexistente);
		verifyNoMoreInteractions(clienteRepository);
		verifyNoInteractions(validadorCliente);
	}

	@Test
	void deveLancarExcecaoQuandoEmailJaPertencerAOutroClienteAoAtualizar() {

		// PREPARAR
		Long clienteId = 1L;

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		ReflectionTestUtils.setField(cliente, "id", clienteId);

		AtualizarClienteRequest request = new AtualizarClienteRequest("João da Silva Atualizado", null,
				"email.ja.usado@email.com", "47988887777", "88330000", "Rua Nova", "456", "Casa 2", "Centro",
				"Balneário Camboriú", "SC");

		// MOCKS
		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

		when(clienteRepository.existsByEmailAndIdNot("email.ja.usado@email.com", clienteId)).thenReturn(true);

		// EXECUTAR
		EmailClienteJaCadastradoException excecao = assertThrows(EmailClienteJaCadastradoException.class,
				() -> clienteService.atualizar(request, clienteId));

		// VERIFICAR
		assertEquals("E-mail já cadastrado para outro cliente.", excecao.getMessage());

		verify(clienteRepository).findById(clienteId);

		verify(clienteRepository).existsByEmailAndIdNot("email.ja.usado@email.com", clienteId);

		verify(validadorCliente).validarNomeFantasia(TipoCliente.PF, null);

		verify(validadorCliente).validarEstado("SC");

		assertEquals("João da Silva", cliente.getNomeRazaoSocial());
		assertEquals("joao@email.com", cliente.getEmail());

		verifyNoMoreInteractions(clienteRepository);
	}

	@Test
	void deveLancarExcecaoQuandoPfReceberNomeFantasiaAoAtualizar() {

		// PREPARAR
		Long clienteId = 1L;

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		ReflectionTestUtils.setField(cliente, "id", clienteId);

		AtualizarClienteRequest request = new AtualizarClienteRequest("João da Silva Atualizado",
				"Nome Fantasia Indevido", "joao.atualizado@email.com", "47988887777", "88330000", "Rua Nova", "456",
				"Casa 2", "Centro", "Balneário Camboriú", "SC");

		// MOCKS
		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

		doThrow(new NomeFantasiaNaoPermitidoException()).when(validadorCliente).validarNomeFantasia(TipoCliente.PF,
				"Nome Fantasia Indevido");

		// EXECUTAR
		NomeFantasiaNaoPermitidoException excecao = assertThrows(NomeFantasiaNaoPermitidoException.class,
				() -> clienteService.atualizar(request, clienteId));

		// VERIFICAR
		assertEquals("Nome fantasia não é permitido para cliente pessoa física.", excecao.getMessage());

		verify(clienteRepository).findById(clienteId);

		verify(validadorCliente).validarNomeFantasia(TipoCliente.PF, "Nome Fantasia Indevido");

		verify(clienteRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());

		verify(validadorCliente, never()).validarEstado(anyString());

		assertEquals("João da Silva", cliente.getNomeRazaoSocial());
		assertEquals("joao@email.com", cliente.getEmail());

		verifyNoMoreInteractions(clienteRepository);
	}

	@Test
	void deveLancarExcecaoQuandoEstadoForInvalidoAoAtualizar() {

		// PREPARAR
		Long clienteId = 1L;

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		ReflectionTestUtils.setField(cliente, "id", clienteId);

		AtualizarClienteRequest request = new AtualizarClienteRequest("João da Silva Atualizado", null,
				"joao.atualizado@email.com", "47988887777", "88330000", "Rua Nova", "456", "Casa 2", "Centro",
				"Balneário Camboriú", "ZZ");

		// MOCKS
		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

		doThrow(new EstadoInvalidoException()).when(validadorCliente).validarEstado("ZZ");

		// EXECUTAR
		EstadoInvalidoException excecao = assertThrows(EstadoInvalidoException.class,
				() -> clienteService.atualizar(request, clienteId));

		// VERIFICAR
		assertEquals("Estado inválido.", excecao.getMessage());

		verify(clienteRepository).findById(clienteId);

		verify(validadorCliente).validarNomeFantasia(TipoCliente.PF, null);

		verify(validadorCliente).validarEstado("ZZ");

		verify(clienteRepository, never()).existsByEmailAndIdNot(anyString(), anyLong());

		assertEquals("João da Silva", cliente.getNomeRazaoSocial());
		assertEquals("joao@email.com", cliente.getEmail());

		verifyNoMoreInteractions(clienteRepository);
	}

	@Test
	void deveAtivarClienteQuandoEstiverInativo() {
		// PREPARAR
		Long idCliente = 1L;
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");
		ReflectionTestUtils.setField(cliente, "id", idCliente);
		cliente.desativar();

		// MOCKS
		when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(cliente));

		// EXECUTAR
		ClienteResponse resposta = clienteService.ativar(idCliente);

		// VERIFICAR
		verify(clienteRepository).findById(idCliente);

		assertTrue(cliente.isAtivo());
		assertTrue(resposta.ativo());
		assertEquals(idCliente, resposta.id());

	}

	@Test
	void deveLancarExcecaoQuandoClienteJaEstiverAtivo() {
		// PREPARAR
		Long idCliente = 1L;
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");
		ReflectionTestUtils.setField(cliente, "id", idCliente);

		// MOCKS
		when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(cliente));

		// EXECUTAR
		ClienteJaAtivoException excecao = assertThrows(ClienteJaAtivoException.class,
				() -> clienteService.ativar(idCliente));

		// VERIFICAR
		assertEquals("Cliente já está ativo.", excecao.getMessage());

		verify(clienteRepository).findById(idCliente);

		assertTrue(cliente.isAtivo());

	}

	@Test
	void deveDesativarClienteComSucesso() {
		// PREPARAR
		Long idCliente = 1L;
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");
		ReflectionTestUtils.setField(cliente, "id", idCliente);

		// MOCKS
		when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(cliente));

		// EXECUTAR
		ClienteResponse resposta = clienteService.desativar(idCliente);

		// VERIFICAR
		assertNotNull(resposta);
		assertEquals(idCliente, resposta.id());
		assertFalse(resposta.ativo());
		assertFalse(cliente.isAtivo());

		verify(clienteRepository).findById(idCliente);
		verifyNoMoreInteractions(clienteRepository);
	}

	@Test
	void deveLancarExcecaoQuandoClienteJaEstiverInativo() {
		// PREPARAR
		Long idCliente = 1L;
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");
		ReflectionTestUtils.setField(cliente, "id", idCliente);
		cliente.desativar();

		// MOCKS
		when(clienteRepository.findById(idCliente)).thenReturn(Optional.of(cliente));

		// EXECUTAR
		ClienteJaInativoException excecao = assertThrows(ClienteJaInativoException.class,
				() -> clienteService.desativar(idCliente));

		// VERIFICAR
		assertEquals("Cliente já está inativo.", excecao.getMessage());

		verify(clienteRepository).findById(idCliente);

		assertFalse(cliente.isAtivo());

		verifyNoMoreInteractions(clienteRepository);
	}

}
