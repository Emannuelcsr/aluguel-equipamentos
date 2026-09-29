package br.com.projetosecsr.aluguelequipamentos.usuario.excecao;

public class RedefinicaoPropriaSenhaNaoPermitidaException extends RuntimeException {

	static final long serialVersionUID = 1L;

	public RedefinicaoPropriaSenhaNaoPermitidaException() {

		super("Não é permitido redefinir a própria senha por esta operação.");
	}

}
