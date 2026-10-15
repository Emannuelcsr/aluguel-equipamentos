package br.com.projetosecsr.aluguelequipamentos.reserva.excecao;

public class ConflitoDeReservaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConflitoDeReservaException() {

		super("A unidade de equipamento já possui uma reserva conflitante nesse período.");

	}
}
