package br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.entidade.Equipamento;
import br.com.projetosecsr.aluguelequipamentos.equipamento.excecao.EquipamentoJaInativoException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.excecao.EquipamentoNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.repository.EquipamentoRepository;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.UnidadeEquipamento;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoJaAtivaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoJaInativaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.repository.UnidadeEquipamentoRepository;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.request.CadastrarUnidadeEquipamentoRequest;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.response.UnidadeEquipamentoResponse;

@Service
public class UnidadeEquipamentoService {

	private final UnidadeEquipamentoRepository unidadeEquipamentoRepository;

	private final EquipamentoRepository equipamentoRepository;

	public UnidadeEquipamentoService(UnidadeEquipamentoRepository unidadeEquipamentoRepository,
			EquipamentoRepository equipamentoRepository) {
		this.unidadeEquipamentoRepository = unidadeEquipamentoRepository;
		this.equipamentoRepository = equipamentoRepository;
	}

	private PaginaResponse<UnidadeEquipamentoResponse> converterPagina(Page<UnidadeEquipamentoResponse> pagina) {

		if (pagina.getTotalPages() > 0 && pagina.getNumber() >= pagina.getTotalPages()) {

			throw new PaginaInvalidaException("A página informada não existe");
		}

		return new PaginaResponse<>(pagina.getContent(), pagina.getNumber() + 1, pagina.getSize(),
				pagina.getNumberOfElements(), pagina.getTotalElements(), pagina.getTotalPages(), pagina.isFirst(),
				pagina.isLast());
	}

	private UnidadeEquipamento buscarUnidadeEquipamentoPorId(Long id) {

		Optional<UnidadeEquipamento> unidadeEquipamentoEncontrada = unidadeEquipamentoRepository.findById(id);

		if (unidadeEquipamentoEncontrada.isEmpty()) {

			throw new UnidadeEquipamentoNaoEncontradoException();
		}

		UnidadeEquipamento unidadeEquipamento = unidadeEquipamentoEncontrada.get();

		return unidadeEquipamento;
	}

	@Transactional
	public UnidadeEquipamentoResponse cadastrar(CadastrarUnidadeEquipamentoRequest request) {

		Optional<Equipamento> equipamentoEncontrado = equipamentoRepository.findById(request.equipamentoId());

		if (equipamentoEncontrado.isEmpty()) {

			throw new EquipamentoNaoEncontradoException();
		}

		Equipamento equipamento = equipamentoEncontrado.get();

		if (!equipamento.isAtivo()) {

			throw new EquipamentoJaInativoException();
		}

		UnidadeEquipamento unidadeEquipamento = new UnidadeEquipamento(equipamento);

		UnidadeEquipamento unidadeSalva = unidadeEquipamentoRepository.save(unidadeEquipamento);

		return UnidadeEquipamentoResponse.de(unidadeSalva);

	}

	@Transactional(readOnly = true)
	public UnidadeEquipamentoResponse buscarPorId(Long id) {

		UnidadeEquipamento unidadeEquipamento = buscarUnidadeEquipamentoPorId(id);

		UnidadeEquipamentoResponse resposta = UnidadeEquipamentoResponse.de(unidadeEquipamento);

		return resposta;
	}

	@Transactional(readOnly = true)
	public PaginaResponse<UnidadeEquipamentoResponse> listarPaginado(Pageable paginacao) {

		Page<UnidadeEquipamento> paginaDeUnidades = unidadeEquipamentoRepository.findAll(paginacao);

		Page<UnidadeEquipamentoResponse> paginaDeRespostas = paginaDeUnidades
				.map(unidadeEquipamento -> UnidadeEquipamentoResponse.de(unidadeEquipamento));

		return converterPagina(paginaDeRespostas);

	}

	@Transactional
	public UnidadeEquipamentoResponse ativar(Long id) {

		UnidadeEquipamento unidadeEquipamento = buscarUnidadeEquipamentoPorId(id);

		if (unidadeEquipamento.isAtivo()) {

			throw new UnidadeEquipamentoJaAtivaException();
		}

		unidadeEquipamento.ativar();

		return UnidadeEquipamentoResponse.de(unidadeEquipamento);
	}

	@Transactional
	public UnidadeEquipamentoResponse desativar(Long id) {

		UnidadeEquipamento unidadeEquipamento = buscarUnidadeEquipamentoPorId(id);

		if (!unidadeEquipamento.isAtivo()) {

			throw new UnidadeEquipamentoJaInativaException();
		}

		unidadeEquipamento.desativar();

		return UnidadeEquipamentoResponse.de(unidadeEquipamento);
	}

	@Transactional
	public UnidadeEquipamentoResponse enviarParaManutencao(Long id) {

		UnidadeEquipamento unidadeEquipamento = buscarUnidadeEquipamentoPorId(id);

		unidadeEquipamento.enviarParaManutencao();

		return UnidadeEquipamentoResponse.de(unidadeEquipamento);
	}

	@Transactional
	public UnidadeEquipamentoResponse liberarDaManutencao(Long id) {

		UnidadeEquipamento unidadeEquipamento = buscarUnidadeEquipamentoPorId(id);

		unidadeEquipamento.liberarDaManutencao();

		return UnidadeEquipamentoResponse.de(unidadeEquipamento);
	}

}
