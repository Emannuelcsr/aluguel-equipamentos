package br.com.projetosecsr.aluguelequipamentos.equipamento.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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

@ExtendWith(MockitoExtension.class)
public class EquipamentoServiceTest {

	@Mock
	private EquipamentoRepository equipamentoRepository;

	@Mock
	CategoriaRepository categoriaRepository;

	@InjectMocks
	private EquipamentoService equipamentoService;

	@Test
	void deveCadastrarEquipamento() {

		// PREPARAR
		CadastrarEquipamentoRequest request = new CadastrarEquipamentoRequest("Marreta",
				"Marreta com cabo de madeira de 1 metro", new BigDecimal("900.00"), 1L);

		Long categoriaId = 1l;
		Categoria categoria = new Categoria("Marreta", "Marreta com cabo de madeira de 1 metro");
		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoria));
		when(equipamentoRepository.save(any(Equipamento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

		// EXECUTAR
		EquipamentoResponse resposta = equipamentoService.cadastrar(request);

		// VERIFICAR
		ArgumentCaptor<Equipamento> equipamentoCaptor = ArgumentCaptor.forClass(Equipamento.class);

		verify(equipamentoRepository).save(equipamentoCaptor.capture());

		Equipamento equipamentoEnviadoParaSalvar = equipamentoCaptor.getValue();

		assertEquals("Marreta", equipamentoEnviadoParaSalvar.getNome());
		assertEquals("Marreta com cabo de madeira de 1 metro", equipamentoEnviadoParaSalvar.getDescricao());
		assertEquals(new BigDecimal("900.00"), equipamentoEnviadoParaSalvar.getValorDiaria());
		assertEquals(1L, equipamentoEnviadoParaSalvar.getCategoria().getId());

	}

	@Test
	void deveRecusarCadastroQuandoCategoriaNaoExistir() {

		// PREPARAR
		CadastrarEquipamentoRequest request = new CadastrarEquipamentoRequest("Marreta",
				"Marreta com cabo de madeira de 1 metro", new BigDecimal("900.00"), 1L);

		Long categoriaId = 1l;

		when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.empty());

		// EXECUTAR
		CategoriaNaoEncontradoException excecao = assertThrows(CategoriaNaoEncontradoException.class,
				() -> equipamentoService.cadastrar(request));

		// VERIFICAR
		assertEquals("Categoria não encontrada.", excecao.getMessage());

		verify(equipamentoRepository, never()).save(any(Equipamento.class));

		verify(categoriaRepository).findById(categoriaId);
	}

	@Test
	void deveRecusarCadastroQuandoCategoriaEstiverInativa() {

		// PREPARAR
		CadastrarEquipamentoRequest request = new CadastrarEquipamentoRequest("Marreta",
				"Marreta com cabo de madeira de 1 metro", new BigDecimal("900.00"), 1L);

		Long categoriaId = 1l;
		Categoria categoria = new Categoria("Marreta", "Marreta com cabo de madeira de 1 metro");
		categoria.desativar();
		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoria));

		// EXECUTAR
		CategoriaJaInativaException excecao = assertThrows(CategoriaJaInativaException.class,
				() -> equipamentoService.cadastrar(request));

		// VERIFICAR
		assertEquals("Categoria já está inativa.", excecao.getMessage());

		verify(equipamentoRepository, never()).save(any(Equipamento.class));

		verify(categoriaRepository).findById(categoriaId);
	}

	@Test
	void deveBuscarEquipamentoPorIdQuandoCategoriaExistir() {

		//
		Long categoriaId = 1L;

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas manuais");

		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		Long equipamentoId = 10L;

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira de 1 metro",
				new BigDecimal("900.00"), categoria);

		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);

		when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.of(equipamento));

		// EXECUTAR
		EquipamentoResponse resposta = equipamentoService.buscarPorId(equipamentoId);

		// VERIFICAR
		verify(equipamentoRepository).findById(equipamentoId);

		assertEquals(equipamentoId, resposta.id());
		assertEquals("Marreta", resposta.nome());
		assertEquals("Marreta com cabo de madeira de 1 metro", resposta.descricao());
		assertEquals(categoriaId, resposta.categoriaId());
		assertEquals(new BigDecimal("900.00"), resposta.valorDiaria());
		assertEquals("FERRAMENTAS", resposta.categoriaNome());

	}

	@Test
	void deveLancarExcecaoQuandoEquipamentoNaoForEncontradoPorId() {

		// PREPARAR
		Long equipamentoId = 999L;

		// MOKS
		when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.empty());

		// EXECUTAR
		EquipamentoNaoEncontradoException excecao = assertThrows(EquipamentoNaoEncontradoException.class,
				() -> equipamentoService.buscarPorId(equipamentoId));

		// VERIFICAR
		assertEquals("Equipamento não encontrado.", excecao.getMessage());

		verify(equipamentoRepository).findById(equipamentoId);

	}

	@Test
	void deveAtualizarEquipamentoQuandoDadosForemValidos() {

		// PREPARAR
		Long categoriaId = 1L;

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas manuais");

		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		Long equipamentoId = 10L;

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira de 1 metro",
				new BigDecimal("900.00"), categoria);

		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);

		AtualizarEquipamentoRequest equipamentoAtualizado = new AtualizarEquipamentoRequest(" Marreta Atualizada ",
				" Marreta com cabo de madeira de 1 metro ATUAL ", new BigDecimal("900.00"), categoriaId);

		// MOCKS
		when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoria));
		when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.of(equipamento));

		// EXECUTAR
		EquipamentoResponse resposta = equipamentoService.atualizar(equipamentoId, equipamentoAtualizado);

		// VERIFICAR
		verify(equipamentoRepository).findById(equipamentoId);

		assertNotNull(resposta);
		assertEquals(equipamentoId, resposta.id());
		assertEquals("Marreta Atualizada", resposta.nome());
		assertEquals("Marreta com cabo de madeira de 1 metro ATUAL", resposta.descricao());
		assertEquals(new BigDecimal("900.00"), resposta.valorDiaria());
		assertEquals(categoriaId, resposta.categoriaId());
		assertEquals("FERRAMENTAS", resposta.categoriaNome());

		verifyNoMoreInteractions(equipamentoRepository);
		verify(categoriaRepository).findById(categoriaId);

	}

	@Test
	void deveLancarExcecaoQuandoAtualizarEquipamentoSemCategoria() {

		// PREPARAR
		Long categoriaId = 1L;

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas manuais");

		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		Long equipamentoId = 10L;

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira de 1 metro",
				new BigDecimal("900.00"), categoria);

		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);

		AtualizarEquipamentoRequest equipamentoAtualizado = new AtualizarEquipamentoRequest(" Marreta Atualizada ",
				" Marreta com cabo de madeira de 1 metro ATUAL ", new BigDecimal("900.00"), categoriaId);

		// MOCKS
		when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.empty());
		when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.of(equipamento));

		// EXECUTAR
		CategoriaNaoEncontradoException excecao = assertThrows(CategoriaNaoEncontradoException.class,
				() -> equipamentoService.atualizar(equipamentoId, equipamentoAtualizado));

		// VERIFICAR
		assertEquals("Categoria não encontrada.", excecao.getMessage());

		verify(equipamentoRepository).findById(equipamentoId);
		verify(categoriaRepository).findById(categoriaId);
		assertEquals("Marreta", equipamento.getNome());
		assertEquals("Marreta com cabo de madeira de 1 metro", equipamento.getDescricao());
		assertEquals(new BigDecimal("900.00"), equipamento.getValorDiaria());
		assertEquals(categoriaId, equipamento.getCategoria().getId());

	}

	@Test
	void deveLancarExcecaoQuandoCategoriaEstiverInativaNaAtualizacao() {

		// PREPARAR
		Long categoriaId = 1L;

		Categoria categoriaAtual = new Categoria("FERRAMENTAS", "Ferramentas manuais");
		ReflectionTestUtils.setField(categoriaAtual, "id", categoriaId);

		Long equipamentoId = 10L;

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira de 1 metro",
				new BigDecimal("900.00"), categoriaAtual);

		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);

		Categoria categoriaInativa = new Categoria("CONSTRUÇÃO", "Equipamentos de construção");
		ReflectionTestUtils.setField(categoriaInativa, "id", 2L);
		categoriaInativa.desativar();

		AtualizarEquipamentoRequest equipamentoAtualizado = new AtualizarEquipamentoRequest(" Marreta Atualizada ",
				" Marreta com cabo de madeira de 1 metro ATUAL ", new BigDecimal("950.00"), 2L);

		// MOCKS
		when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.of(equipamento));

		when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoriaInativa));

		// EXECUTAR
		CategoriaJaInativaException excecao = assertThrows(CategoriaJaInativaException.class,
				() -> equipamentoService.atualizar(equipamentoId, equipamentoAtualizado));

		// VERIFICAR
		assertEquals("Categoria já está inativa.", excecao.getMessage());

		verify(equipamentoRepository).findById(equipamentoId);
		verify(categoriaRepository).findById(2L);

		assertEquals("Marreta", equipamento.getNome());
		assertEquals("Marreta com cabo de madeira de 1 metro", equipamento.getDescricao());
		assertEquals(new BigDecimal("900.00"), equipamento.getValorDiaria());
		assertEquals(categoriaId, equipamento.getCategoria().getId());
	}

	@Test
	void deveAtivarEquipamentoQuandoEstiverInativo() {

		// PREPARAR
		Long equipamentoId = 10L;

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas manuais");
		ReflectionTestUtils.setField(categoria, "id", 1L);

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("900.00"),
				categoria);

		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);
		equipamento.desativar();

		// MOCKS
		when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.of(equipamento));

		// EXECUTAR
		EquipamentoResponse resposta = equipamentoService.ativar(equipamentoId);

		// VERIFICAR
		assertTrue(equipamento.isAtivo());
		assertTrue(resposta.ativo());
		assertEquals(equipamentoId, resposta.id());

		verify(equipamentoRepository).findById(equipamentoId);
	}

	@Test
	void deveDesativarEquipamentoQuandoEstiverAtivo() {

		// PREPARAR
		Long equipamentoId = 10L;

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas manuais");
		ReflectionTestUtils.setField(categoria, "id", 1L);

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("900.00"),
				categoria);

		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);

		// MOCKS
		when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.of(equipamento));

		// EXECUTAR
		EquipamentoResponse resposta = equipamentoService.desativar(equipamentoId);

		// VERIFICAR
		assertFalse(equipamento.isAtivo());
		assertFalse(resposta.ativo());
		assertEquals(equipamentoId, resposta.id());

		verify(equipamentoRepository).findById(equipamentoId);
	}

	@Test
	void deveLancarExcecaoQuandoEquipamentoJaEstiverAtivo() {

		// PREPARAR
		Long equipamentoId = 10L;

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas manuais");
		ReflectionTestUtils.setField(categoria, "id", 1L);

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("900.00"),
				categoria);

		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);

		// MOCKS
		when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.of(equipamento));

		// EXECUTAR
		EquipamentoJaAtivoException excecao = assertThrows(EquipamentoJaAtivoException.class,
				() -> equipamentoService.ativar(equipamentoId));

		// VERIFICAR
		assertEquals("O equipamento já está ativo.", excecao.getMessage());
		assertTrue(equipamento.isAtivo());

		verify(equipamentoRepository).findById(equipamentoId);
	}

	@Test
	void deveLancarExcecaoQuandoEquipamentoJaEstiverInativo() {

		// PREPARAR
		Long equipamentoId = 10L;

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas manuais");
		ReflectionTestUtils.setField(categoria, "id", 1L);

		Equipamento equipamento = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("900.00"),
				categoria);

		ReflectionTestUtils.setField(equipamento, "id", equipamentoId);
		equipamento.desativar();

		// MOCKS
		when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.of(equipamento));

		// EXECUTAR
		EquipamentoJaInativoException excecao = assertThrows(EquipamentoJaInativoException.class,
				() -> equipamentoService.desativar(equipamentoId));

		// VERIFICAR
		assertEquals("O equipamento já está inativo.", excecao.getMessage());
		assertFalse(equipamento.isAtivo());

		verify(equipamentoRepository).findById(equipamentoId);
	}

	@Test
	void deveListarEquipamentosComPaginacao() {

		// PREPARAR
		Long categoriaId = 1L;

		Categoria categoria = new Categoria("FERRAMENTAS", "Ferramentas manuais");

		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		Long equipamentoId1 = 10L;
		Long equipamentoId2 = 20L;

		Equipamento equipamento1 = new Equipamento("Marreta", "Marreta com cabo de madeira", new BigDecimal("50.00"),
				categoria);

		ReflectionTestUtils.setField(equipamento1, "id", equipamentoId1);

		Equipamento equipamento2 = new Equipamento("Furadeira", "Furadeira elétrica", new BigDecimal("80.00"),
				categoria);

		ReflectionTestUtils.setField(equipamento2, "id", equipamentoId2);

		Pageable paginacao = PageRequest.of(0, 2);

		Page<Equipamento> paginaDeEquipamentos = new PageImpl<>(List.of(equipamento1, equipamento2), paginacao, 5);

		// MOCKS
		when(equipamentoRepository.findAll(paginacao)).thenReturn(paginaDeEquipamentos);

		// EXECUTAR
		PaginaResponse<EquipamentoResponse> resposta = equipamentoService.listarPaginado(paginacao);

		// VERIFICAR
		assertEquals(1, resposta.paginaAtual());
		assertEquals(2, resposta.tamanho());
		assertEquals(2, resposta.quantidadeElementos());
		assertEquals(5L, resposta.totalElementos());
		assertEquals(3, resposta.totalPaginas());

		assertTrue(resposta.primeiraPagina());
		assertFalse(resposta.ultimaPagina());

		assertEquals(2, resposta.conteudo().size());

		EquipamentoResponse primeiroEquipamento = resposta.conteudo().get(0);

		assertEquals(equipamentoId1, primeiroEquipamento.id());
		assertEquals("Marreta", primeiroEquipamento.nome());
		assertEquals(new BigDecimal("50.00"), primeiroEquipamento.valorDiaria());
		assertEquals(categoriaId, primeiroEquipamento.categoriaId());
		assertEquals("FERRAMENTAS", primeiroEquipamento.categoriaNome());

		EquipamentoResponse segundoEquipamento = resposta.conteudo().get(1);

		assertEquals(equipamentoId2, segundoEquipamento.id());
		assertEquals("Furadeira", segundoEquipamento.nome());
		assertEquals(new BigDecimal("80.00"), segundoEquipamento.valorDiaria());

		verify(equipamentoRepository).findAll(paginacao);
	}

	@Test
	void deveLancarExcecaoQuandoPaginaSolicitadaNaoExistir() {

		// PREPARAR
		Pageable paginacao = PageRequest.of(3, 2);

		Page<Equipamento> paginaVazia = new PageImpl<>(List.of(), paginacao, 5);

		// MOCKS
		when(equipamentoRepository.findAll(paginacao)).thenReturn(paginaVazia);

		// EXECUTAR
		PaginaInvalidaException excecao = assertThrows(PaginaInvalidaException.class,
				() -> equipamentoService.listarPaginado(paginacao));

		// VERIFICAR
		assertEquals("A página informada não existe", excecao.getMessage());

		verify(equipamentoRepository).findAll(paginacao);
	}

}
