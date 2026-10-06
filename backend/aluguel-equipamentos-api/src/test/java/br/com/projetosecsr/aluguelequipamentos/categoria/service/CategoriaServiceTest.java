package br.com.projetosecsr.aluguelequipamentos.categoria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaJaAtivaException;
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaJaCadastradaException;
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaJaInativaException;
import br.com.projetosecsr.aluguelequipamentos.categoria.excecao.CategoriaNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.categoria.repository.CategoriaRepository;
import br.com.projetosecsr.aluguelequipamentos.categoria.request.AtualizarCategoriaRequest;
import br.com.projetosecsr.aluguelequipamentos.categoria.request.CadastrarCategoriaRequest;
import br.com.projetosecsr.aluguelequipamentos.categoria.response.CategoriaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.response.UsuarioResponse;

@ExtendWith(MockitoExtension.class)
public class CategoriaServiceTest {

	@Mock
	private CategoriaRepository categoriaRepository;

	@InjectMocks
	private CategoriaService categoriaService;

	@Test
	void deveCadastrarCategoria() {

		// PREPARAR
		CadastrarCategoriaRequest request = new CadastrarCategoriaRequest("Marreta",
				"Marreta com cabo de madeira de 1 metro");

		when(categoriaRepository.existsByNome("MARRETA")).thenReturn(false);
		when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

		// EXECUTAR
		CategoriaResponse resposta = categoriaService.cadastrar(request);

		// VERIFICAR
		verify(categoriaRepository).existsByNome("MARRETA");

		ArgumentCaptor<Categoria> categoriaCaptor = ArgumentCaptor.forClass(Categoria.class);

		verify(categoriaRepository).save(categoriaCaptor.capture());

		Categoria categoriaEnviadoParaSalvar = categoriaCaptor.getValue();

		assertEquals("MARRETA", categoriaEnviadoParaSalvar.getNome());
		assertEquals("Marreta com cabo de madeira de 1 metro", categoriaEnviadoParaSalvar.getDescricao());
		assertTrue(categoriaEnviadoParaSalvar.isAtivo());
		assertTrue(resposta.ativo());
		assertEquals("MARRETA", resposta.nome());

	}

	@Test
	void deveRecusarCadastroQuandoNomeJaExistir() {

		// PREPARAR
		CadastrarCategoriaRequest request = new CadastrarCategoriaRequest("Marreta",
				"Marreta com cabo de madeira de 1 metro");

		when(categoriaRepository.existsByNome("MARRETA")).thenReturn(true);

		// EXECUTAR
		CategoriaJaCadastradaException excecao = assertThrows(CategoriaJaCadastradaException.class,
				() -> categoriaService.cadastrar(request));

		// VERIFICAR
		assertEquals("Categoria já cadastrada.", excecao.getMessage());

		verify(categoriaRepository).existsByNome("MARRETA");

		verify(categoriaRepository, never()).save(any(Categoria.class));

	}

	@Test
	void deveBuscarCategoriaPorIdQuandoCategoriaExistir() {

		// PREPARAR
		Long categoriaId = 10l;
		Categoria categoria = new Categoria("Marreta", "Marreta com cabo de madeira de 1 metro");
		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		// MOCKS
		when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoria));

		// EXECUTAR
		CategoriaResponse resposta = categoriaService.buscarPorId(categoriaId);

		// VERIFICAR
		verify(categoriaRepository).findById(categoriaId);

		assertEquals(categoriaId, resposta.id());
		assertEquals("Marreta", resposta.nome());
		assertEquals("Marreta com cabo de madeira de 1 metro", resposta.descricao());
		assertTrue(resposta.ativo());
	}

	@Test
	void deveLancarExcecaoQuandoCategoriaNaoForEncontradaPorId() {

		// PREPARAR
		Long categoriaId = 10l;

		// MOKS
		when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.empty());

		// EXECUTAR
		CategoriaNaoEncontradoException excecao = assertThrows(CategoriaNaoEncontradoException.class,
				() -> categoriaService.buscarPorId(categoriaId));

		// VERIFICAR
		assertEquals("Categoria não encontrada.", excecao.getMessage());

		verify(categoriaRepository).findById(categoriaId);

	}

	@Test
	void deveAtualizarCategoriaQuandoDadosForemValidos() {

		// PREPARAR
		Long categoriaId = 10L;
		Categoria categoria = new Categoria("MARRETA", "Marreta com cabo de madeira de 1 metro");
		ReflectionTestUtils.setField(categoria, "id", categoriaId);

		AtualizarCategoriaRequest categoriaAtualizada = new AtualizarCategoriaRequest(" MARRETA ATUALIZADA ",
				" Marreta com cabo de madeira de 1 metro atual ");

		// MOCKS
		when(categoriaRepository.findById(categoriaId)).thenReturn(Optional.of(categoria));
		when(categoriaRepository.existsByNomeAndIdNot("MARRETA ATUALIZADA", categoriaId)).thenReturn(false);

		// EXECUTAR
		CategoriaResponse resposta = categoriaService.atualizar(categoriaAtualizada, categoriaId);

		// VERIFICAR
		verify(categoriaRepository).findById(categoriaId);
		verify(categoriaRepository).existsByNomeAndIdNot("MARRETA ATUALIZADA", categoriaId);

		assertNotNull(resposta);
		assertEquals(categoriaId, resposta.id());
		assertEquals("MARRETA ATUALIZADA", resposta.nome());
		assertEquals("Marreta com cabo de madeira de 1 metro atual", resposta.descricao());

		verifyNoMoreInteractions(categoriaRepository);

	}

	@Test
	void deveLancarExcecaoQuandoNomePertencerAOutraCategoria() {
		// PREPARAR
		Long idCategoria = 1L;
		Categoria categoria = new Categoria("MARRETA ATUALIZADA", "Marreta com cabo de madeira");
		AtualizarCategoriaRequest categoriaAtualizada = new AtualizarCategoriaRequest(" MARRETA ATUALIZADA ",
				" Marreta com cabo de madeira de 1 metro atual ");
		// CORREÇÃO AQUI: Definir o ID na entidade usuario
		ReflectionTestUtils.setField(categoria, "id", idCategoria);

		when(categoriaRepository.findById(idCategoria)).thenReturn(Optional.of(categoria));
		when(categoriaRepository.existsByNomeAndIdNot("MARRETA ATUALIZADA", idCategoria)).thenReturn(true);

		// EXECUTAR & VERIFICAR
		assertThrows(CategoriaJaCadastradaException.class,
				() -> categoriaService.atualizar(categoriaAtualizada, idCategoria));

		verify(categoriaRepository).findById(idCategoria);
		verify(categoriaRepository).existsByNomeAndIdNot("MARRETA ATUALIZADA", idCategoria);
	}

	@Test
	void deveAtivarCategoriaQuandoEstiverInativo() {
		// PREPARAR
		Long idCat = 1L;
		Categoria categoria = new Categoria("MARRETA ATUALIZADA", "Marreta com cabo de madeira");
		ReflectionTestUtils.setField(categoria, "id", idCat);
		categoria.desativar();

		// MOCKS
		when(categoriaRepository.findById(idCat)).thenReturn(Optional.of(categoria));

		// EXECUTAR
		CategoriaResponse resposta = categoriaService.ativar(idCat);

		// VERIFICAR
		verify(categoriaRepository).findById(idCat);

		assertTrue(categoria.isAtivo());
		assertTrue(resposta.ativo());
		assertEquals(idCat, resposta.id());

	}

	@Test
	void deveDesativarCategoriaComSucesso() {
		// PREPARAR
		Long idCat = 1L;

		Categoria categoria = new Categoria("MARRETA ATUALIZADA", "Marreta com cabo de madeira");
		ReflectionTestUtils.setField(categoria, "id", idCat);

		// MOCKS
		when(categoriaRepository.findById(idCat)).thenReturn(Optional.of(categoria));

		// EXECUTAR
		CategoriaResponse resposta = categoriaService.desativar(idCat);

		// VERIFICAR
		assertNotNull(resposta);
		assertEquals(idCat, resposta.id());
		assertFalse(resposta.ativo());
		assertFalse(categoria.isAtivo());

		verify(categoriaRepository).findById(idCat);
		verifyNoMoreInteractions(categoriaRepository);
	}

	@Test
	void deveLancarExcecaoQuandoCategoriaJaEstiverAtiva() {
		// PREPARAR
		Long idCat = 1L;
		Categoria categoria = new Categoria("MARRETA ATUALIZADA", "Marreta com cabo de madeira");
		ReflectionTestUtils.setField(categoria, "id", idCat);

		// MOCKS
		when(categoriaRepository.findById(idCat)).thenReturn(Optional.of(categoria));

		// EXECUTAR
		CategoriaJaAtivaException excecao = assertThrows(CategoriaJaAtivaException.class,
				() -> categoriaService.ativar(idCat));

		// VERIFICAR
		assertEquals("Categoria já está ativa.", excecao.getMessage());

		verify(categoriaRepository).findById(idCat);

		assertTrue(categoria.isAtivo());

	}

	@Test
	void deveLancarExcecaoQuandoCategoriaJaEstiverInativa() {
		// PREPARAR
		Long idCat = 1L;

		Categoria categoria = new Categoria("MARRETA ATUALIZADA", "Marreta com cabo de madeira");
		ReflectionTestUtils.setField(categoria, "id", idCat);
		categoria.desativar();

		// MOCKS
		when(categoriaRepository.findById(idCat)).thenReturn(Optional.of(categoria));

		// EXECUTAR
		CategoriaJaInativaException excecao = assertThrows(CategoriaJaInativaException.class,
				() -> categoriaService.desativar(idCat));

		// VERIFICAR
		assertEquals("Categoria já está inativa.", excecao.getMessage());

		verify(categoriaRepository).findById(idCat);

		assertFalse(categoria.isAtivo());

	}

	@Test
	void deveListarCategoriasComPaginacao() {

		// PREPARAR
		Long idCat1 = 1L;
		Long idCat2 = 2L;

		Pageable paginacao = PageRequest.of(0, 2);

		Categoria categoria1 = new Categoria("Mouse", "Mouse com fio");
		ReflectionTestUtils.setField(categoria1, "id", idCat1);

		Categoria categoria2 = new Categoria("Teclado", "Teclado com fio");
		ReflectionTestUtils.setField(categoria2, "id", idCat2);

		Page<Categoria> paginaDeCategorias = new PageImpl<>(List.of(categoria1, categoria2), paginacao, 5);

		// MOCKS
		when(categoriaRepository.findAll(paginacao)).thenReturn(paginaDeCategorias);

		// EXECUTAR
		PaginaResponse<CategoriaResponse> resposta = categoriaService.listarPaginado(paginacao);

		// VERIFICAR
		assertEquals(1, resposta.paginaAtual());
		assertEquals(2, resposta.tamanho());
		assertEquals(2, resposta.quantidadeElementos());
		assertEquals(5L, resposta.totalElementos());
		assertEquals(3, resposta.totalPaginas());
		assertTrue(resposta.primeiraPagina());
		assertFalse(resposta.ultimaPagina());
		assertEquals(2, resposta.conteudo().size());

		CategoriaResponse cat1 = resposta.conteudo().get(0);

		assertEquals(idCat1, cat1.id());
		assertEquals("Mouse", cat1.nome());

		CategoriaResponse cat2 = resposta.conteudo().get(1);

		assertEquals(idCat2, cat2.id());
		assertEquals("Teclado", cat2.nome());

		verify(categoriaRepository).findAll(paginacao);

	}

	@Test
	void deveLancarExcecaoQuandoPaginaSolicitadaNaoExistir() {

		// PREPARAR
		Pageable paginacao = PageRequest.of(3, 2);

		Page<Categoria> paginaVazia = new PageImpl<>(List.of(), paginacao, 5);

		// MOCKS
		when(categoriaRepository.findAll(paginacao)).thenReturn(paginaVazia);

		PaginaInvalidaException excecao = assertThrows(PaginaInvalidaException.class,
				() -> categoriaService.listarPaginado(paginacao));

		assertEquals("A página informada não existe", excecao.getMessage());

		verify(categoriaRepository).findAll(paginacao);

	}

}
