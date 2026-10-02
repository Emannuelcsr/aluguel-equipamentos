package br.com.projetosecsr.aluguelequipamentos.cliente.response;

public record ViaCepResponse(

		String cep, String logradouro, String complemento, String bairro, String localidade, String uf, Boolean erro

) {

}
