package br.com.projetosecsr.aluguelequipamentos.usuario.excecao;

public class AutodesativacaoNaoPermitidaException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AutodesativacaoNaoPermitidaException() {

		super("Não é permitido desativar a própria conta.");
	}

}
