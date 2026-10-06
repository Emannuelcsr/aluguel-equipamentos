package br.com.projetosecsr.aluguelequipamentos.categoria.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.projetosecsr.aluguelequipamentos.categoria.entidade.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

	boolean existsByNome(String nome);

	boolean existsByNomeAndIdNot(String nome, Long id);

}
