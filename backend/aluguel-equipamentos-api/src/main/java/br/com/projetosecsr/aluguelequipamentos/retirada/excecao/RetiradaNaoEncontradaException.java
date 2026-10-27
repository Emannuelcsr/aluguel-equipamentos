package br.com.projetosecsr.aluguelequipamentos.retirada.excecao;

public class RetiradaNaoEncontradaException extends RuntimeException {

	static final long serialVersionUID = 1L;

	public RetiradaNaoEncontradaException() {

		super("Retirada não encontrada.");
	}
}
