package br.com.projetosecsr.aluguelequipamentos.cliente.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.CepNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.integracao.ViaCepClient;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.EnderecoCepResponse;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.ViaCepResponse;

@ExtendWith(MockitoExtension.class)
public class ConsultaCepServiceTest {

	@Mock
	private ViaCepClient viaCepClient;

	@InjectMocks
	private ConsultaCepService consultaCepService;

	@Test
	void deveRetornarEnderecoQuandoCepForEncontrado() {

		// PREPARAR
		String cep = "88330000";

		ViaCepResponse respostaViaCep = new ViaCepResponse("88330-000", "Rua das Flores", "", "Centro",
				"Balneário Camboriú", "SC", false);

		// MOCKS
		when(viaCepClient.consultar(cep)).thenReturn(respostaViaCep);

		// EXECUTAR
		EnderecoCepResponse resposta = consultaCepService.consultar(cep);

		// VERIFICAR
		assertEquals("88330-000", resposta.cep());
		assertEquals("Rua das Flores", resposta.logradouro());
		assertEquals("", resposta.complemento());
		assertEquals("Centro", resposta.bairro());
		assertEquals("Balneário Camboriú", resposta.cidade());
		assertEquals("SC", resposta.estado());

		verify(viaCepClient).consultar(cep);
	}

	@Test
	void deveLancarExcecaoQuandoCepNaoForEncontrado() {

		// PREPARAR
		String cep = "00000000";

		ViaCepResponse respostaViaCep = new ViaCepResponse(null, null, null, null, null, null, true);

		// MOCKS
		when(viaCepClient.consultar(cep)).thenReturn(respostaViaCep);

		// EXECUTAR
		CepNaoEncontradoException excecao = assertThrows(CepNaoEncontradoException.class,
				() -> consultaCepService.consultar(cep));

		// VERIFICAR
		assertEquals("CEP não encontrado.", excecao.getMessage());

		verify(viaCepClient).consultar(cep);
	}

}
