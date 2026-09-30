package br.com.projetosecsr.aluguelequipamentos.cliente.excecao;

public class NomeFantasiaNaoPermitidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NomeFantasiaNaoPermitidoException() {
        super("Nome fantasia não é permitido para cliente pessoa física.");
    }
}