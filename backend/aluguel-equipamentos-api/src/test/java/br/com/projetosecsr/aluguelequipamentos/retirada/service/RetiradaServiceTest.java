package br.com.projetosecsr.aluguelequipamentos.retirada.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.projetosecsr.aluguelequipamentos.categoria.entidade.Categoria;
import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.Cliente;
import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.TipoCliente;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.equipamento.entidade.Equipamento;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.ItemReserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.Reserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.entidade.StatusReserva;
import br.com.projetosecsr.aluguelequipamentos.reserva.excecao.ReservaNaoEncontradaException;
import br.com.projetosecsr.aluguelequipamentos.reserva.repository.ItemReservaRepository;
import br.com.projetosecsr.aluguelequipamentos.reserva.repository.ReservaRepository;
import br.com.projetosecsr.aluguelequipamentos.reserva.response.ReservaResponse;
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
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.UnidadeEquipamento;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
public class RetiradaServiceTest {

	@Mock
	private RetiradaRepository retiradaRepository;

	@Mock
	private ItemReservaRepository itemReservaRepository;

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private ReservaRepository reservaRepository;

	@InjectMocks
	private RetiradaService retiradaService;

	@Test
	void deveLancarExcecaoQuandoReservaNaoExistir() {

		CadastrarRetiradaRequest request = new CadastrarRetiradaRequest(99L, null, null, null, null, null, null, null);

		when(reservaRepository.buscarPorIdComBloqueio(99L)).thenReturn(Optional.empty());

		// EXECUTAR
		ReservaNaoEncontradaException excecao = assertThrows(ReservaNaoEncontradaException.class,
				() -> retiradaService.cadastrar(request));

		// VERIFICAR
		assertEquals("Reserva não encontrada.", excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(99L);

		verify(retiradaRepository, never()).save(any(Retirada.class));

	}

	@Test
	void deveLancarExcecaoQuandoReservaNaoEstiverAprovada() {

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long reservaId = 2L;
		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));
		ReflectionTestUtils.setField(reserva, "id", reservaId);

		CadastrarRetiradaRequest request = new CadastrarRetiradaRequest(reservaId, null, null, null, null, null, null,
				null);

		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		ReservaNaoAprovadaException excecao = assertThrows(ReservaNaoAprovadaException.class,
				() -> retiradaService.cadastrar(request));

		// VERIFICAR
		assertEquals("A retirada só pode ser registrada para uma reserva aprovada.", excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository, never()).save(any(Retirada.class));

	}

	@Test
	void deveLancarExcecaoQuandoRetiradaForAntesDaDataInicial() {

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long reservaId = 2L;
		Reserva reserva = new Reserva(cliente, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
		reserva.aprovar();
		ReflectionTestUtils.setField(reserva, "id", reservaId);

		CadastrarRetiradaRequest request = new CadastrarRetiradaRequest(reservaId, null, null, null, null, null, null,
				null);

		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		RetiradaForaDoPeriodoException excecao = assertThrows(RetiradaForaDoPeriodoException.class,
				() -> retiradaService.cadastrar(request));

		// VERIFICAR
		assertEquals("A retirada só pode ser registrada dentro do período da reserva.", excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository, never()).save(any(Retirada.class));

	}

	@Test
	void deveLancarExcecaoQuandoRetiradaForDepoisDaDataFinal() {

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long reservaId = 2L;
		Reserva reserva = new Reserva(cliente, LocalDate.now().minusDays(3), LocalDate.now().minusDays(1));
		reserva.aprovar();
		ReflectionTestUtils.setField(reserva, "id", reservaId);

		CadastrarRetiradaRequest request = new CadastrarRetiradaRequest(reservaId, null, null, null, null, null, null,
				null);

		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		RetiradaForaDoPeriodoException excecao = assertThrows(RetiradaForaDoPeriodoException.class,
				() -> retiradaService.cadastrar(request));

		// VERIFICAR
		assertEquals("A retirada só pode ser registrada dentro do período da reserva.", excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository, never()).save(any(Retirada.class));

	}

	@Test
	void deveLancarExcecaoQuandoClienteEstiverInativo() {

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		cliente.desativar();

		Long reservaId = 2L;
		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));
		reserva.aprovar();
		ReflectionTestUtils.setField(reserva, "id", reservaId);

		CadastrarRetiradaRequest request = new CadastrarRetiradaRequest(reservaId, null, null, null, null, null, null,
				null);

		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		ClienteInativoParaRetiradaException excecao = assertThrows(ClienteInativoParaRetiradaException.class,
				() -> retiradaService.cadastrar(request));

		// VERIFICAR
		assertEquals("A retirada só pode ser registrada para cliente ativo", excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository, never()).save(any(Retirada.class));

	}

	@Test
	void deveLancarExcecaoQuandoRetiradaJaExistir() {

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long reservaId = 2L;
		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));
		reserva.aprovar();
		ReflectionTestUtils.setField(reserva, "id", reservaId);

		CadastrarRetiradaRequest request = new CadastrarRetiradaRequest(reservaId, null, null, null, null, null, null,
				null);

		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));
		when(retiradaRepository.existsByReservaId(reservaId)).thenReturn(true);

		RetiradaJaRealizadaException excecao = assertThrows(RetiradaJaRealizadaException.class,
				() -> retiradaService.cadastrar(request));

		// VERIFICAR
		assertEquals("Já existe uma retirada registrada para esta reserva.", excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);
		verify(retiradaRepository).existsByReservaId(reservaId);

		verify(retiradaRepository, never()).save(any(Retirada.class));

	}

	@Test
	void deveLancarExcecaoQuandoUnidadeEstiverInativa() {

		// PREPARAR
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long reservaId = 2L;

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		reserva.aprovar();

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);

		unidade.desativar();

		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);

		reserva.adicionarItem(itemReserva);

		CadastrarRetiradaRequest request = new CadastrarRetiradaRequest(reservaId, null, null, null, null, null, null,
				null);

		// MOCKS
		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		// EXECUTAR
		UnidadeInativaParaRetiradaException excecao = assertThrows(UnidadeInativaParaRetiradaException.class,
				() -> retiradaService.cadastrar(request));

		// VERIFICAR
		assertEquals("A unidade de equipamento está inativa e não pode ser retirada.", excecao.getMessage());

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository).existsByReservaId(reservaId);

		verify(retiradaRepository, never()).save(any(Retirada.class));
	}

	@Test
	void deveLancarExcecaoQuandoUnidadeEstiverIndisponivel() {

		// PREPARAR
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long reservaId = 2L;

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		reserva.aprovar();

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);

		unidade.enviarParaManutencao();

		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);

		reserva.adicionarItem(itemReserva);

		CadastrarRetiradaRequest request = new CadastrarRetiradaRequest(reservaId, null, null, null, null, null, null,
				null);

		// MOCKS
		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		// EXECUTAR
		UnidadeIndisponivelParaRetiradaException excecao = assertThrows(UnidadeIndisponivelParaRetiradaException.class,
				() -> retiradaService.cadastrar(request));

		// VERIFICAR
		assertNotNull(excecao);

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository).existsByReservaId(reservaId);

		verify(retiradaRepository, never()).save(any(Retirada.class));
	}

	@Test
	void deveLancarExcecaoQuandoItemRetiradaEstiverDuplicado() {

		// PREPARAR
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long reservaId = 2L;

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		reserva.aprovar();

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);

		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);

		Long itemReservaId = 10L;

		ReflectionTestUtils.setField(itemReserva, "id", itemReservaId);

		reserva.adicionarItem(itemReserva);

		ItemRetiradaRequest item1 = new ItemRetiradaRequest(itemReservaId, CondicaoFisicaRetirada.SEM_DANOS_APARENTES,
				null);

		ItemRetiradaRequest item2 = new ItemRetiradaRequest(itemReservaId, CondicaoFisicaRetirada.SEM_DANOS_APARENTES,
				null);

		CadastrarRetiradaRequest request = new CadastrarRetiradaRequest(reservaId, null, null, null, null,
				List.of(item1, item2), null, null);

		// MOCKS
		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		// EXECUTAR
		ItemRetiradaDuplicadoException excecao = assertThrows(ItemRetiradaDuplicadoException.class,
				() -> retiradaService.cadastrar(request));

		// VERIFICAR
		assertNotNull(excecao);

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository).existsByReservaId(reservaId);

		verify(retiradaRepository, never()).save(any(Retirada.class));
	}

	@Test
	void deveLancarExcecaoQuandoItensDaRetiradaForemDivergentes() {

		// PREPARAR
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long reservaId = 2L;

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		reserva.aprovar();

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade1 = new UnidadeEquipamento(equipamento);
		UnidadeEquipamento unidade2 = new UnidadeEquipamento(equipamento);

		ItemReserva itemReserva1 = new ItemReserva(unidade1, new BigDecimal("90.00"), 4L);

		ItemReserva itemReserva2 = new ItemReserva(unidade2, new BigDecimal("90.00"), 4L);

		ReflectionTestUtils.setField(itemReserva1, "id", 10L);
		ReflectionTestUtils.setField(itemReserva2, "id", 20L);

		reserva.adicionarItem(itemReserva1);
		reserva.adicionarItem(itemReserva2);

		ItemRetiradaRequest itemRetirada = new ItemRetiradaRequest(10L, CondicaoFisicaRetirada.SEM_DANOS_APARENTES,
				null);

		CadastrarRetiradaRequest request = new CadastrarRetiradaRequest(reservaId, null, null, null, null,
				List.of(itemRetirada), null, null);

		// MOCKS
		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		// EXECUTAR
		ItensRetiradaDivergentesException excecao = assertThrows(ItensRetiradaDivergentesException.class,
				() -> retiradaService.cadastrar(request));

		// VERIFICAR
		assertNotNull(excecao);

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository).existsByReservaId(reservaId);

		verify(retiradaRepository, never()).save(any(Retirada.class));
	}

	@Test
	void deveLancarExcecaoQuandoCondicaoFisicaExigirObservacaoENaoForInformada() {

		// PREPARAR
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long reservaId = 2L;

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		reserva.aprovar();

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);

		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);

		Long itemReservaId = 10L;

		ReflectionTestUtils.setField(itemReserva, "id", itemReservaId);

		reserva.adicionarItem(itemReserva);

		ItemRetiradaRequest itemRetirada = new ItemRetiradaRequest(itemReservaId,
				CondicaoFisicaRetirada.COM_OBSERVACOES, null);

		CadastrarRetiradaRequest request = new CadastrarRetiradaRequest(reservaId, null, null, null, null,
				List.of(itemRetirada), null, null);

		// MOCKS
		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		// EXECUTAR
		ObservacaoObrigatoriaException excecao = assertThrows(ObservacaoObrigatoriaException.class,
				() -> retiradaService.cadastrar(request));

		// VERIFICAR
		assertNotNull(excecao);

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository).existsByReservaId(reservaId);

		verify(retiradaRepository, never()).save(any(Retirada.class));
	}

	@Test
	void deveLancarExcecaoQuandoMetodoOutroNaoTiverDescricao() {

		// PREPARAR
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long reservaId = 2L;

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		reserva.aprovar();

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);

		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);

		Long itemReservaId = 10L;

		ReflectionTestUtils.setField(itemReserva, "id", itemReservaId);

		reserva.adicionarItem(itemReserva);

		ItemRetiradaRequest itemRetirada = new ItemRetiradaRequest(itemReservaId,
				CondicaoFisicaRetirada.SEM_DANOS_APARENTES, null);

		CadastrarRetiradaRequest request = new CadastrarRetiradaRequest(reservaId, null, null,
				MetodoVerificacaoAutorizacao.OUTRO, null, List.of(itemRetirada), null, null);

		// MOCKS
		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		// EXECUTAR
		DescricaoMetodoVerificacaoObrigatoriaException excecao = assertThrows(
				DescricaoMetodoVerificacaoObrigatoriaException.class, () -> retiradaService.cadastrar(request));

		// VERIFICAR
		assertNotNull(excecao);

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository).existsByReservaId(reservaId);

		verify(retiradaRepository, never()).save(any(Retirada.class));
	}

	@Test
	void deveLancarExcecaoQuandoAutorizacaoNaoForConfirmada() {

		// PREPARAR
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long reservaId = 2L;

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		reserva.aprovar();

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);

		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);

		Long itemReservaId = 10L;

		ReflectionTestUtils.setField(itemReserva, "id", itemReservaId);

		reserva.adicionarItem(itemReserva);

		ItemRetiradaRequest itemRetirada = new ItemRetiradaRequest(itemReservaId,
				CondicaoFisicaRetirada.SEM_DANOS_APARENTES, null);

		CadastrarRetiradaRequest request = new CadastrarRetiradaRequest(reservaId, "52998224725", "João da Silva",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, List.of(itemRetirada), false, true);

		// MOCKS
		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		// EXECUTAR
		ConfirmacaoRetiradaObrigatoriaException excecao = assertThrows(ConfirmacaoRetiradaObrigatoriaException.class,
				() -> retiradaService.cadastrar(request));

		// VERIFICAR
		assertNotNull(excecao);

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository).existsByReservaId(reservaId);

		verify(retiradaRepository, never()).save(any(Retirada.class));
	}

	@Test
	void deveLancarExcecaoQuandoRecebimentoNaoForConfirmado() {

		// PREPARAR
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long reservaId = 2L;

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		reserva.aprovar();

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);

		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);

		Long itemReservaId = 10L;

		ReflectionTestUtils.setField(itemReserva, "id", itemReservaId);

		reserva.adicionarItem(itemReserva);

		ItemRetiradaRequest itemRetirada = new ItemRetiradaRequest(itemReservaId,
				CondicaoFisicaRetirada.SEM_DANOS_APARENTES, null);

		CadastrarRetiradaRequest request = new CadastrarRetiradaRequest(reservaId, "52998224725", "João da Silva",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, List.of(itemRetirada), true, false);

		// MOCKS
		when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

		// EXECUTAR
		ConfirmacaoRetiradaObrigatoriaException excecao = assertThrows(ConfirmacaoRetiradaObrigatoriaException.class,
				() -> retiradaService.cadastrar(request));

		// VERIFICAR
		assertNotNull(excecao);

		verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

		verify(retiradaRepository).existsByReservaId(reservaId);

		verify(retiradaRepository, never()).save(any(Retirada.class));
	}

	@Test
	void deveCadastrarRetiradaComSucesso() {

		// PREPARAR
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Long reservaId = 2L;

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		reserva.aprovar();

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);

		ReflectionTestUtils.setField(unidade, "id", 20L);
		ReflectionTestUtils.setField(unidade, "codigo", "UNI-000020");

		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);

		Long itemReservaId = 10L;

		ReflectionTestUtils.setField(itemReserva, "id", itemReservaId);

		reserva.adicionarItem(itemReserva);

		ItemRetiradaRequest itemRetirada = new ItemRetiradaRequest(itemReservaId,
				CondicaoFisicaRetirada.SEM_DANOS_APARENTES, null);

		CadastrarRetiradaRequest request = new CadastrarRetiradaRequest(reservaId, "52998224725", "João da Silva",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, List.of(itemRetirada), true, true);

		// PREPARAR O FUNCIONÁRIO AUTENTICADO
		Long usuarioId = 5L;

		Usuario usuario = new Usuario("Funcionário Carlos", "carlos@email.com", "senhaHash", PerfilUsuario.FUNCIONARIO);

		ReflectionTestUtils.setField(usuario, "id", usuarioId);

		Authentication autenticacao = new UsernamePasswordAuthenticationToken(usuarioId.toString(), null);

		SecurityContextHolder.getContext().setAuthentication(autenticacao);

		try {

			// MOCKS
			when(reservaRepository.buscarPorIdComBloqueio(reservaId)).thenReturn(Optional.of(reserva));

			when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

			when(retiradaRepository.save(any(Retirada.class))).thenAnswer(invocacao -> {
				Retirada retiradaSalva = invocacao.getArgument(0);

				ReflectionTestUtils.setField(retiradaSalva, "id", 30L);

				ReflectionTestUtils.setField(retiradaSalva.getItens().get(0), "id", 40L);

				return retiradaSalva;
			});

			// EXECUTAR
			RetiradaResponse resposta = retiradaService.cadastrar(request);

			// VERIFICAR
			assertNotNull(resposta);

			assertEquals(30L, resposta.id());
			assertEquals(reservaId, resposta.reservaId());
			assertEquals("João da Silva", resposta.nomeRecebedor());
			assertEquals("52998224725", resposta.documentoRecebedor());

			assertEquals(MetodoVerificacaoAutorizacao.PRESENCIAL, resposta.metodoVerificacao());

			assertEquals(usuarioId, resposta.usuarioResponsavelId());
			assertEquals("Funcionário Carlos", resposta.usuarioResponsavelNome());

			assertNotNull(resposta.dataHoraRetirada());

			assertEquals(1, resposta.itens().size());
			assertEquals(itemReservaId, resposta.itens().get(0).itemReservaId());
			assertEquals(20L, resposta.itens().get(0).unidadeEquipamentoId());

			assertEquals(CondicaoFisicaRetirada.SEM_DANOS_APARENTES, resposta.itens().get(0).condicaoFisica());

			assertEquals(StatusReserva.EM_ANDAMENTO, reserva.getStatus());

			assertEquals(StatusUnidadeEquipamento.ALUGADO, unidade.getStatus());

			verify(reservaRepository).buscarPorIdComBloqueio(reservaId);

			verify(retiradaRepository).existsByReservaId(reservaId);

			verify(usuarioRepository).findById(usuarioId);

			verify(retiradaRepository).save(any(Retirada.class));

		} finally {

			SecurityContextHolder.clearContext();
		}
	}

	@Test
	void deveLancarExcecaoQuandoRetiradaNaoForEncontrada() {

		// PREPARAR
		Long retiradaId = 99L;

		// MOCKS
		when(retiradaRepository.findById(retiradaId)).thenReturn(Optional.empty());

		// EXECUTAR
		RetiradaNaoEncontradaException excecao = assertThrows(RetiradaNaoEncontradaException.class,
				() -> retiradaService.buscarPorId(retiradaId));

		// VERIFICAR
		assertNotNull(excecao);

		verify(retiradaRepository).findById(retiradaId);
	}

	@Test
	void deveBuscarRetiradaPorIdComSucesso() {

		// PREPARAR
		Long retiradaId = 30L;
		Long reservaId = 2L;
		Long usuarioId = 5L;
		Long itemReservaId = 10L;

		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		Reserva reserva = new Reserva(cliente, LocalDate.now(), LocalDate.now().plusDays(3));

		reserva.aprovar();

		ReflectionTestUtils.setField(reserva, "id", reservaId);

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("90.00"),
				categoria);

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);

		ReflectionTestUtils.setField(unidade, "id", 20L);
		ReflectionTestUtils.setField(unidade, "codigo", "UNI-000020");

		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);

		ReflectionTestUtils.setField(itemReserva, "id", itemReservaId);

		reserva.adicionarItem(itemReserva);

		Usuario usuario = new Usuario("Funcionário Carlos", "carlos@email.com", "senhaHash", PerfilUsuario.FUNCIONARIO);

		ReflectionTestUtils.setField(usuario, "id", usuarioId);

		OffsetDateTime dataHoraRetirada = OffsetDateTime.now();

		Retirada retirada = new Retirada(dataHoraRetirada, "João da Silva", "52998224725",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, reserva, usuario);

		ReflectionTestUtils.setField(retirada, "id", retiradaId);

		ItemRetirada itemRetirada = new ItemRetirada(itemReserva, CondicaoFisicaRetirada.SEM_DANOS_APARENTES, null);

		ReflectionTestUtils.setField(itemRetirada, "id", 40L);

		retirada.adicionarItem(itemRetirada);

		// MOCKS
		when(retiradaRepository.findById(retiradaId)).thenReturn(Optional.of(retirada));

		// EXECUTAR
		RetiradaResponse resposta = retiradaService.buscarPorId(retiradaId);

		// VERIFICAR
		assertNotNull(resposta);

		assertEquals(retiradaId, resposta.id());
		assertEquals(reservaId, resposta.reservaId());

		assertEquals(dataHoraRetirada, resposta.dataHoraRetirada());

		assertEquals("João da Silva", resposta.nomeRecebedor());
		assertEquals("52998224725", resposta.documentoRecebedor());

		assertEquals(MetodoVerificacaoAutorizacao.PRESENCIAL, resposta.metodoVerificacao());

		assertEquals(usuarioId, resposta.usuarioResponsavelId());
		assertEquals("Funcionário Carlos", resposta.usuarioResponsavelNome());

		assertEquals(1, resposta.itens().size());

		assertEquals(40L, resposta.itens().get(0).id());
		assertEquals(itemReservaId, resposta.itens().get(0).itemReservaId());
		assertEquals(20L, resposta.itens().get(0).unidadeEquipamentoId());

		assertEquals("UNI-000020", resposta.itens().get(0).codigoUnidade());

		assertEquals("Marreta", resposta.itens().get(0).equipamentoNome());

		assertEquals(CondicaoFisicaRetirada.SEM_DANOS_APARENTES, resposta.itens().get(0).condicaoFisica());

		verify(retiradaRepository).findById(retiradaId);

		verify(retiradaRepository, never()).save(any(Retirada.class));
	}

	@Test
	void deveListarRetiradasComPaginacao() {

		// ==================== PREPARAR ====================

		// 1. IDs utilizados no teste
		Long retiradaId = 30L;
		Long retiradaId2 = 40L;
		Long reservaId = 2L;
		Long reservaId2 = 3L;
		Long usuarioId = 5L;

		// 2. Cliente
		Cliente cliente = new Cliente(TipoCliente.PF, "João da Silva", null, "52998224725", "joao@email.com",
				"47999999999", "88330000", "Rua das Flores", "123", "Apto 202", "Centro", "Balneário Camboriú", "SC");

		// 3. Reservas
		LocalDate dataInicial = LocalDate.now();
		LocalDate dataFinal = dataInicial.plusDays(3);

		Reserva reserva = new Reserva(cliente, dataInicial, dataFinal);
		reserva.aprovar();
		ReflectionTestUtils.setField(reserva, "id", reservaId);

		Reserva reserva2 = new Reserva(cliente, dataInicial, dataFinal);
		reserva2.aprovar();
		ReflectionTestUtils.setField(reserva2, "id", reservaId2);

		// 4. Categoria e equipamento
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("90.00"),
				categoria);

		// 5. Unidades de equipamento
		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade, "id", 20L);
		ReflectionTestUtils.setField(unidade, "codigo", "UNI-000020");

		UnidadeEquipamento unidade2 = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade2, "id", 21L);
		ReflectionTestUtils.setField(unidade2, "codigo", "UNI-000021");

		// 6. Itens das reservas
		ItemReserva itemReserva = new ItemReserva(unidade, new BigDecimal("90.00"), 4L);
		ReflectionTestUtils.setField(itemReserva, "id", 10L);
		reserva.adicionarItem(itemReserva);

		ItemReserva itemReserva2 = new ItemReserva(unidade2, new BigDecimal("90.00"), 4L);
		ReflectionTestUtils.setField(itemReserva2, "id", 11L);
		reserva2.adicionarItem(itemReserva2);

		// 7. Funcionário responsável
		Usuario usuario = new Usuario("Funcionário Carlos", "carlos@email.com", "senhaHash", PerfilUsuario.FUNCIONARIO);
		ReflectionTestUtils.setField(usuario, "id", usuarioId);

		// 8. Retiradas
		OffsetDateTime dataHoraRetirada = OffsetDateTime.now();

		Retirada retirada = new Retirada(dataHoraRetirada, "João da Silva", "52998224725",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, reserva, usuario);
		ReflectionTestUtils.setField(retirada, "id", retiradaId);

		Retirada retirada2 = new Retirada(dataHoraRetirada, "João da Silva", "52998224725",
				MetodoVerificacaoAutorizacao.PRESENCIAL, null, reserva2, usuario);
		ReflectionTestUtils.setField(retirada2, "id", retiradaId2);

		// 9. Itens das retiradas
		ItemRetirada itemRetirada = new ItemRetirada(itemReserva, CondicaoFisicaRetirada.SEM_DANOS_APARENTES, null);
		ReflectionTestUtils.setField(itemRetirada, "id", 40L);
		retirada.adicionarItem(itemRetirada);

		ItemRetirada itemRetirada2 = new ItemRetirada(itemReserva2, CondicaoFisicaRetirada.SEM_DANOS_APARENTES, null);
		ReflectionTestUtils.setField(itemRetirada2, "id", 41L);
		retirada2.adicionarItem(itemRetirada2);

		// 10. Paginação
		Pageable paginacao = PageRequest.of(0, 2);

		Page<Retirada> paginaDeRetiradas = new PageImpl<>(List.of(retirada, retirada2), paginacao, 5);

		// ===================== MOCKS ======================

		when(retiradaRepository.findAll(paginacao)).thenReturn(paginaDeRetiradas);

		// ==================== EXECUTAR ====================

		PaginaResponse<RetiradaResponse> resposta = retiradaService.listarPaginado(paginacao);

		// ==================== VERIFICAR ===================

		// 1. Informações da paginação
		assertEquals(1, resposta.paginaAtual());
		assertEquals(2, resposta.tamanho());
		assertEquals(2, resposta.quantidadeElementos());
		assertEquals(5L, resposta.totalElementos());
		assertEquals(3, resposta.totalPaginas());

		assertTrue(resposta.primeiraPagina());
		assertFalse(resposta.ultimaPagina());

		// 2. Quantidade de retiradas retornadas
		assertEquals(2, resposta.conteudo().size());

		// 3. Primeira retirada
		RetiradaResponse primeiraRetirada = resposta.conteudo().get(0);

		assertEquals(retiradaId, primeiraRetirada.id());

		// 4. Segunda retirada
		RetiradaResponse segundaRetirada = resposta.conteudo().get(1);

		assertEquals(retiradaId2, segundaRetirada.id());

		assertEquals(reservaId, primeiraRetirada.reservaId());
		assertEquals("João da Silva", primeiraRetirada.nomeRecebedor());
		assertEquals("52998224725", primeiraRetirada.documentoRecebedor());
		assertEquals(usuarioId, primeiraRetirada.usuarioResponsavelId());
		assertEquals("Funcionário Carlos", primeiraRetirada.usuarioResponsavelNome());
		assertEquals(1, primeiraRetirada.itens().size());

		// 5. Interação com o repository
		verify(retiradaRepository).findAll(paginacao);
	}

	@Test
	void deveRetornarPaginaVaziaQuandoNaoExistiremRetiradas() {

		// PREPARAR
		Pageable paginacao = PageRequest.of(0, 10);

		Page<Retirada> paginaVazia = new PageImpl<>(List.of(), paginacao, 0);

		// MOCKS
		when(retiradaRepository.findAll(paginacao)).thenReturn(paginaVazia);

		// EXECUTAR

		PaginaResponse<RetiradaResponse> resposta = retiradaService.listarPaginado(paginacao);

		// VERIFICAR

		assertEquals(1, resposta.paginaAtual());
		assertEquals(10, resposta.tamanho());
		assertEquals(0, resposta.quantidadeElementos());
		assertEquals(0L, resposta.totalElementos());
		assertEquals(0, resposta.totalPaginas());

		assertTrue(resposta.primeiraPagina());
		assertTrue(resposta.ultimaPagina());
		assertTrue(resposta.conteudo().isEmpty());

		verify(retiradaRepository).findAll(paginacao);
	}

}
