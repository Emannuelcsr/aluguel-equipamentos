package br.com.projetosecsr.aluguelequipamentos.cliente.entidade;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private TipoCliente tipo;

	@Column(name = "nome_razao_social", nullable = false, length = 150)
	private String nomeRazaoSocial;

	@Column(name = "nome_fantasia", length = 150)
	private String nomeFantasia;

	@Column(nullable = false, unique = true, length = 14)
	private String documento;

	@Column(nullable = false, unique = true, length = 150)
	private String email;

	@Column(nullable = false, length = 11)
	private String telefone;

	@Column(nullable = false, length = 8)
	private String cep;

	@Column(nullable = false, length = 150)
	private String logradouro;

	@Column(nullable = false, length = 20)
	private String numero;

	@Column(length = 100)
	private String complemento;

	@Column(nullable = false, length = 100)
	private String bairro;

	@Column(nullable = false, length = 100)
	private String cidade;

	@Column(nullable = false, length = 2)
	private String estado;

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

	protected Cliente() {

	}

	public Cliente(TipoCliente tipo, String nomeRazaoSocial, String nomeFantasia, String documento, String email,
			String telefone, String cep, String logradouro, String numero, String complemento, String bairro,
			String cidade, String estado) {

		this.tipo = tipo;
		this.nomeRazaoSocial = nomeRazaoSocial;
		this.nomeFantasia = nomeFantasia;
		this.documento = documento;
		this.email = email;
		this.telefone = telefone;
		this.cep = cep;
		this.logradouro = logradouro;
		this.numero = numero;
		this.complemento = complemento;
		this.bairro = bairro;
		this.cidade = cidade;
		this.estado = estado;
	}

	public Long getId() {
		return id;
	}

	public TipoCliente getTipo() {
		return tipo;
	}

	public String getNomeRazaoSocial() {
		return nomeRazaoSocial;
	}

	public String getNomeFantasia() {
		return nomeFantasia;
	}

	public String getDocumento() {
		return documento;
	}

	public String getEmail() {
		return email;
	}

	public String getTelefone() {
		return telefone;
	}

	public String getCep() {
		return cep;
	}

	public String getLogradouro() {
		return logradouro;
	}

	public String getNumero() {
		return numero;
	}

	public String getComplemento() {
		return complemento;
	}

	public String getBairro() {
		return bairro;
	}

	public String getCidade() {
		return cidade;
	}

	public String getEstado() {
		return estado;
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

	public void atualizarDados(String nomeRazaoSocial, String nomeFantasia, String email, String telefone, String cep,
			String logradouro, String numero, String complemento, String bairro, String cidade, String estado) {

		this.nomeRazaoSocial = nomeRazaoSocial;
		this.nomeFantasia = nomeFantasia;
		this.email = email;
		this.telefone = telefone;
		this.cep = cep;
		this.logradouro = logradouro;
		this.numero = numero;
		this.complemento = complemento;
		this.bairro = bairro;
		this.cidade = cidade;
		this.estado = estado;
	}

	public void ativar() {
		this.ativo = true;
	}

	public void desativar() {
		this.ativo = false;
	}

}