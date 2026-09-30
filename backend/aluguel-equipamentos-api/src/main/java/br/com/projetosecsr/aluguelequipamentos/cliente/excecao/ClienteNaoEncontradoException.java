package br.com.projetosecsr.aluguelequipamentos.cliente.excecao;

public class ClienteNaoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ClienteNaoEncontradoException() {

		super("Cliente não encontrado.");

	}

}