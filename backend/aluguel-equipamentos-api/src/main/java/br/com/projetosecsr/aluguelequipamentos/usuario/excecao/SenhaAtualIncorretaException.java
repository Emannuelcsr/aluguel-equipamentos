package br.com.projetosecsr.aluguelequipamentos.usuario.excecao;

public class SenhaAtualIncorretaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SenhaAtualIncorretaException() {

		super("A senha atual está incorreta.");

	}

}