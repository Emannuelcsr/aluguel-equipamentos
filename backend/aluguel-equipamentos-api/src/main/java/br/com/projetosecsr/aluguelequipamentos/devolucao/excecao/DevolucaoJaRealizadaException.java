package br.com.projetosecsr.aluguelequipamentos.devolucao.excecao;

public class DevolucaoJaRealizadaException extends RuntimeException {

	static final long serialVersionUID = 1L;

	public DevolucaoJaRealizadaException() {

		super("Devolução dessa reserva ja foi realizada.");
	}
}
