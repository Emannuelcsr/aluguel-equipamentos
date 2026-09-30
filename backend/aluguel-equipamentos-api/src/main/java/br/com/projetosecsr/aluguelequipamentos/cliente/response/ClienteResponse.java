package br.com.projetosecsr.aluguelequipamentos.cliente.response;

import java.time.Instant;

import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.Cliente;
import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.TipoCliente;

public record ClienteResponse(

		Long id, TipoCliente tipo, String nomeRazaoSocial, String nomeFantasia, String documento, String email,
		String telefone, String cep, String logradouro, String numero, String complemento, String bairro, String cidade,
		String estado, boolean ativo, Instant dataCriacao, Instant dataAtualizacao

) {

	public static ClienteResponse de(Cliente cliente) {

		return new ClienteResponse(cliente.getId(), cliente.getTipo(), cliente.getNomeRazaoSocial(),
				cliente.getNomeFantasia(), cliente.getDocumento(), cliente.getEmail(), cliente.getTelefone(),
				cliente.getCep(), cliente.getLogradouro(), cliente.getNumero(), cliente.getComplemento(),
				cliente.getBairro(), cliente.getCidade(), cliente.getEstado(), cliente.isAtivo(),
				cliente.getDataCriacao(), cliente.getDataAtualizacao());
	}

}
