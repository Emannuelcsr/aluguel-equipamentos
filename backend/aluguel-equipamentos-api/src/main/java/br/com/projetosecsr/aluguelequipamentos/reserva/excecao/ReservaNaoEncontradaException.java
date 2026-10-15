package br.com.projetosecsr.aluguelequipamentos.reserva.excecao;

public class ReservaNaoEncontradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ReservaNaoEncontradaException() {

		super("Reserva não encontrada.");
	}
}