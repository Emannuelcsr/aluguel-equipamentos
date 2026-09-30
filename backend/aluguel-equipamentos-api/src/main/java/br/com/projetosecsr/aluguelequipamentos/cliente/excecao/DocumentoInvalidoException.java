package br.com.projetosecsr.aluguelequipamentos.cliente.excecao;

public class DocumentoInvalidoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DocumentoInvalidoException() {
        super("Documento inválido.");
    }
}