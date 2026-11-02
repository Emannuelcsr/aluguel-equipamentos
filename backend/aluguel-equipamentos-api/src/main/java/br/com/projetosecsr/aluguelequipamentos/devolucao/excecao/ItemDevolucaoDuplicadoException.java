package br.com.projetosecsr.aluguelequipamentos.devolucao.excecao;

public class ItemDevolucaoDuplicadoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ItemDevolucaoDuplicadoException() {

		super("Há itens duplicados para devolução.");
	}
}
