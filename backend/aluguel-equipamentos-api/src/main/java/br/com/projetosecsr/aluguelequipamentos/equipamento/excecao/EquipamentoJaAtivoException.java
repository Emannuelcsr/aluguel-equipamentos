package br.com.projetosecsr.aluguelequipamentos.equipamento.excecao;

public class EquipamentoJaAtivoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EquipamentoJaAtivoException() {

		super("O equipamento já está ativo.");

	}
}
