package br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade;

import java.time.Instant;

import br.com.projetosecsr.aluguelequipamentos.equipamento.entidade.Equipamento;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.OperacaoInvalidaException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "unidades_equipamento")
public class UnidadeEquipamento {

	@Id
	@SequenceGenerator(name = "unidade_equipamento_seq_generator", sequenceName = "unidades_equipamento_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "unidade_equipamento_seq_generator")
	private Long id;

	@Column(nullable = false, length = 20)
	private String codigo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private StatusUnidadeEquipamento status;

	@Column(nullable = false)
	private boolean ativo = true;

	@Column(name = "data_criacao", nullable = false, updatable = false)
	private Instant dataCriacao;

	@Column(name = "data_atualizacao", nullable = false)
	private Instant dataAtualizacao;

	@ManyToOne
	@JoinColumn(name = "equipamento_id", nullable = false)
	private Equipamento equipamento;

	@PrePersist
	void prepararParaCriacao() {

		Instant agora = Instant.now();
		this.dataCriacao = agora;
		this.dataAtualizacao = agora;

		this.codigo = "UNI-" + String.format("%06d", getId());

	}

	@PreUpdate
	void prepararParaAtualizacao() {

		this.dataAtualizacao = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getCodigo() {
		return codigo;
	}

	public StatusUnidadeEquipamento getStatus() {
		return status;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public Instant getDataCriacao() {
		return dataCriacao;
	}

	public Instant getDataAtualizacao() {
		return dataAtualizacao;
	}

	public Equipamento getEquipamento() {
		return equipamento;
	}

	public void ativar() {
		this.ativo = true;
	}

	public void desativar() {
		if (this.status == StatusUnidadeEquipamento.ALUGADO) {

			throw new OperacaoInvalidaException("A unidade não pode ser desativada se estiver alugada.");
		}

		this.ativo = false;
	}

	public void enviarParaManutencao() {

		if (this.status == StatusUnidadeEquipamento.EM_MANUTENCAO || this.status == StatusUnidadeEquipamento.ALUGADO) {

			throw new OperacaoInvalidaException(
					"A unidade só pode ser enviada para manutenção quando estiver disponível.");

		}

		this.status = StatusUnidadeEquipamento.EM_MANUTENCAO;

	}

	public void liberarDaManutencao() {

		if (this.status == StatusUnidadeEquipamento.DISPONIVEL || this.status == StatusUnidadeEquipamento.ALUGADO) {

			throw new OperacaoInvalidaException("A unidade só pode sair da manutenção se estiver em manutenção.");

		}

		this.status = StatusUnidadeEquipamento.DISPONIVEL;
	}

	protected UnidadeEquipamento() {
	}

	public UnidadeEquipamento(Equipamento equipamento) {
		this.equipamento = equipamento;
		this.status = StatusUnidadeEquipamento.DISPONIVEL;
	}

}
