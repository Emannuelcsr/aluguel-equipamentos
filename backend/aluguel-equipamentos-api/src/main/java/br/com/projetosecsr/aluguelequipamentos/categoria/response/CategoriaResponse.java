package br.com.projetosecsr.aluguelequipamentos.categoria.response;

import java.time.Instant;

import br.com.projetosecsr.aluguelequipamentos.categoria.entidade.Categoria;

public record CategoriaResponse(

		Long id, String nome, String descricao, boolean ativo, Instant dataCriacao, Instant dataAtualizacao

) {

	public static CategoriaResponse de(Categoria categoria) {

		return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getDescricao(),
				categoria.isAtivo(), categoria.getDataCriacao(), categoria.getDataAtualizacao());
	}

}
