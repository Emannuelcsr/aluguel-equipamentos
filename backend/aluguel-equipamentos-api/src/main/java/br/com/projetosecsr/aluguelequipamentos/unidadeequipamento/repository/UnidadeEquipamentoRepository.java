package br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.projetosecsr.aluguelequipamentos.categoria.entidade.Categoria;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.StatusUnidadeEquipamento;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.UnidadeEquipamento;

public interface UnidadeEquipamentoRepository extends JpaRepository<UnidadeEquipamento, Long> {

	Page<UnidadeEquipamento> findAllByEquipamento_Id(
		    Long equipamentoId,
		    Pageable pageable
		);

	
	List<UnidadeEquipamento> findAllByAtivoTrueAndStatus(
		    StatusUnidadeEquipamento status
		);
	
}
