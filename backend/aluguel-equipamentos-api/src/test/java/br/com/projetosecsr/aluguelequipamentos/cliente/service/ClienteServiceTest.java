package br.com.projetosecsr.aluguelequipamentos.cliente.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.Cliente;
import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.TipoCliente;
import br.com.projetosecsr.aluguelequipamentos.cliente.repository.ClienteRepository;
import br.com.projetosecsr.aluguelequipamentos.cliente.request.CadastrarClienteRequest;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.ClienteResponse;
import br.com.projetosecsr.aluguelequipamentos.cliente.validacao.ValidadorCliente;

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

}
