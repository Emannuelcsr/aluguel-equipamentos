package br.com.projetosecsr.aluguelequipamentos.retirada.excecao;

public class ObservacaoObrigatoriaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ObservacaoObrigatoriaException() {

		super("É obrigatório informar as observações quando a condição física do equipamento for COM_OBSERVACOES.");
	}
}
