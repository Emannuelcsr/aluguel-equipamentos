package br.com.projetosecsr.aluguelequipamentos.devolucao.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.projetosecsr.aluguelequipamentos.devolucao.entidade.Devolucao;

public interface DevolucaoRepository extends JpaRepository<Devolucao, Long> {

	boolean existsByReservaId(Long reservaId);

}
