package br.com.projetosecsr.aluguelequipamentos.retirada.excecao;

public class ConfirmacaoRetiradaObrigatoriaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConfirmacaoRetiradaObrigatoriaException() {

		super("A autorização e o recebimento devem estar confirmados para registrar a retirada.");
	}

}
