package br.com.projetosecsr.aluguelequipamentos.usuario.excecao;

public class EmailJaCadastradoException extends RuntimeException {

	static final long serialVersionUID = 1L;

	public EmailJaCadastradoException() {

		super("O e-mail informado já está cadastrado.");
	}

}
