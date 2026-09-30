package br.com.projetosecsr.aluguelequipamentos.cliente.excecao;

public class EstadoInvalidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EstadoInvalidoException() {
        super("Estado inválido.");
    }
}
