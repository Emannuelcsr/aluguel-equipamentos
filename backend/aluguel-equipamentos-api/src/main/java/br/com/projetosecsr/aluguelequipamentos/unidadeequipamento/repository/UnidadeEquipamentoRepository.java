package br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.UnidadeEquipamento;

public interface UnidadeEquipamentoRepository extends JpaRepository<UnidadeEquipamento, Long> {

}
