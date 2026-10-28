package br.com.projetosecsr.aluguelequipamentos.devolucao.entidade;

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
@Table(name = "itens_devolucao")
public class ItemDevolucao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "devolucao_id", nullable = false)
	private Devolucao devolucao;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_reserva_id", nullable = false, unique = true)
	private ItemReserva itemReserva;

	@Enumerated(EnumType.STRING)
	@Column(name = "condicao_fisica", nullable = false, length = 30)
	private CondicaoFisicaDevolucao condicaoFisica;

	@Column(name = "observacoes", length = 500)
	private String observacoes;

	protected ItemDevolucao() {

	}

	public ItemDevolucao(ItemReserva itemReserva, CondicaoFisicaDevolucao condicaoFisica, String observacoes) {

		this.itemReserva = itemReserva;
		this.condicaoFisica = condicaoFisica;
		this.observacoes = observacoes;
	}

	void definirDevolucao(Devolucao devolucao) {
		this.devolucao = devolucao;
	}

	public Long getId() {
		return id;
	}

	public Devolucao getDevolucao() {
		return devolucao;
	}

	public ItemReserva getItemReserva() {
		return itemReserva;
	}

	public CondicaoFisicaDevolucao getCondicaoFisica() {
		return condicaoFisica;
	}

	public String getObservacoes() {
		return observacoes;
	}

}
