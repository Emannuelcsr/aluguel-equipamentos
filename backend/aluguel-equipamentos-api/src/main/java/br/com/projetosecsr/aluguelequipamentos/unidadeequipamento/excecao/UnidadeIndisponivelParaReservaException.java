package br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao;

public class UnidadeIndisponivelParaReservaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnidadeIndisponivelParaReservaException() {

		super("A unidade de equipamento está em manutenção e não pode ser reservada.");

	}
}
