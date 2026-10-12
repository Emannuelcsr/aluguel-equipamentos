package br.com.projetosecsr.aluguelequipamentos.reserva.entidade;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.Cliente;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.OperacaoReservaInvalidaException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservas")
public class Reserva {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "cliente_id", nullable = false)
	private Cliente cliente;

	@Column(name = "data_inicial", nullable = false)
	private LocalDate dataInicial;

	@Column(name = "data_final", nullable = false)
	private LocalDate dataFinal;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private StatusReserva status;

	@Column(name = "valor_total", nullable = false)
	private BigDecimal valorTotal;

	@OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ItemReserva> itens = new ArrayList<>();

	@Column(name = "data_criacao", nullable = false, updatable = false)
	private Instant dataCriacao;

	@Column(name = "data_atualizacao", nullable = false)
	private Instant dataAtualizacao;

	public void adicionarItem(ItemReserva itemReserva) {

		itens.add(itemReserva);

		itemReserva.definirReserva(this);
	}

	@PrePersist
	void prepararParaCriacao() {

		Instant agora = Instant.now();
		this.dataCriacao = agora;
		this.dataAtualizacao = agora;

	}

	@PreUpdate
	void prepararParaAtualizacao() {

		this.dataAtualizacao = Instant.now();
	}

	protected Reserva() {

	}

	public Reserva(Cliente cliente, LocalDate dataInicial, LocalDate dataFinal) {
		this.cliente = cliente;
		this.dataInicial = dataInicial;
		this.dataFinal = dataFinal;
		this.status = StatusReserva.SOLICITADA;

	}

	public void calcularValorTotal() {

		BigDecimal total = BigDecimal.ZERO;

		for (ItemReserva itemReserva1 : itens) {

			total = total.add(itemReserva1.getSubtotal());

		}

		this.valorTotal = total;
	}

	public void aprovar() {

		if (this.status != StatusReserva.SOLICITADA) {

			throw new OperacaoReservaInvalidaException(
					"A reserva só pode ser aprovada quando estiver com status SOLICITADA.");
		}

		this.status = StatusReserva.APROVADA;

	}

	public void rejeitar() {

		if (this.status != StatusReserva.SOLICITADA) {

			throw new OperacaoReservaInvalidaException(
					"A reserva só pode ser rejeitada quando estiver com status SOLICITADA.");
		}

		this.status = StatusReserva.REJEITADA;

	}

	public void cancelar() {

		if ((this.status != StatusReserva.SOLICITADA) && (this.status != StatusReserva.APROVADA)) {

			throw new OperacaoReservaInvalidaException(
					"A reserva só pode ser cancelada quando estiver com status SOLICITADA ou APROVADA.");
		}

		this.status = StatusReserva.CANCELADA;

	}

	public void iniciar() {

		if (this.status != StatusReserva.APROVADA) {

			throw new OperacaoReservaInvalidaException(
					"A reserva só pode ser iniciada quando estiver com status APROVADA.");
		}

		this.status = StatusReserva.EM_ANDAMENTO;

	}

	public void concluir() {

		if (this.status != StatusReserva.EM_ANDAMENTO) {

			throw new OperacaoReservaInvalidaException(
					"A reserva só pode ser concluída quando estiver com status EM_ANDAMENTO.");
		}

		this.status = StatusReserva.FINALIZADA;

	}

	public Long getId() {
		return id;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public LocalDate getDataInicial() {
		return dataInicial;
	}

	public LocalDate getDataFinal() {
		return dataFinal;
	}

	public StatusReserva getStatus() {
		return status;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public List<ItemReserva> getItens() {
		return itens;
	}

	public Instant getDataCriacao() {
		return dataCriacao;
	}

	public Instant getDataAtualizacao() {
		return dataAtualizacao;
	}

}
