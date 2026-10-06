package br.com.projetosecsr.aluguelequipamentos.categoria.excecao;

public class CategoriaJaCadastradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CategoriaJaCadastradaException() {

		super("Categoria já cadastrada.");

	}
}