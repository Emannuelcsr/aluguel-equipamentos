package br.com.projetosecsr.aluguelequipamentos.reserva.repository;


import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.Reserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.StatusReserva;
import jakarta.persistence.LockModeType;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT r FROM Reserva r WHERE r.id = :id")
	Optional<Reserva> buscarPorIdComBloqueio(@Param("id") Long id);
	
	Page<Reserva> findByStatusOrderByIdAsc(
		    StatusReserva status,
		    Pageable paginacao
		);
}
