package br.com.projetosecsr.aluguelequipamentos.categoria.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.projetosecsr.aluguelequipamentos.categoria.entidade.Categoria;
import br.com.projetosecsr.aluguelequipamentos.categoria.response.CategoriaResponse;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

	boolean existsByNome(String nome);

	boolean existsByNomeAndIdNot(String nome, Long id);

	List<Categoria> findAllByAtivoTrue();

}
