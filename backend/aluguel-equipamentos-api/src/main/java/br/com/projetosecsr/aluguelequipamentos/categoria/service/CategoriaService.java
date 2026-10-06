package br.com.projetosecsr.aluguelequipamentos.categoria.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projetosecsr.aluguelequipamentos.categoria.entidade.Categoria;
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaJaAtivaException;
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaJaCadastradaException;
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaJaInativaException;
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.categoria.repository.CategoriaRepository;
import br.com.projetosecsr.aluguelequipamentos.categoria.request.AtualizarCategoriaRequest;
import br.com.projetosecsr.aluguelequipamentos.categoria.request.CadastrarCategoriaRequest;
import br.com.projetosecsr.aluguelequipamentos.categoria.response.CategoriaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;

@Service
public class CategoriaService {

	private final CategoriaRepository categoriaRepository;

	public CategoriaService(CategoriaRepository categoriaRepository) {
		this.categoriaRepository = categoriaRepository;
	}

	private String normalizarNome(String nome) {

		String nomeNormalizado = nome.strip().toUpperCase(Locale.ROOT);

		return nomeNormalizado;
	}

	private String normalizarDescricao(String descricao) {

		if (descricao == null) {

			return null;
		}

		String descricaoNormalizado = descricao.strip();

		if (descricaoNormalizado.isBlank()) {
			return null;
		}

		return descricaoNormalizado;

	}

	private Categoria buscarCategoriaPorId(Long id) {

		Optional<Categoria> categoriaEncontrado = categoriaRepository.findById(id);

		if (categoriaEncontrado.isEmpty()) {
			throw new CategoriaNaoEncontradoException();
		}

		Categoria categoria = categoriaEncontrado.get();

		return categoria;
	}

	private PaginaResponse<CategoriaResponse> converterPagina(Page<CategoriaResponse> pagina) {

		if (pagina.getTotalPages() > 0 && pagina.getNumber() >= pagina.getTotalPages()) {

			throw new PaginaInvalidaException("A página informada não existe");
		}

		return new PaginaResponse<>(pagina.getContent(), pagina.getNumber() + 1, pagina.getSize(),
				pagina.getNumberOfElements(), pagina.getTotalElements(), pagina.getTotalPages(), pagina.isFirst(),
				pagina.isLast());
	}

	// ================================== metodos
	// publicos=================================================

	@Transactional
	public CategoriaResponse cadastrar(CadastrarCategoriaRequest request) {

		String nome = normalizarNome(request.nome());

		String descricao = normalizarDescricao(request.descricao());

		if (categoriaRepository.existsByNome(nome)) {
			throw new CategoriaJaCadastradaException();
		}

		Categoria categoria = new Categoria(nome, descricao);

		Categoria categoriaSalva = categoriaRepository.save(categoria);

		return CategoriaResponse.de(categoriaSalva);
	}

	@Transactional(readOnly = true)
	public CategoriaResponse buscarPorId(Long id) {

		Categoria categoria = buscarCategoriaPorId(id);

		CategoriaResponse resposta = CategoriaResponse.de(categoria);

		return resposta;
	}

	@Transactional(readOnly = true)
	public PaginaResponse<CategoriaResponse> listarPaginado(Pageable paginacao) {

		Page<Categoria> paginaDeCategorias = categoriaRepository.findAll(paginacao);

		Page<CategoriaResponse> paginaDeRespostas = paginaDeCategorias
				.map(categoria -> CategoriaResponse.de(categoria));

		return converterPagina(paginaDeRespostas);

	}

	@Transactional
	public CategoriaResponse atualizar(AtualizarCategoriaRequest request, Long id) {

		Categoria categoria = buscarCategoriaPorId(id);

		String nome = normalizarNome(request.nome());

		String descricao = normalizarDescricao(request.descricao());

		if (categoriaRepository.existsByNomeAndIdNot(nome, id)) {
			throw new CategoriaJaCadastradaException();
		}

		categoria.atualizarDados(nome, descricao);

		return CategoriaResponse.de(categoria);
	}

	@Transactional
	public CategoriaResponse ativar(Long id) {

		Categoria categoria = buscarCategoriaPorId(id);

		if (categoria.isAtivo()) {

			throw new CategoriaJaAtivaException();
		}

		categoria.ativar();

		return CategoriaResponse.de(categoria);
	}

	@Transactional
	public CategoriaResponse desativar(Long id) {

		Categoria categoria = buscarCategoriaPorId(id);

		if (!categoria.isAtivo()) {
			throw new CategoriaJaInativaException();
		}

		categoria.desativar();

		return CategoriaResponse.de(categoria);
	}

}
