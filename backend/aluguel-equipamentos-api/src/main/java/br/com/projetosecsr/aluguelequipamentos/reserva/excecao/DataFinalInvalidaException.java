package br.com.projetosecsr.aluguelequipamentos.reserva.excecao;

public class DataFinalInvalidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DataFinalInvalidaException() {

		super("Data final não pode ser anterior à data inicial");

	}
}
