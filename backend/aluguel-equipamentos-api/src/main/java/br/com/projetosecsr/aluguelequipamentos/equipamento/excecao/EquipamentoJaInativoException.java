package br.com.projetosecsr.aluguelequipamentos.equipamento.excecao;

public class EquipamentoJaInativoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EquipamentoJaInativoException() {

		super("O equipamento já está inativo.");

	}

}
