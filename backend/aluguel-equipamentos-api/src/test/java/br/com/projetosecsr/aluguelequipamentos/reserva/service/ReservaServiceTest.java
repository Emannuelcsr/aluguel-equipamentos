package br.com.projetosecsr.aluguelequipamentos.reserva.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.projetosecsr.aluguelequipamentos.categoria.entidade.Categoria;
import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.Cliente;
import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.TipoCliente;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ClienteNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.repository.ClienteRepository;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.equipamento.entidade.Equipamento;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.Reserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.ClienteInativoParaReservaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.ConflitoDeReservaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.DataFinalInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.DataInicialInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.OperacaoReservaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.ReservaNaoEncontradaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.UnidadeDuplicadaNaReservaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.repository.ItemReservaRepository;
import br.com.projetosecsr.aluguelequipamentos.reserva.repository.ReservaRepository;
import br.com.projetosecsr.aluguelequipamentos.reserva.request.CadastrarReservaRequest;
import br.com.projetosecsr.aluguelequipamentos.reserva.response.ReservaResponse;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.UnidadeEquipamento;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoJaInativaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeIndisponivelParaReservaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.repository.UnidadeEquipamentoRepository;

@ExtendWith(MockitoExtension.class)
public class ReservaServiceTest {

	@Mock
	private ReservaRepository reservaRepository;

	@Mock
	private ItemReservaRepository itemReservaRepository;

	@Mock
	private ClienteRepository clienteRepository;

	@Mock
	private UnidadeEquipamentoRepository unidadeEquipamentoRepository;

	@InjectMocks
	private ReservaService reservaService;

	@Test
	void deveCadastrarReservaComSucesso() {

		Long clienteId = 1L;
		List<Long> unidadesIds = List.of(10L, 20L, 30L);
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = LocalDate.now().plusDays(3);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long categoriaId = 1L;
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");
		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		Long equipamentoId = 2L;
		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira de 1 metro",
				new BigDecimal("900.00"), categoria);
		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);

		UnidadeEquipamento unidade1 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade1, "id", 10L);

		UnidadeEquipamento unidade2 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade2, "id", 20L);

		UnidadeEquipamento unidade3 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade3, "id", 30L);

		List<UnidadeEquipamento> unidadesDoBanco = List.of(unidade1, unidade2, unidade3);

		CadastrarReservaRequest reserva = new CadastrarReservaRequest(clienteId, dataInicial, dataFinal, unidadesIds);

		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
		when(unidadeEquipamentoRepository.findAllById(unidadesIds)).thenReturn(unidadesDoBanco);
		when(itemReservaRepository.existeConflito(anyLong(), eq(dataInicial), eq(dataFinal), anyList()))
				.thenReturn(false);

		when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

		// EXECUTAR
		ReservaResponse resposta = reservaService.cadastrar(reserva);

		assertNotNull(resposta);
		assertEquals("SOLICITADA", resposta.status());
		assertEquals(3, resposta.itens().size());
		assertEquals(new BigDecimal("10800.00"), resposta.valorTotal());

		verify(reservaRepository, times(1)).save(any(Reserva.class));
		verify(itemReservaRepository, times(3)).existeConflito(anyLong(), eq(dataInicial), eq(dataFinal), anyList());
	}

	@Test
	void deveLancarExcecaoQuandoClienteNaoForEncontrado() {

		Long clienteId = 1L;

		when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

		CadastrarReservaRequest request = new CadastrarReservaRequest(clienteId, LocalDate.now(),
				LocalDate.now().plusDays(3), List.of(10L));

		// EXECUTAR
		ClienteNaoEncontradoException excecao = assertThrows(ClienteNaoEncontradoException.class,
				() -> reservaService.cadastrar(request));

		// VERIFICAR
		assertEquals("Cliente não encontrado.", excecao.getMessage());

		verify(reservaRepository, never()).save(any(Reserva.class));

		verify(clienteRepository).findById(clienteId);

	}

	@Test
	void deveRecusarCadastroQuandoClienteEstiverInativo() {

		Long clienteId = 1L;
		List<Long> unidadesIds = List.of(10L, 20L, 30L);
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = LocalDate.now().plusDays(3);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");
		cliente.desativar();

		CadastrarReservaRequest reserva = new CadastrarReservaRequest(clienteId, dataInicial, dataFinal, unidadesIds);

		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

		// EXECUTAR
		ClienteInativoParaReservaException excecao = assertThrows(ClienteInativoParaReservaException.class,
				() -> reservaService.cadastrar(reserva));

		// VERIFICAR
		assertEquals("Cliente inativo não pode criar uma nova reserva", excecao.getMessage());

		verify(reservaRepository, never()).save(any(Reserva.class));

		verify(clienteRepository).findById(clienteId);
	}

	@Test
	void deveLancarExcecaoQuandoDataInicialForAnteriorAHoje() {

		Long clienteId = 1L;
		List<Long> unidadesIds = List.of(10L, 20L, 30L);
		LocalDate dataInicial = LocalDate.now().minusDays(1);
		LocalDate dataFinal = LocalDate.now().plusDays(3);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		CadastrarReservaRequest reserva = new CadastrarReservaRequest(clienteId, dataInicial, dataFinal, unidadesIds);

		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

		// EXECUTAR
		DataInicialInvalidaException excecao = assertThrows(DataInicialInvalidaException.class,
				() -> reservaService.cadastrar(reserva));

		// VERIFICAR
		assertEquals("Data inicial não pode ser anterior à data de hoje", excecao.getMessage());

		verify(reservaRepository, never()).save(any(Reserva.class));

		verify(clienteRepository).findById(clienteId);

	}

	@Test
	void deveLancarExcecaoQuandoDataFinalForAnteriorADataInicial() {

		Long clienteId = 1L;
		List<Long> unidadesIds = List.of(10L, 20L, 30L);
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = LocalDate.now().minusDays(1);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		CadastrarReservaRequest reserva = new CadastrarReservaRequest(clienteId, dataInicial, dataFinal, unidadesIds);

		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

		// EXECUTAR
		DataFinalInvalidaException excecao = assertThrows(DataFinalInvalidaException.class,
				() -> reservaService.cadastrar(reserva));

		// VERIFICAR
		assertEquals("Data final não pode ser anterior à data inicial", excecao.getMessage());

		verify(reservaRepository, never()).save(any(Reserva.class));

		verify(clienteRepository).findById(clienteId);

	}

	@Test
	void deveLancarExcecaoQuandoTiverIdsDuplicados() {

		Long clienteId = 1L;
		List<Long> unidadesIds = List.of(10L, 10L, 30L);
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = LocalDate.now().plusDays(3);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

		CadastrarReservaRequest reserva = new CadastrarReservaRequest(clienteId, dataInicial, dataFinal, unidadesIds);

		// EXECUTAR
		UnidadeDuplicadaNaReservaException excecao = assertThrows(UnidadeDuplicadaNaReservaException.class,
				() -> reservaService.cadastrar(reserva));

		// VERIFICAR
		assertEquals("A mesma unidade de equipamento foi informada mais de uma vez na mesma reserva.",
				excecao.getMessage());

		verify(reservaRepository, never()).save(any(Reserva.class));

		verify(clienteRepository).findById(clienteId);

	}

	@Test
	void deveLancarExcecaoQuandoEstiverFaltandoUnidades() {

		Long clienteId = 1L;
		List<Long> unidadesIds = List.of(10L, 20L, 30L);
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = LocalDate.now().plusDays(3);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long categoriaId = 1L;
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");
		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		Long equipamentoId = 2L;
		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira de 1 metro",
				new BigDecimal("900.00"), categoria);
		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);

		UnidadeEquipamento unidade1 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade1, "id", 10L);

		UnidadeEquipamento unidade2 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade2, "id", 20L);

		UnidadeEquipamento unidade3 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade3, "id", 30L);

		List<UnidadeEquipamento> unidadesDoBanco = List.of(unidade1, unidade2);

		CadastrarReservaRequest reserva = new CadastrarReservaRequest(clienteId, dataInicial, dataFinal, unidadesIds);

		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
		when(unidadeEquipamentoRepository.findAllById(unidadesIds)).thenReturn(unidadesDoBanco);

		// EXECUTAR
		UnidadeEquipamentoNaoEncontradoException excecao = assertThrows(UnidadeEquipamentoNaoEncontradoException.class,
				() -> reservaService.cadastrar(reserva));

		// VERIFICAR
		assertEquals("Unidades de equipamento não encontradas: [30]", excecao.getMessage());

		verify(reservaRepository, never()).save(any(Reserva.class));

		verify(clienteRepository).findById(clienteId);

	}

	@Test
	void deveLancarExcecaoQuandoUnidadeEstiverInativa() {

		Long clienteId = 1L;
		List<Long> unidadesIds = List.of(10L, 20L, 30L);
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = LocalDate.now().plusDays(3);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long categoriaId = 1L;
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");
		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		Long equipamentoId = 2L;
		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira de 1 metro",
				new BigDecimal("900.00"), categoria);
		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);

		UnidadeEquipamento unidade1 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade1, "id", 10L);

		UnidadeEquipamento unidade2 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade2, "id", 20L);

		UnidadeEquipamento unidade3 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade3, "id", 30L);
		unidade3.desativar();

		List<UnidadeEquipamento> unidadesDoBanco = List.of(unidade1, unidade2, unidade3);

		CadastrarReservaRequest reserva = new CadastrarReservaRequest(clienteId, dataInicial, dataFinal, unidadesIds);

		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
		when(unidadeEquipamentoRepository.findAllById(unidadesIds)).thenReturn(unidadesDoBanco);

		// EXECUTAR
		UnidadeEquipamentoJaInativaException excecao = assertThrows(UnidadeEquipamentoJaInativaException.class,
				() -> reservaService.cadastrar(reserva));

		// VERIFICAR
		assertEquals("A unidade de equipamento já está inativa.", excecao.getMessage());

		verify(reservaRepository, never()).save(any(Reserva.class));

		verify(clienteRepository).findById(clienteId);

	}

	@Test
	void deveLancarExcecaoQuandoUnidadeEstiverStatusEmManutencao() {

		Long clienteId = 1L;
		List<Long> unidadesIds = List.of(10L, 20L, 30L);
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = LocalDate.now().plusDays(3);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long categoriaId = 1L;
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");
		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		Long equipamentoId = 2L;
		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira de 1 metro",
				new BigDecimal("900.00"), categoria);
		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);

		UnidadeEquipamento unidade1 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade1, "id", 10L);
		unidade1.enviarParaManutencao();

		UnidadeEquipamento unidade2 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade2, "id", 20L);

		UnidadeEquipamento unidade3 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade3, "id", 30L);

		List<UnidadeEquipamento> unidadesDoBanco = List.of(unidade1, unidade2, unidade3);

		CadastrarReservaRequest reserva = new CadastrarReservaRequest(clienteId, dataInicial, dataFinal, unidadesIds);

		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
		when(unidadeEquipamentoRepository.findAllById(unidadesIds)).thenReturn(unidadesDoBanco);

		// EXECUTAR
		UnidadeIndisponivelParaReservaException excecao = assertThrows(UnidadeIndisponivelParaReservaException.class,
				() -> reservaService.cadastrar(reserva));

		// VERIFICAR
		assertEquals("A unidade de equipamento está em manutenção e não pode ser reservada.", excecao.getMessage());

		verify(reservaRepository, never()).save(any(Reserva.class));

		verify(clienteRepository).findById(clienteId);

	}

	@Test
	void deveLancarExcecaoQuandoUnidadeEstiverComConflito() {

		Long clienteId = 1L;
		List<Long> unidadesIds = List.of(10L, 20L, 30L);
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = LocalDate.now().plusDays(3);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long categoriaId = 1L;
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");
		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		Long equipamentoId = 2L;
		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira de 1 metro",
				new BigDecimal("900.00"), categoria);
		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);

		UnidadeEquipamento unidade1 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade1, "id", 10L);

		UnidadeEquipamento unidade2 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade2, "id", 20L);

		UnidadeEquipamento unidade3 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade3, "id", 30L);

		List<UnidadeEquipamento> unidadesDoBanco = List.of(unidade1, unidade2, unidade3);

		CadastrarReservaRequest reserva = new CadastrarReservaRequest(clienteId, dataInicial, dataFinal, unidadesIds);

		when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
		when(unidadeEquipamentoRepository.findAllById(unidadesIds)).thenReturn(unidadesDoBanco);
		when(itemReservaRepository.existeConflito(anyLong(), eq(dataInicial), eq(dataFinal), anyList()))
				.thenReturn(true);

		// EXECUTAR
		ConflitoDeReservaException excecao = assertThrows(ConflitoDeReservaException.class,
				() -> reservaService.cadastrar(reserva));

		// VERIFICAR
		assertEquals("A unidade de equipamento já possui uma reserva conflitante nesse período.", excecao.getMessage());

		verify(reservaRepository, never()).save(any(Reserva.class));

		verify(clienteRepository).findById(clienteId);
		verify(itemReservaRepository).existeConflito(anyLong(), eq(dataInicial), eq(dataFinal), anyList());

	}

	@Test
	void deveBuscarReservaPorIdComSucesso() {

		// PREPARAR
		Long reservaId = 1L;

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		reserva.calcularValorTotal();

		// MOCKS
		when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));

		// EXECUTAR
		ReservaResponse resposta = reservaService.buscarPorId(reservaId);

		// VERIFICAR
		assertNotNull(resposta);
		assertEquals(reservaId, resposta.id());
		assertEquals("SOLICITADA", resposta.status());

		verify(reservaRepository).findById(reservaId);
	}

	@Test
	void deveLancarExcecaoAoBuscarReservaSemSucesso() {

		// PREPARAR
		Long reservaId = 1L;

		// MOCKS
		when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());

		// EXECUTAR
		ReservaNaoEncontradaException excecao = assertThrows(ReservaNaoEncontradaException.class,
				() -> reservaService.buscarPorId(reservaId));

		// VERIFICAR
		assertEquals("Reserva não encontrada.", excecao.getMessage());

		verify(reservaRepository).findById(reservaId);
	}

	@Test
	void deveListarReservasComPaginacao() {

		Long clienteId = 1L;
		List<Long> unidadesIds = List.of(10L, 20L, 30L);
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = LocalDate.now().plusDays(3);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva1 = new Reserva(cliente, dataInicial, dataFinal);
		ReflectionTestUtils.setField(reserva1, "id", 40L);
		Reserva reserva2 = new Reserva(cliente, dataInicial, dataFinal);
		ReflectionTestUtils.setField(reserva2, "id", 50L);
		Pageable paginacao = PageRequest.of(0, 2);

		Page<Reserva> paginaDeReservas = new PageImpl<>(List.of(reserva1, reserva2), paginacao, 5);

		// MOCKS
		when(reservaRepository.findAll(paginacao)).thenReturn(paginaDeReservas);

		// EXECUTAR
		PaginaResponse<ReservaResponse> resposta = reservaService.listarPaginado(paginacao);

		// VERIFICAR
		assertEquals(1, resposta.paginaAtual());
		assertEquals(2, resposta.tamanho());
		assertEquals(2, resposta.quantidadeElementos());
		assertEquals(5L, resposta.totalElementos());
		assertEquals(3, resposta.totalPaginas());

		assertTrue(resposta.primeiraPagina());
		assertFalse(resposta.ultimaPagina());

		assertEquals(2, resposta.conteudo().size());

		ReservaResponse primeiraReserva = resposta.conteudo().get(0);

		assertEquals(reserva1.getId(), primeiraReserva.id());

		ReservaResponse segundaReserva = resposta.conteudo().get(1);

		assertEquals(reserva2.getId(), segundaReserva.id());

		verify(reservaRepository).findAll(paginacao);
	}

	@Test
	void deveAprovarReserva() {

		// PREPARAR
		Long clienteId = 1L;
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = LocalDate.now().plusDays(3);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva1 = new Reserva(cliente, dataInicial, dataFinal);
		ReflectionTestUtils.setField(reserva1, "id", 40L);

		// MOCKS
		when(reservaRepository.findById(40L)).thenReturn(Optional.of(reserva1));

		// EXECUTAR
		ReservaResponse resposta = reservaService.aprovar(40L);

		// VERIFICAR
		assertEquals("APROVADA", resposta.status());
		verify(reservaRepository).findById(40L);
	}

	@Test
	void deveRejeitarReserva() {

		// PREPARAR
		Long clienteId = 1L;
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = LocalDate.now().plusDays(3);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva1 = new Reserva(cliente, dataInicial, dataFinal);
		ReflectionTestUtils.setField(reserva1, "id", 40L);

		// MOCKS
		when(reservaRepository.findById(40L)).thenReturn(Optional.of(reserva1));

		// EXECUTAR
		ReservaResponse resposta = reservaService.rejeitar(40L);

		// VERIFICAR
		assertEquals("REJEITADA", resposta.status());
		verify(reservaRepository).findById(40L);
	}

	@Test
	void deveCancelarReserva() {

		// PREPARAR
		Long clienteId = 1L;
		List<Long> unidadesIds = List.of(10L, 20L, 30L);
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = LocalDate.now().plusDays(3);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva1 = new Reserva(cliente, dataInicial, dataFinal);
		ReflectionTestUtils.setField(reserva1, "id", 40L);
		reserva1.aprovar();

		// MOCKS
		when(reservaRepository.findById(40L)).thenReturn(Optional.of(reserva1));

		// EXECUTAR
		ReservaResponse resposta = reservaService.cancelar(40L);

		// VERIFICAR
		assertEquals("CANCELADA", resposta.status());
		verify(reservaRepository).findById(40L);
	}

	@Test
	void deveLancarExcecaoAoAprovarReservaComStatusProibido() {

		// PREPARAR
		Long clienteId = 1L;
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = LocalDate.now().plusDays(3);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva1 = new Reserva(cliente, dataInicial, dataFinal);
		ReflectionTestUtils.setField(reserva1, "id", 40L);
		reserva1.aprovar();

		// MOCKS
		when(reservaRepository.findById(40L)).thenReturn(Optional.of(reserva1));

		// EXECUTAR
		OperacaoReservaInvalidaException excecao = assertThrows(OperacaoReservaInvalidaException.class,
				() -> reservaService.aprovar(40L));

		// VERIFICAR
		assertEquals("A reserva só pode ser aprovada quando estiver com status SOLICITADA.", excecao.getMessage());

		verify(reservaRepository).findById(40L);
	}

	@Test
	void deveLancarExcecaoAoRejeitarReservaQueNaoEstaSolicitada() {

		// PREPARAR
		Long clienteId = 1L;
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = LocalDate.now().plusDays(3);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva1 = new Reserva(cliente, dataInicial, dataFinal);
		ReflectionTestUtils.setField(reserva1, "id", 40L);
		reserva1.aprovar();

		// MOCKS
		when(reservaRepository.findById(40L)).thenReturn(Optional.of(reserva1));

		// EXECUTAR
		OperacaoReservaInvalidaException excecao = assertThrows(OperacaoReservaInvalidaException.class,
				() -> reservaService.rejeitar(40L));

		// VERIFICAR
		assertEquals("A reserva só pode ser rejeitada quando estiver com status SOLICITADA.", excecao.getMessage());

		verify(reservaRepository).findById(40L);
	}

	@Test
	void deveLancarExcecaoAoCancelarReservaQueEstaEmAndamento() {

		// PREPARAR
		Long clienteId = 1L;
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = LocalDate.now().plusDays(3);

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva1 = new Reserva(cliente, dataInicial, dataFinal);
		ReflectionTestUtils.setField(reserva1, "id", 40L);
		reserva1.aprovar();
		reserva1.iniciar();

		// MOCKS
		when(reservaRepository.findById(40L)).thenReturn(Optional.of(reserva1));

		// EXECUTAR
		OperacaoReservaInvalidaException excecao = assertThrows(OperacaoReservaInvalidaException.class,
				() -> reservaService.cancelar(40L));

		// VERIFICAR
		assertEquals("A reserva só pode ser cancelada quando estiver com status SOLICITADA ou APROVADA.",
				excecao.getMessage());

		verify(reservaRepository).findById(40L);
	}

}
