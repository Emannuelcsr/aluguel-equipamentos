package br.com.projetosecsr.aluguelequipamentos.reserva.excecao;

public class DataInicialInvalidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DataInicialInvalidaException() {

		super("Data inicial não pode ser anterior à data de hoje");

	}
}
