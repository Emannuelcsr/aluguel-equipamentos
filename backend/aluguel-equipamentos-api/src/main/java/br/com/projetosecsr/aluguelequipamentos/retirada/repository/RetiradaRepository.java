package br.com.projetosecsr.aluguelequipamentos.retirada.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.Retirada;

public interface RetiradaRepository extends JpaRepository<Retirada, Long> {

	boolean existsByReservaId(Long id);
}
