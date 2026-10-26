package br.com.projetosecsr.aluguelequipamentos.retirada.excecao;

public class UnidadeIndisponivelParaRetiradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnidadeIndisponivelParaRetiradaException() {

		super("A unidade está indisponível para retirada.");
	}
}
