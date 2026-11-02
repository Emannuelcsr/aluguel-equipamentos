package br.com.projetosecsr.aluguelequipamentos.devolucao.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.entidade.CondicaoFisicaDevolucao;
import br.com.projetosecsr.aluguelequipamentos.devolucao.entidade.Devolucao;
import br.com.projetosecsr.aluguelequipamentos.devolucao.entidade.ItemDevolucao;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.ConfirmacaoDevolucaoObrigatoriaException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.DevolucaoAnteriorRetiradaException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.DevolucaoAntesDataInicialException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.DevolucaoJaRealizadaException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.DevolucaoNaoEncontradaException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.ItemDevolucaoDuplicadoException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.ItensDevolucaoDivergentesException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.ObservacaoObrigatoriaException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.StatusUnidadeInvalidoParaDevolucaoException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.repository.DevolucaoRepository;
import br.com.projetosecsr.aluguelequipamentos.devolucao.request.CadastrarDevolucaoRequest;
import br.com.projetosecsr.aluguelequipamentos.devolucao.request.ItemDevolucaoRequest;
import br.com.projetosecsr.aluguelequipamentos.devolucao.response.DevolucaoResponse;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.ItemReserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.Reserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.StatusReserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.OperacaoReservaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.ReservaNaoEncontradaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.repository.ReservaRepository;
import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.Retirada;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.RetiradaNaoEncontradaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.repository.RetiradaRepository;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.StatusUnidadeEquipamento;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.UnidadeEquipamento;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.OperacaoInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.UsuarioNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.repository.UsuarioRepository;
import br.com.projetosecsr.aluguelequipamentos.usuario.response.UsuarioResponse;

@Service
public class DevolucaoService {

	private final DevolucaoRepository devolucaoRepository;

	private final ReservaRepository reservaRepository;

	private final UsuarioRepository usuarioRepository;

	private final RetiradaRepository retiradaRepository;

	public DevolucaoService(DevolucaoRepository devolucaoRepository, ReservaRepository reservaRepository,
			UsuarioRepository usuarioRepository, RetiradaRepository retiradaRepository) {
		this.devolucaoRepository = devolucaoRepository;
		this.reservaRepository = reservaRepository;
		this.usuarioRepository = usuarioRepository;
		this.retiradaRepository = retiradaRepository;
	}

	private PaginaResponse<DevolucaoResponse> converterPagina(Page<DevolucaoResponse> pagina) {

		if (pagina.getTotalPages() > 0 && pagina.getNumber() >= pagina.getTotalPages()) {

			throw new PaginaInvalidaException("A página informada não existe");
		}

		return new PaginaResponse<>(pagina.getContent(), pagina.getNumber() + 1, pagina.getSize(),
				pagina.getNumberOfElements(), pagina.getTotalElements(), pagina.getTotalPages(), pagina.isFirst(),
				pagina.isLast());
	}

	private Devolucao buscarDevolucaoPorId(Long id) {

		Optional<Devolucao> devolucao = devolucaoRepository.findById(id);

		if (devolucao.isEmpty()) {

			throw new DevolucaoNaoEncontradaException();
		}

		Devolucao devolucaoEncontrada = devolucao.get();

		return devolucaoEncontrada;
	}

	private void validarConfirmacaoRecebimento(CadastrarDevolucaoRequest request) {

		if (!Boolean.TRUE.equals(request.confirmacaoRecebimento())) {
			throw new ConfirmacaoDevolucaoObrigatoriaException();
		}
	}

	private Reserva validarReserva(CadastrarDevolucaoRequest request) {

		Optional<Reserva> reserva = reservaRepository.buscarPorIdComBloqueio(request.reservaId());

		if (reserva.isEmpty()) {

			throw new ReservaNaoEncontradaException();
		}

		Reserva reservaEncontrada = reserva.get();

		if (reservaEncontrada.getStatus() != StatusReserva.EM_ANDAMENTO) {

			throw new OperacaoReservaInvalidaException("A situação da reserva deve estar EM ANDAMENTO");
		}

		return reservaEncontrada;
	}

	private Retirada validarExistenciaRetirada(Reserva reserva) {

		Optional<Retirada> retirada = retiradaRepository.findByReservaId(reserva.getId());

		if (retirada.isEmpty()) {

			throw new RetiradaNaoEncontradaException();
		}

		Retirada retiradaEncontrada = retirada.get();

		return retiradaEncontrada;
	}

	private void validarDevolucaoExistente(Reserva reserva) {

		if (devolucaoRepository.existsByReservaId(reserva.getId())) {

			throw new DevolucaoJaRealizadaException();
		}

	}

	private void validarItensDevolucao(CadastrarDevolucaoRequest request, Reserva reserva) {

		Set<Long> idsEnviados = request.itens().stream().map(item -> item.itemReservaId()).collect(Collectors.toSet());

		if (request.itens().size() != idsEnviados.size()) {
			throw new ItemDevolucaoDuplicadoException();
		}

		Set<Long> idsReservados = reserva.getItens().stream().map(item -> item.getId()).collect(Collectors.toSet());

		if (!idsEnviados.equals(idsReservados)) {
			throw new ItensDevolucaoDivergentesException();
		}

	}

	private void validarStatusUnidades(Reserva reserva) {

		for (ItemReserva item : reserva.getItens()) {

			if (item.getUnidadeEquipamento().getStatus() != StatusUnidadeEquipamento.ALUGADO) {

				throw new StatusUnidadeInvalidoParaDevolucaoException(item.getUnidadeEquipamento().getCodigo());
			}
		}

	}

	private void validarCondicaoFisica(CadastrarDevolucaoRequest request) {

		for (ItemDevolucaoRequest item : request.itens()) {

			if (item.condicaoFisica() == CondicaoFisicaDevolucao.COM_OBSERVACOES
					&& (item.observacoes() == null || item.observacoes().isBlank())) {

				throw new ObservacaoObrigatoriaException();

			}

		}

	}

	private void validarDataDevolucao(Reserva reserva, OffsetDateTime dataHoraDevolucao) {

		LocalDate dataDevolucao = dataHoraDevolucao.toLocalDate();

		if (dataDevolucao.isBefore(reserva.getDataInicial())) {
			throw new DevolucaoAntesDataInicialException();
		}

	}

	private void validarDataHoraDevolucao(Retirada retirada, OffsetDateTime dataHoraDevolucao) {

		OffsetDateTime dataHoraRetirada = retirada.getDataHoraRetirada();

		if (dataHoraDevolucao.isBefore(dataHoraRetirada)) {
			throw new DevolucaoAnteriorRetiradaException();
		}

	}

	private Usuario buscarUsuarioAutenticado() {

		Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

		Long usuarioId = Long.parseLong(autenticacao.getName());

		Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);

		if (usuario.isEmpty()) {

			throw new UsuarioNaoEncontradoException();
		}

		Usuario usuarioEncontrado = usuario.get();

		return usuarioEncontrado;

	}

	@Transactional
	public DevolucaoResponse cadastrar(CadastrarDevolucaoRequest request) {

		OffsetDateTime dataHoraDevolucao = OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"));

		// 1. Validar confirmação de recebimento
		validarConfirmacaoRecebimento(request);

		// 2. Buscar e validar reserva com bloqueio pessimista
		Reserva reserva = validarReserva(request);

		// 3. Validar a data da devolução
		validarDataDevolucao(reserva, dataHoraDevolucao);

		// 4. Buscar e validar retirada anterior
		Retirada retirada = validarExistenciaRetirada(reserva);

		// 5. Verificar se já existe devolução
		validarDevolucaoExistente(reserva);

		// 6. Validar itens informados
		validarItensDevolucao(request, reserva);

		// 7. Validar status das unidades
		validarStatusUnidades(reserva);

		// 8. Validar condições físicas
		validarCondicaoFisica(request);

		// 9. Validar data e hora da retirada
		validarDataHoraDevolucao(retirada, dataHoraDevolucao);

		// 10. Identificar usuário responsável
		Usuario usuarioResponsavel = buscarUsuarioAutenticado();

		// 11. Criar a devolução
		Devolucao devolucao = new Devolucao(dataHoraDevolucao, reserva, usuarioResponsavel);

		// 12. Criar os itens da devolução e atualizar as unidades

		for (ItemDevolucaoRequest itemEnviadoNaRequisicao : request.itens()) {

			for (ItemReserva itemCadastradoNaReserva : reserva.getItens()) {

				if (itemEnviadoNaRequisicao.itemReservaId().equals(itemCadastradoNaReserva.getId())) {

					String observacoesEnviadas = itemEnviadoNaRequisicao.observacoes();

					if (observacoesEnviadas != null) {

						observacoesEnviadas = observacoesEnviadas.strip();

						if (observacoesEnviadas.isEmpty()) {

							observacoesEnviadas = null;
						}
					}

					ItemDevolucao novoItemDevolucao = new ItemDevolucao(itemCadastradoNaReserva,
							itemEnviadoNaRequisicao.condicaoFisica(), observacoesEnviadas);

					devolucao.adicionarItem(novoItemDevolucao);

					UnidadeEquipamento unidadeAlugada = itemCadastradoNaReserva.getUnidadeEquipamento();

					if (itemEnviadoNaRequisicao.condicaoFisica() == CondicaoFisicaDevolucao.SEM_DANOS_APARENTES) {

						unidadeAlugada.devolverSemDanos();

					} else if (itemEnviadoNaRequisicao.condicaoFisica() == CondicaoFisicaDevolucao.COM_OBSERVACOES) {

						unidadeAlugada.devolverParaManutencao();
					}

					break;
				}
			}
		}

		reserva.concluir();

		Devolucao devolucaoSalva = devolucaoRepository.save(devolucao);

		return DevolucaoResponse.de(devolucaoSalva);
	}

	@Transactional(readOnly = true)
	public DevolucaoResponse buscarPorId(Long id) {

		Devolucao devolucao = buscarDevolucaoPorId(id);

		DevolucaoResponse resposta = DevolucaoResponse.de(devolucao);

		return resposta;

	}

	@Transactional(readOnly = true)
	public PaginaResponse<DevolucaoResponse> listarPaginado(Pageable paginacao) {

		Page<Devolucao> paginaDeDevolucoes = devolucaoRepository.findAll(paginacao);

		Page<DevolucaoResponse> paginaDeRespostas = paginaDeDevolucoes
				.map(devolucao -> DevolucaoResponse.de(devolucao));

		return converterPagina(paginaDeRespostas);

	}

}
