package br.com.projetosecsr.aluguelequipamentos.cliente.excecao;

public class DocumentoIncompativelComTipoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DocumentoIncompativelComTipoException() {
        super("Documento incompatível com o tipo do cliente.");
    }
}