package br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao;

public class UnidadeEquipamentoJaInativaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnidadeEquipamentoJaInativaException() {

		super("A unidade de equipamento já está inativa.");

	}

}
