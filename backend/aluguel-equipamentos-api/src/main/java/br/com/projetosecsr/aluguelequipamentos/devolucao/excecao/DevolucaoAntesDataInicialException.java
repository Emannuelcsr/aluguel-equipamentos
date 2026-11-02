package br.com.projetosecsr.aluguelequipamentos.devolucao.excecao;

public class DevolucaoAntesDataInicialException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DevolucaoAntesDataInicialException() {

		super("Data da devolução não pode ser anterior à data inicial.");

	}
}
