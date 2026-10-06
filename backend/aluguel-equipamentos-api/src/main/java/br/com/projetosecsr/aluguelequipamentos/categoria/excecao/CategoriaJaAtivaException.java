package br.com.projetosecsr.aluguelequipamentos.categoria.excecao;

public class CategoriaJaAtivaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CategoriaJaAtivaException() {
        super("Categoria já está ativa.");
    }
}