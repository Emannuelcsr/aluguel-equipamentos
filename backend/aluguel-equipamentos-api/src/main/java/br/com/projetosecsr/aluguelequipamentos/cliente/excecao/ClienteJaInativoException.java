package br.com.projetosecsr.aluguelequipamentos.cliente.excecao;

public class ClienteJaInativoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ClienteJaInativoException() {
        super("Cliente já está inativo.");
    }
}