package br.com.projetosecsr.aluguelequipamentos.cliente.integracao;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ConsultaCepIndisponivelException;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.ViaCepResponse;

@Component
public class ViaCepClient {

	private final RestClient restClient;

	public ViaCepClient(RestClient.Builder builder) {
		this.restClient = builder.baseUrl("https://viacep.com.br").build();
	}

	public ViaCepResponse consultar(String cep) {

		try {

			return restClient.get().uri("/ws/{cep}/json/", cep).retrieve().body(ViaCepResponse.class);

		} catch (RestClientException ex) {

			throw new ConsultaCepIndisponivelException();
		}
	}
}