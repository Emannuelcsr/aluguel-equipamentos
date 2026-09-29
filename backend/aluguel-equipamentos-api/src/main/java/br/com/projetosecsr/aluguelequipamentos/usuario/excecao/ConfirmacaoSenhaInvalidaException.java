package br.com.projetosecsr.aluguelequipamentos.usuario.excecao;

public class ConfirmacaoSenhaInvalidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConfirmacaoSenhaInvalidaException() {

		super("A confirmação da nova senha não corresponde à nova senha.");

	}

}