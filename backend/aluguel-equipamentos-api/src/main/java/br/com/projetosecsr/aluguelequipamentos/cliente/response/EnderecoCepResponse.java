package br.com.projetosecsr.aluguelequipamentos.cliente.response;

public record EnderecoCepResponse(

		String cep, String logradouro, String complemento, String bairro, String cidade, String estado

) {

}