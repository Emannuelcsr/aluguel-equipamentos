package br.com.projetosecsr.aluguelequipamentos.cliente.excecao;

public class CepNaoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CepNaoEncontradoException() {
        super("CEP não encontrado.");
    }
}