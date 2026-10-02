package br.com.projetosecsr.aluguelequipamentos.cliente.integracao;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ConsultaCepIndisponivelException;

public class ViaCepClientTest {

	@Test
	void deveLancarExcecaoQuandoConsultaAoViaCepFalhar() {

		// PREPARAR
		RestClient.Builder builder = RestClient.builder();

		MockRestServiceServer mockServer = MockRestServiceServer.bindTo(builder).build();

		ViaCepClient viaCepClient = new ViaCepClient(builder);

		mockServer.expect(once(), requestTo("https://viacep.com.br/ws/88330000/json/")).andRespond(withServerError());

		// EXECUTAR
		ConsultaCepIndisponivelException excecao = assertThrows(ConsultaCepIndisponivelException.class,
				() -> viaCepClient.consultar("88330000"));

		// VERIFICAR
		assertEquals("Serviço de consulta de CEP indisponível.", excecao.getMessage());

		mockServer.verify();
	}
}