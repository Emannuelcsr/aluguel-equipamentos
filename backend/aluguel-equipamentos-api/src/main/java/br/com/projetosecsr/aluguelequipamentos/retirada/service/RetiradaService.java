
package br.com.projetosecsr.aluguelequipamentos.retirada.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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
import br.com.projetosecsr.aluguelequipamentos.reserva.controller.ReservaController;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.ItemReserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.Reserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.StatusReserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.ReservaNaoEncontradaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.repository.ItemReservaRepository;
import br.com.projetosecsr.aluguelequipamentos.reserva.repository.ReservaRepository;
import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.CondicaoFisicaRetirada;
import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.ItemRetirada;
import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.MetodoVerificacaoAutorizacao;
import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.Retirada;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.ClienteInativoParaRetiradaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.ConfirmacaoRetiradaObrigatoriaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.DescricaoMetodoVerificacaoObrigatoriaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.ItemRetiradaDuplicadoException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.ItensRetiradaDivergentesException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.ObservacaoObrigatoriaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.ReservaNaoAprovadaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.RetiradaForaDoPeriodoException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.RetiradaJaRealizadaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.RetiradaNaoEncontradaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.UnidadeInativaParaRetiradaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.UnidadeIndisponivelParaRetiradaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.repository.RetiradaRepository;
import br.com.projetosecsr.aluguelequipamentos.retirada.request.CadastrarRetiradaRequest;
import br.com.projetosecsr.aluguelequipamentos.retirada.request.ItemRetiradaRequest;
import br.com.projetosecsr.aluguelequipamentos.retirada.response.RetiradaResponse;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.StatusUnidadeEquipamento;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.UsuarioNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.repository.UsuarioRepository;

@Service
public class RetiradaService {

	private final ReservaController reservaController;

	private final RetiradaRepository retiradaRepository;
	private final ItemReservaRepository itemReservaRepository;
	private final UsuarioRepository usuarioRepository;
	private final ReservaRepository reservaRepository;

	public RetiradaService(RetiradaRepository retiradaRepository, ItemReservaRepository itemReservaRepository,
			UsuarioRepository usuarioRepository, ReservaRepository reservaRepository,
			ReservaController reservaController) {

		this.retiradaRepository = retiradaRepository;
		this.itemReservaRepository = itemReservaRepository;
		this.usuarioRepository = usuarioRepository;
		this.reservaRepository = reservaRepository;
		this.reservaController = reservaController;
	}

	private PaginaResponse<RetiradaResponse> converterPagina(Page<RetiradaResponse> pagina) {

		if (pagina.getTotalPages() > 0 && pagina.getNumber() >= pagina.getTotalPages()) {

			throw new PaginaInvalidaException("A página informada não existe");
		}

		return new PaginaResponse<>(pagina.getContent(), pagina.getNumber() + 1, pagina.getSize(),
				pagina.getNumberOfElements(), pagina.getTotalElements(), pagina.getTotalPages(), pagina.isFirst(),
				pagina.isLast());
	}

	private Retirada buscarRetiradaPorId(Long id) {

		Optional<Retirada> retiradaEncontrada = retiradaRepository.findById(id);

		if (retiradaEncontrada.isEmpty()) {

			throw new RetiradaNaoEncontradaException();
		}

		Retirada retirada = retiradaEncontrada.get();

		return retirada;
	}

	private Reserva validarReserva(Long reservaId) {

		Optional<Reserva> reserva = reservaRepository.buscarPorIdComBloqueio(reservaId);

		if (reserva.isEmpty()) {

			throw new ReservaNaoEncontradaException();
		}

		Reserva reservaEncontrada = reserva.get();

		if (reservaEncontrada.getStatus() != StatusReserva.APROVADA) {

			throw new ReservaNaoAprovadaException();
		}

		return reservaEncontrada;
	}

	private void validarPeriodoRetirada(Reserva reserva) {

		LocalDate hoje = LocalDate.now();

		if ((reserva.getDataInicial().isAfter(hoje)) || (reserva.getDataFinal().isBefore(hoje))) {

			throw new RetiradaForaDoPeriodoException();
		}

	}

	private void validarCliente(Reserva reserva) {

		if (!reserva.getCliente().isAtivo()) {

			throw new ClienteInativoParaRetiradaException();
		}

	}

	private void validarRetiradaExistente(Long reservaId) {

		if (retiradaRepository.existsByReservaId(reservaId)) {

			throw new RetiradaJaRealizadaException();

		}

	}

	private void validarDisponibilidadeUnidadesDaReserva(Reserva reserva) {

		for (ItemReserva item : reserva.getItens()) {

			if (!item.getUnidadeEquipamento().isAtivo()) {

				throw new UnidadeInativaParaRetiradaException();

			}

			if (item.getUnidadeEquipamento().getStatus() != StatusUnidadeEquipamento.DISPONIVEL) {

				throw new UnidadeIndisponivelParaRetiradaException();

			}

		}

	}

	private void validarItensDaRetirada(Reserva reserva, CadastrarRetiradaRequest request) {

		Set<Long> idsEnviados = request.itens().stream().map(item -> item.itemReservaId()).collect(Collectors.toSet());

		if (request.itens().size() != idsEnviados.size()) {
			throw new ItemRetiradaDuplicadoException();
		}

		Set<Long> idsReservados = reserva.getItens().stream().map(item -> item.getId()).collect(Collectors.toSet());

		if (!idsEnviados.equals(idsReservados)) {
			throw new ItensRetiradaDivergentesException();
		}
	}

	private void validarCondicaoFisica(CadastrarRetiradaRequest request) {

		for (ItemRetiradaRequest itemRetirada : request.itens()) {

			if (itemRetirada.condicaoFisica() == CondicaoFisicaRetirada.COM_OBSERVACOES
					&& (itemRetirada.observacoes() == null || itemRetirada.observacoes().isBlank())) {

				throw new ObservacaoObrigatoriaException();

			}

		}

	}

	private void validarMetodoVerificacao(CadastrarRetiradaRequest request) {

		if (request.metodoVerificacao() == MetodoVerificacaoAutorizacao.OUTRO
				&& (request.descricaoOutroMetodo() == null || request.descricaoOutroMetodo().isBlank())) {

			throw new DescricaoMetodoVerificacaoObrigatoriaException();

		}

	}

	private void validarConfirmacoes(CadastrarRetiradaRequest request) {

		if (!Boolean.TRUE.equals(request.autorizacaoConfirmada())
				|| !Boolean.TRUE.equals(request.recebimentoConfirmado())) {

			throw new ConfirmacaoRetiradaObrigatoriaException();

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
	public RetiradaResponse cadastrar(CadastrarRetiradaRequest request) {

		Reserva reserva = validarReserva(request.reservaId());

		validarPeriodoRetirada(reserva);

		validarCliente(reserva);

		validarRetiradaExistente(reserva.getId());

		validarDisponibilidadeUnidadesDaReserva(reserva);

		validarItensDaRetirada(reserva, request);

		validarCondicaoFisica(request);

		validarMetodoVerificacao(request);

		validarConfirmacoes(request);

		Usuario usuarioResponsavel = buscarUsuarioAutenticado();

		OffsetDateTime dataHoraRetirada = OffsetDateTime.now();

		Retirada retirada = new Retirada(dataHoraRetirada, request.nomeRecebedor(), request.documentoRecebedor(),
				request.metodoVerificacao(), request.descricaoOutroMetodo(), reserva, usuarioResponsavel);

		for (ItemRetiradaRequest item : request.itens()) {

			for (ItemReserva itemReserva : reserva.getItens()) {

				if (item.itemReservaId().equals(itemReserva.getId())) {

					ItemRetirada itemRetirada = new ItemRetirada(itemReserva, item.condicaoFisica(),
							item.observacoes());

					retirada.adicionarItem(itemRetirada);

				}

			}

		}

		retiradaRepository.save(retirada);

		reserva.iniciar();

		for (ItemReserva itemReserva : reserva.getItens()) {

			itemReserva.getUnidadeEquipamento().alugar();

		}

		return RetiradaResponse.de(retirada);

	}

	@Transactional(readOnly = true)
	public RetiradaResponse buscarPorId(Long id) {

		Retirada retirada = buscarRetiradaPorId(id);

		RetiradaResponse resposta = RetiradaResponse.de(retirada);

		return resposta;

	}

	@Transactional(readOnly = true)
	public PaginaResponse<RetiradaResponse> listarPaginado(Pageable paginacao) {

		Page<Retirada> paginaDeRetiradas = retiradaRepository.findAll(paginacao);

		Page<RetiradaResponse> paginaDeRespostas = paginaDeRetiradas.map(retirada -> RetiradaResponse.de(retirada));

		return converterPagina(paginaDeRespostas);

	}

}
