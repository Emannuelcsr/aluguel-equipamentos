package br.com.projetosecsr.aluguelequipamentos.equipamento.entidade;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.projetosecsr.aluguelequipamentos.categoria.entidade.Categoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "equipamentos")
public class Equipamento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nome", nullable = false, length = 150)
	private String nome;

	@Column(name = "descricao", length = 500)
	private String descricao;

	@Column(name = "valor_diaria", nullable = false)
	private BigDecimal valorDiaria;

	@Column(nullable = false)
	private boolean ativo = true;

	@Column(name = "data_criacao", nullable = false, updatable = false)
	private Instant dataCriacao;

	@Column(name = "data_atualizacao", nullable = false)
	private Instant dataAtualizacao;

	@ManyToOne
	@JoinColumn(name = "categoria_id", nullable = false)
	private Categoria categoria;

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

	public void atualizarDados(String nome, String descricao, BigDecimal valorDiaria, Categoria categoria) {
		this.nome = nome;
		this.descricao = descricao;
		this.valorDiaria = valorDiaria;
		this.categoria = categoria;
	}

	public Equipamento(String nome, String descricao, BigDecimal valorDiaria, Categoria categoria) {
		this.nome = nome;
		this.descricao = descricao;
		this.valorDiaria = valorDiaria;
		this.categoria = categoria;
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public BigDecimal getValorDiaria() {
		return valorDiaria;
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

	public void ativar() {
		this.ativo = true;
	}

	public void desativar() {
		this.ativo = false;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	protected Equipamento() {
	}

}
