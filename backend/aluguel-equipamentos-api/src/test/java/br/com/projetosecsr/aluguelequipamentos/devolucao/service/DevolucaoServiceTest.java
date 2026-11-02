package br.com.projetosecsr.aluguelequipamentos.devolucao.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.projetosecsr.aluguelequipamentos.categoria.entidade.Categoria;
import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.Cliente;
import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.TipoCliente;
import br.com.projetosecsr.aluguelequipamentos.devolucao.entidade.CondicaoFisicaDevolucao;
import br.com.projetosecsr.aluguelequipamentos.devolucao.entidade.Devolucao;
import br.com.projetosecsr.aluguelequipamentos.devolucao.entidade.ItemDevolucao;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.ConfirmacaoDevolucaoObrigatoriaException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.DevolucaoAnteriorRetiradaException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.DevolucaoAntesDataInicialException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.DevolucaoJaRealizadaException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.ItemDevolucaoDuplicadoException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.ItensDevolucaoDivergentesException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.ObservacaoObrigatoriaException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.excecao.StatusUnidadeInvalidoParaDevolucaoException;
import br.com.projetosecsr.aluguelequipamentos.devolucao.repository.DevolucaoRepository;
import br.com.projetosecsr.aluguelequipamentos.devolucao.request.CadastrarDevolucaoRequest;
import br.com.projetosecsr.aluguelequipamentos.devolucao.request.ItemDevolucaoRequest;
import br.com.projetosecsr.aluguelequipamentos.devolucao.response.DevolucaoResponse;
import br.com.projetosecsr.aluguelequipamentos.equipamento.entidade.Equipamento;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.ItemReserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.Reserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.StatusReserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.OperacaoReservaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.ReservaNaoEncontradaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.repository.ItemReservaRepository;
import br.com.projetosecsr.aluguelequipamentos.reserva.repository.ReservaRepository;
import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.MetodoVerificacaoAutorizacao;
import br.com.projetosecsr.aluguelequipamentos.retirada.entidade.Retirada;
import br.com.projetosecsr.aluguelequipamentos.retirada.excecao.RetiradaNaoEncontradaException;
import br.com.projetosecsr.aluguelequipamentos.retirada.repository.RetiradaRepository;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.StatusUnidadeEquipamento;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.UnidadeEquipamento;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
public class DevolucaoServiceTest {

	@Mock
	private RetiradaRepository retiradaRepository;

	@Mock
	private ItemReservaRepository itemReservaRepository;

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private ReservaRepository reservaRepository;

	@Mock
	private DevolucaoRepository devolucaoRepository;

	@InjectMocks
	private DevolucaoService devolucaoService;

	@Test
	void deveLancarExcecaoQuandoNaoTiverConfirmacaoDoRecebimento() {

		CadastrarDevolucaoRequest request = new CadastrarDevolucaoRequest(1L, false, null);

		// EXECUTAR
		ConfirmacaoDevolucaoObrigatoriaException excecao = assertThrows(ConfirmacaoDevolucaoObrigatoriaException.class,
				() -> devolucaoService.cadastrar(request));

		// VERIFICAR
		assertEquals("Favor confirmar a devolução dos equipamentos alugados.", excecao.getMessage());

		verify(devolucaoRepository, never()).save(any(Devolucao.class));
	}

	@Test
	void deveLancarExcecaoQuandoReservaNaoExistir() {

		CadastrarDevolucaoRequest request = new CadastrarDevolucaoRequest(1L, true, null);

		when(reservaRepository.buscarPorIdComBloqueio(1L)).thenReturn(Optional.empty());

		// EXECUTAR
		ReservaNaoEncontradaException excecao = assertThrows(ReservaNaoEncontradaException.class,
				() -> devolucaoService.cadastrar(request));

		// VERIFICAR
		assertEquals("Reserva não encontrada.", excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(1L);

		verify(devolucaoRepository, never()).save(any(Devolucao.class));
	}

	@Test
	void deveLancarExcecaoQuandoReservaNaoEstiverEmAndamento() {

		Long reservaId = 2L;
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");
		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));
		ReflectionTestUtils.setField(reserva, "id", reservaId);
		CadastrarDevolucaoRequest request = new CadastrarDevolucaoRequest(reservaId, true, null);

		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		// EXECUTAR
		OperacaoReservaInvalidaException excecao = assertThrows(OperacaoReservaInvalidaException.class,
				() -> devolucaoService.cadastrar(request));

		// VERIFICAR
		assertEquals("A situação da reserva deve estar EM ANDAMENTO", excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(devolucaoRepository, never()).save(any(Devolucao.class));
	}

	@Test
	void deveLancarExcecaoQuandoDataDaDevolucaForAnteriorADataInicialDaReserva() {

		Long reservaId = 2L;
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");
		Reserva reserva = new Reserva(cliente, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
		ReflectionTestUtils.setField(reserva, "id", reservaId);
		reserva.aprovar();
		reserva.iniciar();
		CadastrarDevolucaoRequest request = new CadastrarDevolucaoRequest(reservaId, true, null);

		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		// EXECUTAR
		DevolucaoAntesDataInicialException excecao = assertThrows(DevolucaoAntesDataInicialException.class,
				() -> devolucaoService.cadastrar(request));

		// VERIFICAR
		assertEquals("Data da devolução não pode ser anterior à data inicial.", excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(devolucaoRepository, never()).save(any(Devolucao.class));
	}

	@Test
	void deveLancarExcecaoQuandoRetiradaNaoForEncontrada() {

		Long reservaId = 2L;
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");
		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));
		ReflectionTestUtils.setField(reserva, "id", reservaId);
		reserva.aprovar();
		reserva.iniciar();
		CadastrarDevolucaoRequest request = new CadastrarDevolucaoRequest(reservaId, true, null);

		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));
		when(retiradaRepository.findByReservaId(reservaId)).thenReturn(Optional.empty());

		// EXECUTAR
		RetiradaNaoEncontradaException excecao = assertThrows(RetiradaNaoEncontradaException.class,
				() -> devolucaoService.cadastrar(request));

		// VERIFICAR
		assertEquals("Retirada não encontrada.", excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(devolucaoRepository, never()).save(any(Devolucao.class));
	}

	@Test
	void deveLancarExcecaoQuandoJaExistirDevolucao() {

		Long reservaId = 2L;
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");
		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));
		ReflectionTestUtils.setField(reserva, "id", reservaId);
		reserva.aprovar();
		reserva.iniciar();
		CadastrarDevolucaoRequest request = new CadastrarDevolucaoRequest(reservaId, true, null);

		Retirada retirada = new Retirada(OffsetDateTime.now().minusHours(2), "João da Silva", "52998224725",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, reserva, null);

		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));
		when(retiradaRepository.findByReservaId(reservaId)).thenReturn(Optional.of(retirada));
		when(devolucaoRepository.existsByReservaId(reservaId)).thenReturn(true);

		// EXECUTAR
		DevolucaoJaRealizadaException excecao = assertThrows(DevolucaoJaRealizadaException.class,
				() -> devolucaoService.cadastrar(request));

		// VERIFICAR
		assertEquals("Devolução dessa reserva ja foi realizada.", excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(devolucaoRepository, never()).save(any(Devolucao.class));
	}

	@Test
	void deveLancarExcecaoQuandoJaExistirItensDuplicados() {

		Long reservaId = 2L;
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");
		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));
		ReflectionTestUtils.setField(reserva, "id", reservaId);
		reserva.aprovar();
		reserva.iniciar();

		Long itemReservaId = 10L;
		ItemDevolucaoRequest itens = new ItemDevolucaoRequest(itemReservaId,
				CondicaoFisicaDevolucao.SEM_DANOS_APARENTES, "");
		CadastrarDevolucaoRequest request = new CadastrarDevolucaoRequest(reservaId, true, List.of(itens, itens));

		Retirada retirada = new Retirada(OffsetDateTime.now().minusHours(2), "João da Silva", "52998224725",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, reserva, null);

		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));
		when(retiradaRepository.findByReservaId(reservaId)).thenReturn(Optional.of(retirada));
		when(devolucaoRepository.existsByReservaId(reservaId)).thenReturn(false);

		// EXECUTAR
		ItemDevolucaoDuplicadoException excecao = assertThrows(ItemDevolucaoDuplicadoException.class,
				() -> devolucaoService.cadastrar(request));

		// VERIFICAR
		assertEquals("Há itens duplicados para devolução.", excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(devolucaoRepository, never()).save(any(Devolucao.class));
		verify(devolucaoRepository).existsByReservaId(reservaId);
	}

	@Test
	void deveLancarExcecaoQuandoExistirItensDivergentesEntreReservaEDevolucao() {

		Long reservaId = 2L;

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		reserva.aprovar();
		reserva.iniciar();

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade1 = new UnidadeEquipamento(equipamento);
		UnidadeEquipamento unidade2 = new UnidadeEquipamento(equipamento);

		ItemReserva itemReserva1 = new ItemReserva(unidade1, new BigDecimal("90.00"), 4L);

		ItemReserva itemReserva2 = new ItemReserva(unidade2, new BigDecimal("90.00"), 4L);

		ReflectionTestUtils.setField(itemReserva1, "id", 10L);
		ReflectionTestUtils.setField(itemReserva2, "id", 11L);

		reserva.adicionarItem(itemReserva1);
		reserva.adicionarItem(itemReserva2);

		ItemDevolucaoRequest itemEnviado1 = new ItemDevolucaoRequest(10L, CondicaoFisicaDevolucao.SEM_DANOS_APARENTES,
				"");

		ItemDevolucaoRequest itemEnviado2 = new ItemDevolucaoRequest(15L, CondicaoFisicaDevolucao.SEM_DANOS_APARENTES,
				"");

		CadastrarDevolucaoRequest request = new CadastrarDevolucaoRequest(reservaId, true,
				List.of(itemEnviado1, itemEnviado2));

		Retirada retirada = new Retirada(OffsetDateTime.now().minusHours(2), "João da Silva", "52998224725",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, reserva, null);

		// MOCKS

		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		when(retiradaRepository.findByReservaId(reservaId)).thenReturn(Optional.of(retirada));

		when(devolucaoRepository.existsByReservaId(reservaId)).thenReturn(false);

		// EXECUTAR

		ItensDevolucaoDivergentesException excecao = assertThrows(ItensDevolucaoDivergentesException.class,
				() -> devolucaoService.cadastrar(request));

		// VERIFICAR

		assertEquals("Os itens informados para devolução devem corresponder exatamente aos itens da reserva.",
				excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository).findByReservaId(reservaId);

		verify(devolucaoRepository).existsByReservaId(reservaId);

		verify(devolucaoRepository, never()).save(any(Devolucao.class));
	}

	@Test
	void deveLancarExcecaoQuandoStatusDaUnidadeForInvalidoParaDevolucao() {

		// PREPARAR

		Long reservaId = 2L;
		Long itemReservaId = 10L;

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		reserva.aprovar();
		reserva.iniciar();

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);

		ReflectionTestUtils.setField(unidade, "codigo", "UNI-000001");

		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);

		ReflectionTestUtils.setField(itemReserva, "id", itemReservaId);

		reserva.adicionarItem(itemReserva);

		ItemDevolucaoRequest itemEnviado = new ItemDevolucaoRequest(itemReservaId,
				CondicaoFisicaDevolucao.SEM_DANOS_APARENTES, null);

		CadastrarDevolucaoRequest request = new CadastrarDevolucaoRequest(reservaId, true, List.of(itemEnviado));

		Retirada retirada = new Retirada(OffsetDateTime.now().minusHours(2), "João da Silva", "52998224725",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, reserva, null);

		// MOCKS

		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		when(retiradaRepository.findByReservaId(reservaId)).thenReturn(Optional.of(retirada));

		when(devolucaoRepository.existsByReservaId(reservaId)).thenReturn(false);

		// EXECUTAR

		StatusUnidadeInvalidoParaDevolucaoException excecao = assertThrows(
				StatusUnidadeInvalidoParaDevolucaoException.class, () -> devolucaoService.cadastrar(request));

		// VERIFICAR

		assertEquals("A unidade do equipamento UNI-000001 deve estar com status ALUGADO para permitir a devolução.",
				excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository).findByReservaId(reservaId);

		verify(devolucaoRepository).existsByReservaId(reservaId);

		verify(devolucaoRepository, never()).save(any(Devolucao.class));

		assertEquals(StatusUnidadeEquipamento.DISPONIVEL, unidade.getStatus());
	}

	@Test
	void deveLancarExcecaoQuandoObservacaoObrigatoriaNaoForInformada() {

		// PREPARAR

		Long reservaId = 2L;
		Long itemReservaId = 10L;

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		reserva.aprovar();
		reserva.iniciar();

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Furadeira", "Furadeira elétrica", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);

		unidade.alugar();

		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);

		ReflectionTestUtils.setField(itemReserva, "id", itemReservaId);

		reserva.adicionarItem(itemReserva);

		ItemDevolucaoRequest itemEnviado = new ItemDevolucaoRequest(itemReservaId,
				CondicaoFisicaDevolucao.COM_OBSERVACOES, null);

		CadastrarDevolucaoRequest request = new CadastrarDevolucaoRequest(reservaId, true, List.of(itemEnviado));

		Retirada retirada = new Retirada(OffsetDateTime.now().minusHours(2), "João da Silva", "52998224725",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, reserva, null);

		// MOCKS

		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		when(retiradaRepository.findByReservaId(reservaId)).thenReturn(Optional.of(retirada));

		when(devolucaoRepository.existsByReservaId(reservaId)).thenReturn(false);

		// EXECUTAR

		ObservacaoObrigatoriaException excecao = assertThrows(ObservacaoObrigatoriaException.class,
				() -> devolucaoService.cadastrar(request));

		// VERIFICAR

		assertEquals(
				"É obrigatório informar as observações quando a condição física do equipamento for COM_OBSERVACOES.",
				excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository).findByReservaId(reservaId);

		verify(devolucaoRepository).existsByReservaId(reservaId);

		verify(devolucaoRepository, never()).save(any(Devolucao.class));

		assertEquals(StatusUnidadeEquipamento.ALUGADO, unidade.getStatus());

		assertEquals(StatusReserva.EM_ANDAMENTO, reserva.getStatus());
	}

	@Test
	void deveLancarExcecaoQuandoObservacaoObrigatoriaNaoForEscrita() {

		// PREPARAR

		Long reservaId = 2L;
		Long itemReservaId = 10L;

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		reserva.aprovar();
		reserva.iniciar();

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Furadeira", "Furadeira elétrica", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);

		unidade.alugar();

		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);

		ReflectionTestUtils.setField(itemReserva, "id", itemReservaId);

		reserva.adicionarItem(itemReserva);

		ItemDevolucaoRequest itemEnviado = new ItemDevolucaoRequest(itemReservaId,
				CondicaoFisicaDevolucao.COM_OBSERVACOES, "   ");

		CadastrarDevolucaoRequest request = new CadastrarDevolucaoRequest(reservaId, true, List.of(itemEnviado));

		Retirada retirada = new Retirada(OffsetDateTime.now().minusHours(2), "João da Silva", "52998224725",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, reserva, null);

		// MOCKS

		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		when(retiradaRepository.findByReservaId(reservaId)).thenReturn(Optional.of(retirada));

		when(devolucaoRepository.existsByReservaId(reservaId)).thenReturn(false);

		// EXECUTAR

		ObservacaoObrigatoriaException excecao = assertThrows(ObservacaoObrigatoriaException.class,
				() -> devolucaoService.cadastrar(request));

		// VERIFICAR

		assertEquals(
				"É obrigatório informar as observações quando a condição física do equipamento for COM_OBSERVACOES.",
				excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository).findByReservaId(reservaId);

		verify(devolucaoRepository).existsByReservaId(reservaId);

		verify(devolucaoRepository, never()).save(any(Devolucao.class));

		assertEquals(StatusUnidadeEquipamento.ALUGADO, unidade.getStatus());

		assertEquals(StatusReserva.EM_ANDAMENTO, reserva.getStatus());
	}

	@Test
	void deveLancarExcecaoQuandoDataEHoraDaDevolucaoAnteriorARetirada() {

		// PREPARAR

		Long reservaId = 2L;
		Long itemReservaId = 10L;

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		reserva.aprovar();
		reserva.iniciar();

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Furadeira", "Furadeira elétrica", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);

		unidade.alugar();

		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);

		ReflectionTestUtils.setField(itemReserva, "id", itemReservaId);

		reserva.adicionarItem(itemReserva);

		ItemDevolucaoRequest itemEnviado = new ItemDevolucaoRequest(itemReservaId,
				CondicaoFisicaDevolucao.SEM_DANOS_APARENTES, " Quebrado  ");

		CadastrarDevolucaoRequest request = new CadastrarDevolucaoRequest(reservaId, true, List.of(itemEnviado));

		Retirada retirada = new Retirada(OffsetDateTime.now().plusHours(2), "João da Silva", "52998224725",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, reserva, null);

		// MOCKS

		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		when(retiradaRepository.findByReservaId(reservaId)).thenReturn(Optional.of(retirada));

		when(devolucaoRepository.existsByReservaId(reservaId)).thenReturn(false);

		// EXECUTAR

		DevolucaoAnteriorRetiradaException excecao = assertThrows(DevolucaoAnteriorRetiradaException.class,
				() -> devolucaoService.cadastrar(request));

		// VERIFICAR

		assertEquals("A data e hora da devolução não podem ser anteriores à data e hora da retirada.",
				excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository).findByReservaId(reservaId);

		verify(devolucaoRepository).existsByReservaId(reservaId);

		verify(devolucaoRepository, never()).save(any(Devolucao.class));

		assertEquals(StatusUnidadeEquipamento.ALUGADO, unidade.getStatus());

		assertEquals(StatusReserva.EM_ANDAMENTO, reserva.getStatus());
	}

	@Test
	void deveCadastrarDevolucaoComSucessoQuandoEquipamentoEstiverSemDanos() {

		// PREPARAR

		Long reservaId = 2L;
		Long itemReservaId = 10L;
		Long usuarioId = 1L;

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		reserva.aprovar();
		reserva.iniciar();

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Furadeira", "Furadeira elétrica", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);

		unidade.alugar();

		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);

		ReflectionTestUtils.setField(itemReserva, "id", itemReservaId);

		reserva.adicionarItem(itemReserva);

		ItemDevolucaoRequest itemEnviado = new ItemDevolucaoRequest(itemReservaId,
				CondicaoFisicaDevolucao.SEM_DANOS_APARENTES, null);

		CadastrarDevolucaoRequest request = new CadastrarDevolucaoRequest(reservaId, true, List.of(itemEnviado));

		Retirada retirada = new Retirada(OffsetDateTime.now().minusHours(2), "João da Silva", "52998224725",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, reserva, null);

		Usuario usuario = new Usuario("Administrador", "admin@email.com", "senhaHash", PerfilUsuario.ADMINISTRADOR);

		ReflectionTestUtils.setField(usuario, "id", usuarioId);

		// Simula o usuário autenticado pelo Spring Security
		Authentication autenticacao = new UsernamePasswordAuthenticationToken(usuarioId.toString(), null, List.of());

		SecurityContextHolder.getContext().setAuthentication(autenticacao);

		try {

			// MOCKS

			when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

			when(retiradaRepository.findByReservaId(reservaId)).thenReturn(Optional.of(retirada));

			when(devolucaoRepository.existsByReservaId(reservaId)).thenReturn(false);

			when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

			when(devolucaoRepository.save(any(Devolucao.class))).thenAnswer(invocation -> {

				Devolucao devolucao = invocation.getArgument(0);

				ReflectionTestUtils.setField(devolucao, "id", 1L);

				return devolucao;
			});

			// EXECUTAR

			DevolucaoResponse response = devolucaoService.cadastrar(request);

			// VERIFICAR

			assertNotNull(response);

			assertEquals(StatusUnidadeEquipamento.DISPONIVEL, unidade.getStatus());

			assertEquals(StatusReserva.FINALIZADA, reserva.getStatus());

			ArgumentCaptor<Devolucao> captor = ArgumentCaptor.forClass(Devolucao.class);

			verify(devolucaoRepository).save(captor.capture());

			Devolucao devolucaoSalva = captor.getValue();

			assertNotNull(devolucaoSalva);

			assertEquals(reserva, devolucaoSalva.getReserva());

			assertEquals(usuario, devolucaoSalva.getResponsavel());

			assertEquals(1, devolucaoSalva.getItens().size());

			ItemDevolucao itemSalvo = devolucaoSalva.getItens().get(0);

			assertEquals(itemReserva, itemSalvo.getItemReserva());

			assertEquals(CondicaoFisicaDevolucao.SEM_DANOS_APARENTES, itemSalvo.getCondicaoFisica());

			assertNull(itemSalvo.getObservacoes());

			verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

			verify(retiradaRepository).findByReservaId(reservaId);

			verify(devolucaoRepository).existsByReservaId(reservaId);

			verify(usuarioRepository).findById(usuarioId);

		} finally {

			SecurityContextHolder.clearContext();
		}
	}

	@Test
	void deveCadastrarDevolucaoComSucessoQuandoEquipamentoPossuirObservacoes() {

		// PREPARAR

		Long reservaId = 2L;
		Long itemReservaId = 10L;
		Long usuarioId = 1L;

		String observacoes = "Mandril danificado, equipamento necessita de manutenção.";

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		reserva.aprovar();
		reserva.iniciar();

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Furadeira", "Furadeira elétrica", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);

		unidade.alugar();

		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);

		ReflectionTestUtils.setField(itemReserva, "id", itemReservaId);

		reserva.adicionarItem(itemReserva);

		ItemDevolucaoRequest itemEnviado = new ItemDevolucaoRequest(itemReservaId,
				CondicaoFisicaDevolucao.COM_OBSERVACOES, observacoes);

		CadastrarDevolucaoRequest request = new CadastrarDevolucaoRequest(reservaId, true, List.of(itemEnviado));

		Retirada retirada = new Retirada(OffsetDateTime.now().minusHours(2), "João da Silva", "52998224725",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, reserva, null);

		Usuario usuario = new Usuario("Administrador", "admin@email.com", "senhaHash", PerfilUsuario.ADMINISTRADOR);

		ReflectionTestUtils.setField(usuario, "id", usuarioId);

		Authentication autenticacao = new UsernamePasswordAuthenticationToken(usuarioId.toString(), null, List.of());

		SecurityContextHolder.getContext().setAuthentication(autenticacao);

		try {

			// MOCKS

			when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

			when(retiradaRepository.findByReservaId(reservaId)).thenReturn(Optional.of(retirada));

			when(devolucaoRepository.existsByReservaId(reservaId)).thenReturn(false);

			when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

			when(devolucaoRepository.save(any(Devolucao.class))).thenAnswer(invocation -> {

				Devolucao devolucao = invocation.getArgument(0);

				ReflectionTestUtils.setField(devolucao, "id", 1L);

				return devolucao;
			});

			// EXECUTAR

			DevolucaoResponse response = devolucaoService.cadastrar(request);

			// VERIFICAR

			assertNotNull(response);

			assertEquals(StatusUnidadeEquipamento.EM_MANUTENCAO, unidade.getStatus());

			assertEquals(StatusReserva.FINALIZADA, reserva.getStatus());

			ArgumentCaptor<Devolucao> captor = ArgumentCaptor.forClass(Devolucao.class);

			verify(devolucaoRepository).save(captor.capture());

			Devolucao devolucaoSalva = captor.getValue();

			assertNotNull(devolucaoSalva);

			assertEquals(reserva, devolucaoSalva.getReserva());

			assertEquals(usuario, devolucaoSalva.getResponsavel());

			assertEquals(1, devolucaoSalva.getItens().size());

			ItemDevolucao itemSalvo = devolucaoSalva.getItens().get(0);

			assertEquals(itemReserva, itemSalvo.getItemReserva());

			assertEquals(CondicaoFisicaDevolucao.COM_OBSERVACOES, itemSalvo.getCondicaoFisica());

			assertEquals(observacoes, itemSalvo.getObservacoes());

			verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

			verify(retiradaRepository).findByReservaId(reservaId);

			verify(devolucaoRepository).existsByReservaId(reservaId);

			verify(usuarioRepository).findById(usuarioId);

		} finally {

			SecurityContextHolder.clearContext();
		}
	}

	@Test
	void deveCadastrarDevolucaoComUnidadesEmCondicoesDiferentes() {

		// PREPARAR

		Long reservaId = 2L;
		Long usuarioId = 1L;

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		reserva.aprovar();
		reserva.iniciar();

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Furadeira", "Furadeira elétrica", new BigDecimal("90.00"),
				categoria);

		// PRIMEIRA UNIDADE: SEM DANOS

		UnidadeEquipamento unidade1 = new UnidadeEquipamento(equipamento);
		unidade1.alugar();

		ItemReserva itemReserva1 = new ItemReserva(unidade1, new BigDecimal("90.00"), 4L);

		ReflectionTestUtils.setField(itemReserva1, "id", 10L);
		reserva.adicionarItem(itemReserva1);

		// SEGUNDA UNIDADE: COM OBSERVAÇÕES

		UnidadeEquipamento unidade2 = new UnidadeEquipamento(equipamento);
		unidade2.alugar();

		ItemReserva itemReserva2 = new ItemReserva(unidade2, new BigDecimal("90.00"), 4L);

		ReflectionTestUtils.setField(itemReserva2, "id", 11L);
		reserva.adicionarItem(itemReserva2);

		// ITENS ENVIADOS NA DEVOLUÇÃO

		ItemDevolucaoRequest itemEnviado1 = new ItemDevolucaoRequest(10L, CondicaoFisicaDevolucao.SEM_DANOS_APARENTES,
				null);

		String observacoes = "Mandril danificado.";

		ItemDevolucaoRequest itemEnviado2 = new ItemDevolucaoRequest(11L, CondicaoFisicaDevolucao.COM_OBSERVACOES,
				observacoes);

		CadastrarDevolucaoRequest request = new CadastrarDevolucaoRequest(reservaId, true,
				List.of(itemEnviado1, itemEnviado2));

		Retirada retirada = new Retirada(OffsetDateTime.now().minusHours(2), "João da Silva", "52998224725",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, reserva, null);

		Usuario usuario = new Usuario("Administrador", "admin@email.com", "senhaHash", PerfilUsuario.ADMINISTRADOR);

		ReflectionTestUtils.setField(usuario, "id", usuarioId);

		Authentication autenticacao = new UsernamePasswordAuthenticationToken(usuarioId.toString(), null, List.of());

		SecurityContextHolder.getContext().setAuthentication(autenticacao);

		try {

			// MOCKS

			when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

			when(retiradaRepository.findByReservaId(reservaId)).thenReturn(Optional.of(retirada));

			when(devolucaoRepository.existsByReservaId(reservaId)).thenReturn(false);

			when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

			when(devolucaoRepository.save(any(Devolucao.class))).thenAnswer(invocation -> {

				Devolucao devolucao = invocation.getArgument(0);

				ReflectionTestUtils.setField(devolucao, "id", 1L);

				return devolucao;
			});

			// EXECUTAR

			DevolucaoResponse response = devolucaoService.cadastrar(request);

			// VERIFICAR

			assertNotNull(response);

			// Cada unidade deve receber seu próprio status

			assertEquals(StatusUnidadeEquipamento.DISPONIVEL, unidade1.getStatus());

			assertEquals(StatusUnidadeEquipamento.EM_MANUTENCAO, unidade2.getStatus());

			// A reserva deve ser finalizada

			assertEquals(StatusReserva.FINALIZADA, reserva.getStatus());

			ArgumentCaptor<Devolucao> captor = ArgumentCaptor.forClass(Devolucao.class);

			verify(devolucaoRepository).save(captor.capture());

			Devolucao devolucaoSalva = captor.getValue();

			assertEquals(reserva, devolucaoSalva.getReserva());
			assertEquals(usuario, devolucaoSalva.getResponsavel());

			// Devem existir dois itens na devolução

			assertEquals(2, devolucaoSalva.getItens().size());

			ItemDevolucao itemSalvo1 = devolucaoSalva.getItens().get(0);
			ItemDevolucao itemSalvo2 = devolucaoSalva.getItens().get(1);

			// Primeira unidade

			assertEquals(itemReserva1, itemSalvo1.getItemReserva());

			assertEquals(CondicaoFisicaDevolucao.SEM_DANOS_APARENTES, itemSalvo1.getCondicaoFisica());

			assertNull(itemSalvo1.getObservacoes());

			// Segunda unidade

			assertEquals(itemReserva2, itemSalvo2.getItemReserva());

			assertEquals(CondicaoFisicaDevolucao.COM_OBSERVACOES, itemSalvo2.getCondicaoFisica());

			assertEquals(observacoes, itemSalvo2.getObservacoes());

			// Verificações dos repositórios

			verify(reservaRepository).buscarPorIdComBloqueio(reservaId);
			verify(retiradaRepository).findByReservaId(reservaId);
			verify(devolucaoRepository).existsByReservaId(reservaId);
			verify(usuarioRepository).findById(usuarioId);

			verify(devolucaoRepository, times(1)).save(any(Devolucao.class));

		} finally {

			SecurityContextHolder.clearContext();
		}
	}

}
