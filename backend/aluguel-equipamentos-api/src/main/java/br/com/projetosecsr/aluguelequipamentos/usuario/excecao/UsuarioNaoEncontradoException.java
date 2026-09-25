package br.com.projetosecsr.aluguelequipamentos.usuario.excecao;

public class UsuarioNaoEncontradoException extends RuntimeException {

    static final long serialVersionUID = 1L;

    public UsuarioNaoEncontradoException() {

        super("Usuário não encontrado.");
    }
}
