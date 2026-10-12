package br.com.projetosecsr.aluguelequipamentos.reserva.entidade;

import java.math.BigDecimal;

import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.UnidadeEquipamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "itens_reserva")
public class ItemReserva {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "reserva_id", nullable = false)
	private Reserva reserva;

	@ManyToOne
	@JoinColumn(name = "unidade_equipamento_id", nullable = false)
	private UnidadeEquipamento unidadeEquipamento;

	@Column(name = "valor_diaria", nullable = false)
	private BigDecimal valorDiaria;

	@Column(name = "quantidade_dias", nullable = false)
	private Long quantidadeDias;

	@Column(name = "subtotal", nullable = false)
	private BigDecimal subtotal;

	public void definirReserva(Reserva reserva) {
		this.reserva = reserva;
	}

	public ItemReserva(UnidadeEquipamento unidadeEquipamento, BigDecimal valorDiaria, Long quantidadeDias) {
		this.unidadeEquipamento = unidadeEquipamento;
		this.valorDiaria = valorDiaria;
		this.quantidadeDias = quantidadeDias;
		this.subtotal = valorDiaria.multiply(BigDecimal.valueOf(quantidadeDias));
	}

	protected ItemReserva() {

	}

	public Long getId() {
		return id;
	}

	public Reserva getReserva() {
		return reserva;
	}

	public UnidadeEquipamento getUnidadeEquipamento() {
		return unidadeEquipamento;
	}

	public BigDecimal getValorDiaria() {
		return valorDiaria;
	}

	public Long getQuantidadeDias() {
		return quantidadeDias;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

}
