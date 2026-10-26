package br.com.projetosecsr.aluguelequipamentos.reserva.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT r FROM Reserva r WHERE r.id = :id")
	Optional<Reserva> buscarPorIdComBloqueio(@Param("id") Long id);
}
