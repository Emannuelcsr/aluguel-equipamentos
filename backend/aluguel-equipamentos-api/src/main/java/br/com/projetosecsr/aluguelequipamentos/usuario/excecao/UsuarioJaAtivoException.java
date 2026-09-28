package br.com.projetosecsr.aluguelequipamentos.usuario.excecao;

public class UsuarioJaAtivoException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UsuarioJaAtivoException() {

		super("O usuário já está ativo.");

	}

}
