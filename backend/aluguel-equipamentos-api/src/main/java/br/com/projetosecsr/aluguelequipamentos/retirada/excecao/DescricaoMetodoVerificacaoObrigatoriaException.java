package br.com.projetosecsr.aluguelequipamentos.retirada.excecao;

public class DescricaoMetodoVerificacaoObrigatoriaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DescricaoMetodoVerificacaoObrigatoriaException() {

		super("A descrição do método de verificação é obrigatória quando o método selecionado for OUTRO.");
	}

}
