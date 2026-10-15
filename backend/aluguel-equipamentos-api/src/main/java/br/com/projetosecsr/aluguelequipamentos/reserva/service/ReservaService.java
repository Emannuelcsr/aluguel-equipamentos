package br.com.projetosecsr.aluguelequipamentos.reserva.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.Cliente;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ClienteNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.repository.ClienteRepository;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.ItemReserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.Reserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.StatusReserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.ClienteInativoParaReservaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.ConflitoDeReservaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.DataFinalInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.DataInicialInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.ReservaNaoEncontradaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.UnidadeDuplicadaNaReservaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.repository.ItemReservaRepository;
import br.com.projetosecsr.aluguelequipamentos.reserva.repository.ReservaRepository;
import br.com.projetosecsr.aluguelequipamentos.reserva.request.CadastrarReservaRequest;
import br.com.projetosecsr.aluguelequipamentos.reserva.response.ReservaResponse;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.StatusUnidadeEquipamento;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.UnidadeEquipamento;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoJaInativaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeIndisponivelParaReservaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.repository.UnidadeEquipamentoRepository;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.UsuarioJaAtivoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.response.UsuarioResponse;

@Service
public class ReservaService {

	private final ReservaRepository reservaRepository;

	private final ItemReservaRepository itemReservaRepository;

	private final ClienteRepository clienteRepository;

	private final UnidadeEquipamentoRepository unidadeEquipamentoRepository;

	public ReservaService(ReservaRepository reservaRepository, ItemReservaRepository itemReservaRepository,
			ClienteRepository clienteRepository, UnidadeEquipamentoRepository unidadeEquipamentoRepository) {
		this.reservaRepository = reservaRepository;
		this.itemReservaRepository = itemReservaRepository;
		this.clienteRepository = clienteRepository;
		this.unidadeEquipamentoRepository = unidadeEquipamentoRepository;
	}

	private PaginaResponse<ReservaResponse> converterPagina(Page<ReservaResponse> pagina) {

		if (pagina.getTotalPages() > 0 && pagina.getNumber() >= pagina.getTotalPages()) {

			throw new PaginaInvalidaException("A página informada não existe");
		}

		return new PaginaResponse<>(pagina.getContent(), pagina.getNumber() + 1, pagina.getSize(),
				pagina.getNumberOfElements(), pagina.getTotalElements(), pagina.getTotalPages(), pagina.isFirst(),
				pagina.isLast());
	}

	private Reserva buscarReservaPorId(Long id) {

		Optional<Reserva> reservaEncontrada = reservaRepository.findById(id);

		if (reservaEncontrada.isEmpty()) {

			throw new ReservaNaoEncontradaException();
		}

		Reserva reserva = reservaEncontrada.get();

		return reserva;
	}

	private void validarDatas(LocalDate dataInicial, LocalDate dataFinal) {

		if (dataInicial.isBefore(LocalDate.now())) {

			throw new DataInicialInvalidaException();
		}

		if (dataFinal.isBefore(dataInicial)) {

			throw new DataFinalInvalidaException();
		}

	}

	private void validarIdsDuplicados(List<Long> idsRecebidos) {

		Set<Long> idsUnicos = new HashSet<>(idsRecebidos);

		if (idsRecebidos.size() != idsUnicos.size()) {
			throw new UnidadeDuplicadaNaReservaException();
		}
	}

	private List<UnidadeEquipamento> buscarEValidarUnidades(List<Long> idsRecebidos) {

		List<UnidadeEquipamento> unidadesDoBanco = unidadeEquipamentoRepository.findAllById(idsRecebidos);

		List<Long> idsEncontrados = new ArrayList<>();

		for (UnidadeEquipamento unidadeDoBanco : unidadesDoBanco) {

			idsEncontrados.add(unidadeDoBanco.getId());

		}

		List<Long> idsNaoEncontrados = new ArrayList<>();

		for (Long idPedido : idsRecebidos) {

			if (!idsEncontrados.contains(idPedido)) {

				idsNaoEncontrados.add(idPedido);
			}

		}

		if (!idsNaoEncontrados.isEmpty()) {
			throw new UnidadeEquipamentoNaoEncontradoException(
					"Unidades de equipamento não encontradas: " + idsNaoEncontrados);
		}

		return unidadesDoBanco;

	}

	private void validarUnidades(List<UnidadeEquipamento> unidadesDoBanco, List<StatusReserva> statusBloqueadores,
			LocalDate dataInicial, LocalDate dataFinal) {

		for (UnidadeEquipamento unidadeDoBanco : unidadesDoBanco) {

			if (!unidadeDoBanco.isAtivo()) {

				throw new UnidadeEquipamentoJaInativaException();
			}

			if (unidadeDoBanco.getStatus() == StatusUnidadeEquipamento.EM_MANUTENCAO) {

				throw new UnidadeIndisponivelParaReservaException();
			}

			if (itemReservaRepository.existeConflito(unidadeDoBanco.getId(), dataInicial, dataFinal,
					statusBloqueadores)) {

				throw new ConflitoDeReservaException();

			}

		}

	}

	private void criarItensReserva(Reserva reserva, LocalDate dataInicial, LocalDate dataFinal,
			List<UnidadeEquipamento> unidadesDoBanco) {

		Long quantidadeDias = ChronoUnit.DAYS.between(dataInicial, dataFinal) + 1;

		for (UnidadeEquipamento unidadeDoBanco : unidadesDoBanco) {

			BigDecimal valorDiaria = unidadeDoBanco.getEquipamento().getValorDiaria();

			ItemReserva itemReserva = new ItemReserva(unidadeDoBanco, valorDiaria, quantidadeDias);

			reserva.adicionarItem(itemReserva);

		}
	}

	@Transactional
	public ReservaResponse cadastrar(CadastrarReservaRequest request) {

		Optional<Cliente> cliente = clienteRepository.findById(request.clienteId());

		if (cliente.isEmpty()) {

			throw new ClienteNaoEncontradoException();
		}

		Cliente clienteEncontrado = cliente.get();

		if (!clienteEncontrado.isAtivo()) {

			throw new ClienteInativoParaReservaException();
		}

		validarDatas(request.dataInicial(), request.dataFinal());

		validarIdsDuplicados(request.unidadesIds());

		List<UnidadeEquipamento> unidadesDoBanco = buscarEValidarUnidades(request.unidadesIds());

		List<StatusReserva> statusBloqueadores = List.of(StatusReserva.SOLICITADA, StatusReserva.APROVADA,
				StatusReserva.EM_ANDAMENTO);

		validarUnidades(unidadesDoBanco, statusBloqueadores, request.dataInicial(), request.dataFinal());

		Reserva reserva = new Reserva(clienteEncontrado, request.dataInicial(), request.dataFinal());

		criarItensReserva(reserva, request.dataInicial(), request.dataFinal(), unidadesDoBanco);

		reserva.calcularValorTotal();

		Reserva reservaSalva = reservaRepository.save(reserva);

		return ReservaResponse.de(reservaSalva);
	}

	@Transactional(readOnly = true)
	public ReservaResponse buscarPorId(Long id) {

		Reserva reserva = buscarReservaPorId(id);

		ReservaResponse resposta = ReservaResponse.de(reserva);

		return resposta;

	}

	@Transactional(readOnly = true)
	public PaginaResponse<ReservaResponse> listarPaginado(Pageable paginacao) {

		Page<Reserva> paginaDeReservas = reservaRepository.findAll(paginacao);

		Page<ReservaResponse> paginaDeRespostas = paginaDeReservas.map(reserva -> ReservaResponse.de(reserva));

		return converterPagina(paginaDeRespostas);

	}

	@Transactional
	public ReservaResponse aprovar(Long id) {

		Reserva reserva = buscarReservaPorId(id);

		reserva.aprovar();

		return ReservaResponse.de(reserva);
	}

	@Transactional
	public ReservaResponse rejeitar(Long id) {

		Reserva reserva = buscarReservaPorId(id);

		reserva.rejeitar();

		return ReservaResponse.de(reserva);
	}

	@Transactional
	public ReservaResponse cancelar(Long id) {

		Reserva reserva = buscarReservaPorId(id);

		reserva.cancelar();

		return ReservaResponse.de(reserva);
	}

}
