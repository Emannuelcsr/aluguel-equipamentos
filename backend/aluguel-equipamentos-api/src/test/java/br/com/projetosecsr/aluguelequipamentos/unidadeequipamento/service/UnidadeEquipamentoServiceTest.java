package br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.projetosecsr.aluguelequipamentos.categoria.entidade.Categoria;
import br.com.projetosecsr.aluguelequipamentos.categoria.repository.CategoriaRepository;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.entidade.Equipamento;
import br.com.projetosecsr.aluguelequipamentos.equipamento.excecao.EquipamentoJaInativoException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.excecao.EquipamentoNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.equipamento.repository.EquipamentoRepository;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.StatusUnidadeEquipamento;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.entidade.UnidadeEquipamento;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.OperacaoInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoJaAtivaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoJaInativaException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.excecao.UnidadeEquipamentoNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.repository.UnidadeEquipamentoRepository;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.request.CadastrarUnidadeEquipamentoRequest;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.response.UnidadeEquipamentoResponse;

@ExtendWith(MockitoExtension.class)
public class UnidadeEquipamentoServiceTest {

	@Mock
	private UnidadeEquipamentoRepository unidadeEquipamentoRepository;

	@Mock
	private EquipamentoRepository equipamentoRepository;

	@Mock
	CategoriaRepository categoriaRepository;

	@InjectMocks
	private UnidadeEquipamentoService unidadeEquipamentoService;

	@Test
	void deveCadastrarEquipamento() {

		Long categoriaId = 1L;
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");
		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		Long equipamentoId = 2L;
		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira de 1 metro",
				new BigDecimal("900.00"), categoria);
		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);

		CadastrarUnidadeEquipamentoRequest request = new CadastrarUnidadeEquipamentoRequest(equipamentoId);

		when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.of(equipamento));

		when(unidadeEquipamentoRepository.save(any(UnidadeEquipamento.class)))
				.thenAnswer(invocacao -> invocacao.getArgument(0));

		UnidadeEquipamentoResponse resposta = unidadeEquipamentoService.cadastrar(request);

		ArgumentCaptor<UnidadeEquipamento> captor = ArgumentCaptor.forClass(UnidadeEquipamento.class);

		verify(unidadeEquipamentoRepository).save(captor.capture());

		UnidadeEquipamento unidadeCriada = captor.getValue();

		assertEquals(equipamentoId, unidadeCriada.getEquipamento().getId());
		assertEquals(StatusUnidadeEquipamento.DISPONIVEL, unidadeCriada.getStatus());
		verify(equipamentoRepository).findById(equipamentoId);

	}

	@Test
	void deveRecusarCadastroQuandoEquipamentoNaoExistir() {

		// PREPARAR
		Long categoriaId = 1L;
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");
		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		Long equipamentoId = 2L;
		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira de 1 metro",
				new BigDecimal("900.00"), categoria);
		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);

		CadastrarUnidadeEquipamentoRequest request = new CadastrarUnidadeEquipamentoRequest(equipamentoId);

		when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.empty());

		// EXECUTAR
		EquipamentoNaoEncontradoException excecao = assertThrows(EquipamentoNaoEncontradoException.class,
				() -> unidadeEquipamentoService.cadastrar(request));

		// VERIFICAR
		assertEquals("Equipamento não encontrado.", excecao.getMessage());

		verify(unidadeEquipamentoRepository, never()).save(any(UnidadeEquipamento.class));

		verify(equipamentoRepository).findById(equipamentoId);
	}

	@Test
	void deveRecusarCadastroQuandoEquipamentoEstiverInativo() {

		// PREPARAR
		Long categoriaId = 1L;
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");
		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		Long equipamentoId = 2L;
		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira de 1 metro",
				new BigDecimal("900.00"), categoria);
		equipamento.desativar();
		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);

		CadastrarUnidadeEquipamentoRequest request = new CadastrarUnidadeEquipamentoRequest(equipamentoId);

		when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.of(equipamento));

		// EXECUTAR
		EquipamentoJaInativoException excecao = assertThrows(EquipamentoJaInativoException.class,
				() -> unidadeEquipamentoService.cadastrar(request));

		// VERIFICAR
		assertEquals("O equipamento já está inativo.", excecao.getMessage());

		verify(unidadeEquipamentoRepository, never()).save(any(UnidadeEquipamento.class));

		verify(equipamentoRepository).findById(equipamentoId);
	}

	@Test
	void deveBuscarUnidadeEquipamentoPorIdQuandoEquipamentoExistir() {

		//
		// PREPARAR
		Long categoriaId = 1L;
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");
		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		Long equipamentoId = 2L;
		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira de 1 metro",
				new BigDecimal("900.00"), categoria);
		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);

		UnidadeEquipamento unidadeEquipamento = new UnidadeEquipamento(equipamento);
		Long unidadeEquipamentoId = 3L;
		ReflectionTestUtils.setField(unidadeEquipamento, "id", unidadeEquipamentoId);

		when(unidadeEquipamentoRepository.findById(unidadeEquipamentoId)).thenReturn(Optional.of(unidadeEquipamento));

		// EXECUTAR
		UnidadeEquipamentoResponse resposta = unidadeEquipamentoService.buscarPorId(unidadeEquipamentoId);

		// VERIFICAR
		verify(unidadeEquipamentoRepository).findById(unidadeEquipamentoId);

		assertEquals(unidadeEquipamentoId, resposta.id());
		assertEquals(equipamentoId, resposta.equipamentoId());
		assertEquals("Marreta", resposta.equipamentoNome());

	}

	@Test
	void deveLancarExcecaoQuandoEquipamentoNaoForEncontradoPorId() {

		// PREPARAR
		Long unidadeEquipamentoId = 999L;

		// MOKS
		when(unidadeEquipamentoRepository.findById(unidadeEquipamentoId)).thenReturn(Optional.empty());

		// EXECUTAR
		UnidadeEquipamentoNaoEncontradoException excecao = assertThrows(UnidadeEquipamentoNaoEncontradoException.class,
				() -> unidadeEquipamentoService.buscarPorId(unidadeEquipamentoId));

		// VERIFICAR
		assertEquals("Unidade de equipamento não encontrada.", excecao.getMessage());

		verify(unidadeEquipamentoRepository).findById(unidadeEquipamentoId);

	}

	@Test
	void deveListarEquipamentosComPaginacao() {

		// PREPARAR
		Long categoriaId = 1L;
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");
		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		Long equipamentoId1 = 10L;
		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira de 1 metro",
				new BigDecimal("900.00"), categoria);
		ReflectionTestUtils.setField(equipamento, "id", equipamentoId1);

		Long equipamentoId2 = 20L;
		Equipamento equipamento1 = new Equipamento("Marreta", "Marreta com cabo de madeira de 1 metro",
				new BigDecimal("900.00"), categoria);
		ReflectionTestUtils.setField(equipamento1, "id", equipamentoId2);

		UnidadeEquipamento unidadeEquipamento1 = new UnidadeEquipamento(equipamento);
		Long unidadeEquipamentoId1 = 3L;
		ReflectionTestUtils.setField(unidadeEquipamento1, "id", unidadeEquipamentoId1);

		UnidadeEquipamento unidadeEquipamento2 = new UnidadeEquipamento(equipamento1);
		Long unidadeEquipamentoId2 = 4L;
		ReflectionTestUtils.setField(unidadeEquipamento2, "id", unidadeEquipamentoId2);

		Pageable paginacao = PageRequest.of(0, 2);

		Page<UnidadeEquipamento> paginaDeEquipamentos = new PageImpl<>(
				List.of(unidadeEquipamento1, unidadeEquipamento2), paginacao, 5);

		// MOCKS
		when(unidadeEquipamentoRepository.findAll(paginacao)).thenReturn(paginaDeEquipamentos);

		// EXECUTAR
		PaginaResponse<UnidadeEquipamentoResponse> resposta = unidadeEquipamentoService.listarPaginado(paginacao);

		// VERIFICAR
		assertEquals(1, resposta.paginaAtual());
		assertEquals(2, resposta.tamanho());
		assertEquals(2, resposta.quantidadeElementos());
		assertEquals(5L, resposta.totalElementos());
		assertEquals(3, resposta.totalPaginas());

		assertTrue(resposta.primeiraPagina());
		assertFalse(resposta.ultimaPagina());

		assertEquals(2, resposta.conteudo().size());

		UnidadeEquipamentoResponse primeiroEquipamento = resposta.conteudo().get(0);

		assertEquals(unidadeEquipamentoId1, primeiroEquipamento.id());

		UnidadeEquipamentoResponse segundoEquipamento = resposta.conteudo().get(1);

		assertEquals(unidadeEquipamentoId2, segundoEquipamento.id());

		verify(unidadeEquipamentoRepository).findAll(paginacao);
	}

	@Test
	void deveLancarExcecaoQuandoPaginaSolicitadaNaoExistir() {

		// PREPARAR
		Pageable paginacao = PageRequest.of(3, 2);

		Page<UnidadeEquipamento> paginaVazia = new PageImpl<>(List.of(), paginacao, 5);

		// MOCKS
		when(unidadeEquipamentoRepository.findAll(paginacao)).thenReturn(paginaVazia);

		// EXECUTAR
		PaginaInvalidaException excecao = assertThrows(PaginaInvalidaException.class,
				() -> unidadeEquipamentoService.listarPaginado(paginacao));

		// VERIFICAR
		assertEquals("A página informada não existe", excecao.getMessage());

		verify(unidadeEquipamentoRepository).findAll(paginacao);
	}

	@Test
	void deveAtivarUnidadeEquipamentoQuandoEstiverInativa() {

		// PREPARAR
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("900.00"),
				categoria);

		Long unidadeId = 3L;

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade, "id", unidadeId);
		unidade.desativar();

		// MOCKS
		when(unidadeEquipamentoRepository.findById(unidadeId)).thenReturn(Optional.of(unidade));

		// EXECUTAR
		UnidadeEquipamentoResponse resposta = unidadeEquipamentoService.ativar(unidadeId);

		// VERIFICAR
		assertTrue(resposta.ativo());

		verify(unidadeEquipamentoRepository).findById(unidadeId);
	}

	@Test
	void deveRecusarAtivacaoQuandoUnidadeJaEstiverAtiva() {

		// PREPARAR
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("900.00"),
				categoria);

		Long unidadeId = 3L;

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade, "id", unidadeId);

		// MOCKS
		when(unidadeEquipamentoRepository.findById(unidadeId)).thenReturn(Optional.of(unidade));

		// EXECUTAR
		UnidadeEquipamentoJaAtivaException excecao = assertThrows(UnidadeEquipamentoJaAtivaException.class,
				() -> unidadeEquipamentoService.ativar(unidadeId));

		// VERIFICAR
		assertEquals("A unidade do equipamento já está ativa.", excecao.getMessage());

		verify(unidadeEquipamentoRepository).findById(unidadeId);
	}

	@Test
	void deveDesativarUnidadeEquipamentoQuandoEstiverAtiva() {

		// PREPARAR
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("900.00"),
				categoria);

		Long unidadeId = 3L;

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade, "id", unidadeId);

		// MOCKS
		when(unidadeEquipamentoRepository.findById(unidadeId)).thenReturn(Optional.of(unidade));

		// EXECUTAR
		UnidadeEquipamentoResponse resposta = unidadeEquipamentoService.desativar(unidadeId);

		// VERIFICAR
		assertFalse(resposta.ativo());

		verify(unidadeEquipamentoRepository).findById(unidadeId);
	}

	@Test
	void deveRecusarDesativacaoQuandoUnidadeJaEstiverInativa() {

		// PREPARAR
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("900.00"),
				categoria);

		Long unidadeId = 3L;

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade, "id", unidadeId);
		unidade.desativar();

		// MOCKS
		when(unidadeEquipamentoRepository.findById(unidadeId)).thenReturn(Optional.of(unidade));

		// EXECUTAR
		UnidadeEquipamentoJaInativaException excecao = assertThrows(UnidadeEquipamentoJaInativaException.class,
				() -> unidadeEquipamentoService.desativar(unidadeId));

		// VERIFICAR
		assertEquals("A unidade de equipamento já está inativa.", excecao.getMessage());

		verify(unidadeEquipamentoRepository).findById(unidadeId);
	}

	@Test
	void deveRecusarDesativacaoQuandoUnidadeEstiverAlugada() {

		// PREPARAR
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("900.00"),
				categoria);

		Long unidadeId = 3L;

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade, "id", unidadeId);
		ReflectionTestUtils.setField(unidade, "status", StatusUnidadeEquipamento.ALUGADO);

		// MOCKS
		when(unidadeEquipamentoRepository.findById(unidadeId)).thenReturn(Optional.of(unidade));

		// EXECUTAR
		OperacaoInvalidaException excecao = assertThrows(OperacaoInvalidaException.class,
				() -> unidadeEquipamentoService.desativar(unidadeId));

		// VERIFICAR
		assertEquals("A unidade não pode ser desativada se estiver alugada.", excecao.getMessage());

		assertTrue(unidade.isAtivo());

		verify(unidadeEquipamentoRepository).findById(unidadeId);
	}

	@Test
	void deveEnviarUnidadeParaManutencaoQuandoEstiverDisponivel() {

		// PREPARAR
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("900.00"),
				categoria);

		Long unidadeId = 3L;

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade, "id", unidadeId);

		// MOCKS
		when(unidadeEquipamentoRepository.findById(unidadeId)).thenReturn(Optional.of(unidade));

		// EXECUTAR
		UnidadeEquipamentoResponse resposta = unidadeEquipamentoService.enviarParaManutencao(unidadeId);

		// VERIFICAR
		assertEquals("EM_MANUTENCAO", resposta.status());

		verify(unidadeEquipamentoRepository).findById(unidadeId);
	}

	@Test
	void deveRecusarEnvioParaManutencaoQuandoUnidadeEstiverAlugada() {

		// PREPARAR
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("900.00"),
				categoria);

		Long unidadeId = 3L;

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade, "id", unidadeId);
		ReflectionTestUtils.setField(unidade, "status", StatusUnidadeEquipamento.ALUGADO);

		// MOCKS
		when(unidadeEquipamentoRepository.findById(unidadeId)).thenReturn(Optional.of(unidade));

		// EXECUTAR
		OperacaoInvalidaException excecao = assertThrows(OperacaoInvalidaException.class,
				() -> unidadeEquipamentoService.enviarParaManutencao(unidadeId));

		// VERIFICAR
		assertEquals("A unidade só pode ser enviada para manutenção quando estiver disponível.", excecao.getMessage());

		assertEquals(StatusUnidadeEquipamento.ALUGADO, unidade.getStatus());

		verify(unidadeEquipamentoRepository).findById(unidadeId);
	}

	@Test
	void deveLiberarUnidadeDaManutencaoQuandoEstiverEmManutencao() {

		// PREPARAR
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("900.00"),
				categoria);

		Long unidadeId = 3L;

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade, "id", unidadeId);

		unidade.enviarParaManutencao();

		// MOCKS
		when(unidadeEquipamentoRepository.findById(unidadeId)).thenReturn(Optional.of(unidade));

		// EXECUTAR
		UnidadeEquipamentoResponse resposta = unidadeEquipamentoService.liberarDaManutencao(unidadeId);

		// VERIFICAR
		assertEquals("DISPONIVEL", resposta.status());

		verify(unidadeEquipamentoRepository).findById(unidadeId);
	}

	@Test
	void deveRecusarLiberacaoDaManutencaoQuandoUnidadeNaoEstiverEmManutencao() {

		// PREPARAR
		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas gerais");

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("900.00"),
				categoria);

		Long unidadeId = 3L;

		UnidadeEquipamento unidade = new UnidadeEquipamento(equipamento);
		ReflectionTestUtils.setField(unidade, "id", unidadeId);

		// MOCKS
		when(unidadeEquipamentoRepository.findById(unidadeId)).thenReturn(Optional.of(unidade));

		// EXECUTAR
		OperacaoInvalidaException excecao = assertThrows(OperacaoInvalidaException.class,
				() -> unidadeEquipamentoService.liberarDaManutencao(unidadeId));

		// VERIFICAR
		assertEquals("A unidade só pode sair da manutenção se estiver em manutenção.", excecao.getMessage());

		assertEquals(StatusUnidadeEquipamento.DISPONIVEL, unidade.getStatus());

		verify(unidadeEquipamentoRepository).findById(unidadeId);
	}

}
