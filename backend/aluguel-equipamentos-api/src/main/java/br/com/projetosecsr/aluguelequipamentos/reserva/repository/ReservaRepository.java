package br.com.projetosecsr.aluguelequipamentos.reserva.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

}
