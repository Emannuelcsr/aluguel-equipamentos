package br.com.projetosecsr.aluguelequipamentos.retirada.excecao;

public class RetiradaForaDoPeriodoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RetiradaForaDoPeriodoException() {

		super("A retirada só pode ser registrada dentro do período da reserva.");
	}
}
