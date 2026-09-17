package br.com.projetosecsr.aluguelequipamentos.autenticacao.excecao;

public class UsuarioInativoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UsuarioInativoException() {
		super("A conta do usuário está inativa.");
	}
}
