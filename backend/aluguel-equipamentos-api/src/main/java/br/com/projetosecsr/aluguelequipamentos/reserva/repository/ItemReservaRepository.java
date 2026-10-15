package br.com.projetosecsr.aluguelequipamentos.reserva.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.ItemReserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.StatusReserva;

public interface ItemReservaRepository extends JpaRepository<ItemReserva, Long> {

	@Query("""
			    SELECT COUNT(item) > 0
			    FROM ItemReserva item
			    WHERE item.unidadeEquipamento.id = :unidadeId
			      AND :dataInicial <= item.reserva.dataFinal
			      AND :dataFinal >= item.reserva.dataInicial
			      AND item.reserva.status IN :statusBloqueadores
			""")
    boolean existeConflito(
            @Param("unidadeId") Long unidadeId,
            @Param("dataInicial") LocalDate dataInicial,
            @Param("dataFinal") LocalDate dataFinal,
            @Param("statusBloqueadores") List<StatusReserva> statusBloqueadores);

}
