package br.com.projetosecsr.aluguelequipamentos.usuario.excecao;

public class NovaSenhaIgualAtualException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NovaSenhaIgualAtualException() {

		super("A nova senha deve ser diferente da senha atual.");
	}

}