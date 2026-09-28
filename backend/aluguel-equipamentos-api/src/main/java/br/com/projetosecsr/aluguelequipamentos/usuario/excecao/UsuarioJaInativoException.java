package br.com.projetosecsr.aluguelequipamentos.usuario.excecao;

public class UsuarioJaInativoException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UsuarioJaInativoException() {

		super("O usuário já está inativo.");

	}

}
