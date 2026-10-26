package br.com.projetosecsr.aluguelequipamentos.retirada.entidade;

import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.ItemReserva;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "itens_retirada")
public class ItemRetirada {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "retirada_id", nullable = false)
	private Retirada retirada;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_reserva_id", nullable = false, unique = true)
	private ItemReserva itemReserva;

	@Enumerated(EnumType.STRING)
	@Column(name = "condicao_fisica", nullable = false, length = 30)
	private CondicaoFisicaRetirada condicaoFisica;

	@Column(length = 500)
	private String observacoes;

	protected ItemRetirada() {

	}

	public ItemRetirada(ItemReserva itemReserva, CondicaoFisicaRetirada condicaoFisica, String observacoes) {
		this.itemReserva = itemReserva;
		this.condicaoFisica = condicaoFisica;
		this.observacoes = observacoes;
	}

	public void definirRetirada(Retirada retirada) {

		this.retirada = retirada;
	}

	public Long getId() {
		return id;
	}

	public Retirada getRetirada() {
		return retirada;
	}

	public ItemReserva getItemReserva() {
		return itemReserva;
	}

	public CondicaoFisicaRetirada getCondicaoFisica() {
		return condicaoFisica;
	}

	public String getObservacoes() {
		return observacoes;
	}

	
	
	
}
