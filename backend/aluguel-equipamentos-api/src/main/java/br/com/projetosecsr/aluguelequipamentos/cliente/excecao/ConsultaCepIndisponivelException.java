package br.com.projetosecsr.aluguelequipamentos.cliente.excecao;

public class ConsultaCepIndisponivelException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConsultaCepIndisponivelException() {
        super("Serviço de consulta de CEP indisponível.");
    }
}
