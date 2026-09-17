package br.com.projetosecsr.aluguelequipamentos.autenticacao.excecao;

public class CredenciaisInvalidasException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CredenciaisInvalidasException() {
		super("E-mail ou senha inválidos.");
	}

}
