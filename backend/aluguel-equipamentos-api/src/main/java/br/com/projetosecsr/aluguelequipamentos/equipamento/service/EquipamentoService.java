package br.com.projetosecsr.aluguelequipamentos.equipamento.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projetosecsr.aluguelequipamentos.categoria.entidade.Categoria;
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaJaInativaException;
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.categoria.repository.CategoriaRepository;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.entidade.Equipamento;
import br.com.projetosecsr.aluguelequipamentos.equipamento.excecao.EquipamentoJaAtivoException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.excecao.EquipamentoJaInativoException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.excecao.EquipamentoNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.repository.EquipamentoRepository;
import br.com.projetosecsr.aluguelequipamentos.equipamento.request.AtualizarEquipamentoRequest;
import br.com.projetosecsr.aluguelequipamentos.equipamento.request.CadastrarEquipamentoRequest;
import br.com.projetosecsr.aluguelequipamentos.equipamento.response.EquipamentoResponse;

@Service
public class EquipamentoService {

	private final EquipamentoRepository equipamentoRepository;

	private final CategoriaRepository categoriaRepository;

	public EquipamentoService(EquipamentoRepository equipamentoRepository, CategoriaRepository categoriaRepository) {
		this.equipamentoRepository = equipamentoRepository;
		this.categoriaRepository = categoriaRepository;
	}

	private PaginaResponse<EquipamentoResponse> converterPagina(Page<EquipamentoResponse> pagina) {

		if (pagina.getTotalPages() > 0 && pagina.getNumber() >= pagina.getTotalPages()) {

			throw new PaginaInvalidaException("A página informada não existe");
		}

		return new PaginaResponse<>(pagina.getContent(), pagina.getNumber() + 1, pagina.getSize(),
				pagina.getNumberOfElements(), pagina.getTotalElements(), pagina.getTotalPages(), pagina.isFirst(),
				pagina.isLast());
	}

	private Equipamento buscarEquipamentoPorId(Long id) {

		Optional<Equipamento> equipamentoEncontrado = equipamentoRepository.findById(id);

		if (equipamentoEncontrado.isEmpty()) {

			throw new EquipamentoNaoEncontradoException();
		}

		Equipamento equipamento = equipamentoEncontrado.get();

		return equipamento;
	}

	private String normalizarNome(String nome) {

		String nomeNormalizado = nome.strip();

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

	@Transactional
	public EquipamentoResponse cadastrar(CadastrarEquipamentoRequest request) {

		String nomeNormalizado = normalizarNome(request.nome());

		String descricaoNormalizado = normalizarDescricao(request.descricao());

		Optional<Categoria> categoria = categoriaRepository.findById(request.categoriaId());

		if (categoria.isEmpty()) {

			throw new CategoriaNaoEncontradoException();
		}

		Categoria categoriaEncontrada = categoria.get();

		if (!categoriaEncontrada.isAtivo()) {

			throw new CategoriaJaInativaException();
		}

		Equipamento equipamento = new Equipamento(nomeNormalizado, descricaoNormalizado, request.valorDiaria(),
				categoriaEncontrada);

		Equipamento equipamentoSalvo = equipamentoRepository.save(equipamento);

		return EquipamentoResponse.de(equipamentoSalvo);
	}

	@Transactional(readOnly = true)
	public EquipamentoResponse buscarPorId(Long id) {

		Equipamento equipamento = buscarEquipamentoPorId(id);

		EquipamentoResponse resposta = EquipamentoResponse.de(equipamento);

		return resposta;

	}

	@Transactional(readOnly = true)
	public PaginaResponse<EquipamentoResponse> listarPaginado(Pageable paginacao) {

		Page<Equipamento> paginaDeEquipamentos = equipamentoRepository.findAll(paginacao);

		Page<EquipamentoResponse> paginaDeRespostas = paginaDeEquipamentos
				.map(equipamento -> EquipamentoResponse.de(equipamento));

		return converterPagina(paginaDeRespostas);

	}

	@Transactional
	public EquipamentoResponse atualizar(Long id, AtualizarEquipamentoRequest request) {

		Equipamento equipamento = buscarEquipamentoPorId(id);

		String nomeNormalizado = normalizarNome(request.nome());

		String descricaoNormalizado = normalizarDescricao(request.descricao());

		Optional<Categoria> categoria = categoriaRepository.findById(request.categoriaId());

		if (categoria.isEmpty()) {

			throw new CategoriaNaoEncontradoException();
		}

		Categoria categoriaEncontrada = categoria.get();

		if (!categoriaEncontrada.isAtivo()) {

			throw new CategoriaJaInativaException();
		}

		equipamento.atualizarDados(nomeNormalizado, descricaoNormalizado, request.valorDiaria(), categoriaEncontrada);

		return EquipamentoResponse.de(equipamento);
	}

	@Transactional
	public EquipamentoResponse ativar(Long id) {

		Equipamento equipamento = buscarEquipamentoPorId(id);

		if (equipamento.isAtivo()) {

			throw new EquipamentoJaAtivoException();
		}

		equipamento.ativar();

		return EquipamentoResponse.de(equipamento);
	}

	@Transactional
	public EquipamentoResponse desativar(Long id) {

		Equipamento equipamento = buscarEquipamentoPorId(id);

		if (!equipamento.isAtivo()) {

			throw new EquipamentoJaInativoException();
		}

		equipamento.desativar();

		return EquipamentoResponse.de(equipamento);
	}

}
