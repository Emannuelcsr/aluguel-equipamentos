package br.com.projetosecsr.aluguelequipamentos.devolucao.excecao;

public class DevolucaoNaoEncontradaException extends RuntimeException {

	static final long serialVersionUID = 1L;

	public DevolucaoNaoEncontradaException() {

		super("Devolução não encontrada.");
	}
}
