package br.com.projetosecsr.aluguelequipamentos.retirada.excecao;

public class ClienteInativoParaRetiradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ClienteInativoParaRetiradaException() {

		super("A retirada só pode ser registrada para cliente ativo");
	}

}
