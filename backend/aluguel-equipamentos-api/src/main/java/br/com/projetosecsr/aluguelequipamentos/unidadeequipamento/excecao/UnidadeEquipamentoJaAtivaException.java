package br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao;

public class UnidadeEquipamentoJaAtivaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnidadeEquipamentoJaAtivaException() {

		super("A unidade do equipamento já está ativa.");

	}

}
