package br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao;

public class UnidadeEquipamentoNaoEncontradoException extends RuntimeException {

	static final long serialVersionUID = 1L;

	public UnidadeEquipamentoNaoEncontradoException() {

		super("Unidade de equipamento não encontrada.");
	}
}
