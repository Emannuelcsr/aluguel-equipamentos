package br.com.projetosecsr.aluguelequipamentos.devolucao.excecao;

public class StatusUnidadeInvalidoParaDevolucaoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public StatusUnidadeInvalidoParaDevolucaoException(String codigoPatrimonial) {

		super("A unidade do equipamento " + codigoPatrimonial
				+ " deve estar com status ALUGADO para permitir a devolução.");
	}
}
