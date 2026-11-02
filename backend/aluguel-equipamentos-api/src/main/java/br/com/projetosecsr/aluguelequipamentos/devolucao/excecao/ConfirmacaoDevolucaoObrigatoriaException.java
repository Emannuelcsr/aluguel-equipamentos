package br.com.projetosecsr.aluguelequipamentos.devolucao.excecao;

public class ConfirmacaoDevolucaoObrigatoriaException extends RuntimeException {

	static final long serialVersionUID = 1L;

	public ConfirmacaoDevolucaoObrigatoriaException() {

		super("Favor confirmar a devolução dos equipamentos alugados.");
	}
}
