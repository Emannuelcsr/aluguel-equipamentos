package br.com.projetosecsr.aluguelequipamentos.equipamento.excecao;

public class EquipamentoNaoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EquipamentoNaoEncontradoException() {

		super("Equipamento não encontrado.");
	}

}
