package br.com.projetosecsr.aluguelequipamentos.cliente.excecao;

public class DocumentoJaCadastradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DocumentoJaCadastradoException() {
        super("Documento já cadastrado.");
    }
}