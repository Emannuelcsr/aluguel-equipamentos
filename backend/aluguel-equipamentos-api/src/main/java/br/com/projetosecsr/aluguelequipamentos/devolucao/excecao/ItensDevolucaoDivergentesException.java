package br.com.projetosecsr.aluguelequipamentos.devolucao.excecao;

public class ItensDevolucaoDivergentesException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ItensDevolucaoDivergentesException() {

		super("Os itens informados para devolução devem corresponder exatamente aos itens da reserva.");
	}
}
