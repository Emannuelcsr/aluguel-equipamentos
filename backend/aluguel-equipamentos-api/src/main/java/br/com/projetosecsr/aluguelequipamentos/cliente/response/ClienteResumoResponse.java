package br.com.projetosecsr.aluguelequipamentos.cliente.response;

import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.Cliente;
import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.TipoCliente;

public record ClienteResumoResponse(

		Long id, TipoCliente tipo, String nomeRazaoSocial, String nomeFantasia, String documento, String email,
		String telefone, boolean ativo

) {

	public static ClienteResumoResponse de(Cliente cliente) {

		return new ClienteResumoResponse(cliente.getId(), cliente.getTipo(), cliente.getNomeRazaoSocial(),
				cliente.getNomeFantasia(), cliente.getDocumento(), cliente.getEmail(), cliente.getTelefone(),
				cliente.isAtivo()

		);
	}

}