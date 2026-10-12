package br.com.projetosecsr.aluguelequipamentos.reserva.excecao;

public class OperacaoReservaInvalidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OperacaoReservaInvalidaException(String mensagem) {

		super(mensagem);

	}
}
