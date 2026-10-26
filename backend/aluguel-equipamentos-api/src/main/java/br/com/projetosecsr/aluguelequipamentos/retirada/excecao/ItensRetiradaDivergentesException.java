package br.com.projetosecsr.aluguelequipamentos.retirada.excecao;

public class ItensRetiradaDivergentesException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ItensRetiradaDivergentesException() {

		super("Os itens informados para retirada devem corresponder exatamente aos itens da reserva.");
	}
}
