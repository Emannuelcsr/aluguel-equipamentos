package br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao;

import java.util.List;

public record PaginaResponse<T>(

		List<T> conteudo, int paginaAtual, int tamanho, int quantidadeElementos, long totalElementos, int totalPaginas,
		boolean primeiraPagina, boolean ultimaPagina

) {

}
