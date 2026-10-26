package br.com.projetosecsr.aluguelequipamentos.retirada.excecao;

public class RetiradaJaRealizadaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RetiradaJaRealizadaException() {

		super("Já existe uma retirada registrada para esta reserva.");
	}
}