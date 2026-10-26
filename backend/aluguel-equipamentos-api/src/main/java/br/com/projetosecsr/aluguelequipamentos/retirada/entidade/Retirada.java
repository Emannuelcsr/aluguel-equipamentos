package br.com.projetosecsr.aluguelequipamentos.retirada.entidade;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.Reserva;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "retiradas")
public class Retirada {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "data_hora_retirada", nullable = false)
	private OffsetDateTime dataHoraRetirada;

	@Column(name = "nome_recebedor", nullable = false, length = 150)
	private String nomeRecebedor;

	@Column(name = "documento_recebedor", nullable = false, length = 11)
	private String documentoRecebedor;

	@Enumerated(EnumType.STRING)
	@Column(name = "metodo_verificacao", nullable = false, length = 30)
	private MetodoVerificacaoAutorizacao metodoVerificacao;

	@Column(name = "descricao_outro_metodo", length = 500)
	private String descricaoOutroMetodo;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reserva_id", nullable = false, unique = true)
	private Reserva reserva;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_responsavel_id", nullable = false)
	private Usuario responsavel;

	@OneToMany(mappedBy = "retirada", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	private List<ItemRetirada> itens = new ArrayList<>();

	protected Retirada() {

	}

	public Retirada(OffsetDateTime dataHoraRetirada, String nomeRecebedor, String documentoRecebedor,
			MetodoVerificacaoAutorizacao metodoVerificacao, String descricaoOutroMetodo, Reserva reserva,
			Usuario responsavel) {
		this.dataHoraRetirada = dataHoraRetirada;
		this.nomeRecebedor = nomeRecebedor;
		this.documentoRecebedor = documentoRecebedor;
		this.metodoVerificacao = metodoVerificacao;
		this.descricaoOutroMetodo = descricaoOutroMetodo;
		this.reserva = reserva;
		this.responsavel = responsavel;
	}

	public void adicionarItem(ItemRetirada itemRetirada) {
		itens.add(itemRetirada);
		itemRetirada.definirRetirada(this);
	}

	public Long getId() {
		return id;
	}

	public OffsetDateTime getDataHoraRetirada() {
		return dataHoraRetirada;
	}

	public String getNomeRecebedor() {
		return nomeRecebedor;
	}

	public String getDocumentoRecebedor() {
		return documentoRecebedor;
	}

	public MetodoVerificacaoAutorizacao getMetodoVerificacao() {
		return metodoVerificacao;
	}

	public String getDescricaoOutroMetodo() {
		return descricaoOutroMetodo;
	}

	public Reserva getReserva() {
		return reserva;
	}

	public Usuario getResponsavel() {
		return responsavel;
	}

	public List<ItemRetirada> getItens() {
		return List.copyOf(itens);
	}

}
