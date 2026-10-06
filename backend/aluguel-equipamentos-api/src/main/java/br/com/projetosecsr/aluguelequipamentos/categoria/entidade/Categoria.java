package br.com.projetosecsr.aluguelequipamentos.categoria.entidade;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "categorias")
public class Categoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nome", nullable = false, length = 100)
	private String nome;

	@Column(name = "descricao", length = 255)
	private String descricao;

	@Column(nullable = false)
	private boolean ativo = true;

	@Column(name = "data_criacao", nullable = false, updatable = false)
	private Instant dataCriacao;

	@Column(name = "data_atualizacao", nullable = false)
	private Instant dataAtualizacao;

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

	public Categoria(String nome, String descricao) {

		this.nome = nome;
		this.descricao = descricao;
	}

	protected Categoria() {

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

	public boolean isAtivo() {
		return ativo;
	}

	public Instant getDataCriacao() {
		return dataCriacao;
	}

	public Instant getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void atualizarDados(String nome, String descricao) {

		this.nome = nome;
		this.descricao = descricao;
	}

	public void ativar() {
		this.ativo = true;
	}

	public void desativar() {
		this.ativo = false;
	}

}
