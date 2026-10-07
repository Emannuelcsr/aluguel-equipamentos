package br.com.projetosecsr.aluguelequipamentos.equipamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.projetosecsr.aluguelequipamentos.equipamento.entidade.Equipamento;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {

	boolean existsByNomeAndIdNot(String nome, Long id);

}
