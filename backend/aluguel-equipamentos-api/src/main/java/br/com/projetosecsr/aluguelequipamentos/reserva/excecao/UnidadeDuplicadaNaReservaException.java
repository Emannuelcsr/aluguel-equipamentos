package br.com.projetosecsr.aluguelequipamentos.reserva.excecao;

public class UnidadeDuplicadaNaReservaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnidadeDuplicadaNaReservaException() {

		super("A mesma unidade de equipamento foi informada mais de uma vez na mesma reserva.");
	}

}
