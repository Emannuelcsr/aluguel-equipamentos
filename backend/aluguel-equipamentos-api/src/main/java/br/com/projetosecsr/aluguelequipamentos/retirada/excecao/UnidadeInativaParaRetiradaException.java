package br.com.projetosecsr.aluguelequipamentos.retirada.excecao;

public class UnidadeInativaParaRetiradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnidadeInativaParaRetiradaException() {

		super("A unidade de equipamento está inativa e não pode ser retirada.");
	}
}
