package br.com.projetosecsr.aluguelequipamentos.retirada.excecao;

public class ReservaNaoAprovadaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ReservaNaoAprovadaException() {

		super("A retirada só pode ser registrada para uma reserva aprovada.");
	}
}
