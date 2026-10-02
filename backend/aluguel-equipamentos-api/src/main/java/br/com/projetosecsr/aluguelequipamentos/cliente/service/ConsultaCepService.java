package br.com.projetosecsr.aluguelequipamentos.cliente.service;

import org.springframework.stereotype.Service;

import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.CepNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.integracao.ViaCepClient;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.EnderecoCepResponse;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.ViaCepResponse;

@Service
public class ConsultaCepService {

	private final ViaCepClient viaCepClient;

	public ConsultaCepService(ViaCepClient viaCepClient) {
		this.viaCepClient = viaCepClient;
	}

	public EnderecoCepResponse consultar(String cep) {

		ViaCepResponse resposta = viaCepClient.consultar(cep);

		if (Boolean.TRUE.equals(resposta.erro())) {

			throw new CepNaoEncontradoException();

		}

		EnderecoCepResponse enderecoCepResponse = new EnderecoCepResponse(resposta.cep(), resposta.logradouro(),
				resposta.complemento(), resposta.bairro(), resposta.localidade(), resposta.uf());

		return enderecoCepResponse;
	}
}
