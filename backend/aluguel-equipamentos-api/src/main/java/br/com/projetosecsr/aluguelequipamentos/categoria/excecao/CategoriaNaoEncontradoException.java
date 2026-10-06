package br.com.projetosecsr.aluguelequipamentos.categoria.excecao;

public class CategoriaNaoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CategoriaNaoEncontradoException() {

		super("Categoria não encontrada.");

	}
}
