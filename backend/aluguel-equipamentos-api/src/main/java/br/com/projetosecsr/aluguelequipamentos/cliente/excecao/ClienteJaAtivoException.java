package br.com.projetosecsr.aluguelequipamentos.cliente.excecao;

public class ClienteJaAtivoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ClienteJaAtivoException() {
        super("Cliente já está ativo.");
    }
}