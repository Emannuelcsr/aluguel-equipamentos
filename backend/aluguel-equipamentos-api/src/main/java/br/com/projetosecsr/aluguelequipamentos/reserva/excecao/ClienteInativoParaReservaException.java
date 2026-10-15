package br.com.projetosecsr.aluguelequipamentos.reserva.excecao;

public class ClienteInativoParaReservaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ClienteInativoParaReservaException() {
		super("Cliente inativo não pode criar uma nova reserva");
	}
}
