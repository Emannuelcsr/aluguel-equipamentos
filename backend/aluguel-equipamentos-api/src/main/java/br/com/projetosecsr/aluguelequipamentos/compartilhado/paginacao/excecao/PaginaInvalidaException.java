package br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao;

public class PaginaInvalidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PaginaInvalidaException(String mensagem) {
		super(mensagem);
	}

}
