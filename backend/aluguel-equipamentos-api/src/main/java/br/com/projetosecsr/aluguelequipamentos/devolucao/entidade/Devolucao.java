package br.com.projetosecsr.aluguelequipamentos.devolucao.entidade;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.Reserva;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "devolucoes")
public class Devolucao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "data_hora_devolucao", nullable = false)
	private OffsetDateTime dataHoraDevolucao;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reserva_id", nullable = false, unique = true)
	private Reserva reserva;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_responsavel_id", nullable = false)
	private Usuario responsavel;

	@OneToMany(mappedBy = "devolucao", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	private List<ItemDevolucao> itens = new ArrayList<>();

	public void adicionarItem(ItemDevolucao itemDevolucao) {
		itens.add(itemDevolucao);
		itemDevolucao.definirDevolucao(this);
	}

	protected Devolucao() {

	}

	public Devolucao(OffsetDateTime dataHoraDevolucao, Reserva reserva, Usuario responsavel) {

		this.dataHoraDevolucao = dataHoraDevolucao;
		this.reserva = reserva;
		this.responsavel = responsavel;
	}

	public Long getId() {
		return id;
	}

	public OffsetDateTime getDataHoraDevolucao() {
		return dataHoraDevolucao;
	}

	public Reserva getReserva() {
		return reserva;
	}

	public Usuario getResponsavel() {
		return responsavel;
	}

	public List<ItemDevolucao> getItens() {
		return List.copyOf(itens);
	}

}
