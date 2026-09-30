package br.com.projetosecsr.aluguelequipamentos.cliente.excecao;

public class EmailClienteJaCadastradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmailClienteJaCadastradoException() {
        super("E-mail já cadastrado para outro cliente.");
    }
}