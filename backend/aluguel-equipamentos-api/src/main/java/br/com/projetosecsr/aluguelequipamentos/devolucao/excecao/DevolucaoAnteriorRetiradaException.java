package br.com.projetosecsr.aluguelequipamentos.devolucao.excecao;

public class DevolucaoAnteriorRetiradaException extends RuntimeException {

	static final long serialVersionUID = 1L;

	public DevolucaoAnteriorRetiradaException() {

		super("A data e hora da devolução não podem ser anteriores à data e hora da retirada.");
	}
}
