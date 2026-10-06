package br.com.projetosecsr.aluguelequipamentos.categoria.excecao;

public class CategoriaJaInativaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CategoriaJaInativaException() {
        super("Categoria já está inativa.");
    }
}