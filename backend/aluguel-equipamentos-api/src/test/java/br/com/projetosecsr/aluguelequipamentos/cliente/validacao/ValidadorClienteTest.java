package br.com.projetosecsr.aluguelequipamentos.cliente.validacao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.TipoCliente;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.DocumentoIncompativelComTipoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.DocumentoInvalidoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.EstadoInvalidoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.NomeFantasiaNaoPermitidoException;

public class ValidadorClienteTest {

	private ValidadorCliente validadorCliente = new ValidadorCliente();

	@Test
	void deveLancarExcecaoQuandoPfTiverNomeFantasia() {

		NomeFantasiaNaoPermitidoException excecao = assertThrows(NomeFantasiaNaoPermitidoException.class,
				() -> validadorCliente.validarNomeFantasia(TipoCliente.PF, "nomeFantasiaTeste"));

		assertEquals("Nome fantasia não é permitido para cliente pessoa física.", excecao.getMessage());

	}

	@Test
	void naoDeveLancarExcecaoQuandoPjTiverNomeFantasia() {

		// EXECUTAR / VERIFICAR
		assertDoesNotThrow(() -> validadorCliente.validarNomeFantasia(TipoCliente.PJ, "Empresa Teste"));
	}

	@Test
	void pfComNomeFantasiaNullNaoDeveLancarExcecao() {

		// EXECUTAR / VERIFICAR
		assertDoesNotThrow(() -> validadorCliente.validarNomeFantasia(TipoCliente.PF, null));
	}

	@Test
	void pfComNomeFantasiaVazioOuSoEspaçosNaoDeveLancarExcecao() {

		// EXECUTAR / VERIFICAR
		assertDoesNotThrow(() -> validadorCliente.validarNomeFantasia(TipoCliente.PF, "  "));

	}

	@Test
	void deveLancarExcecaoQuandoPfReceberDocumentoComTamanhoIncompativel() {

		DocumentoIncompativelComTipoException excecao = assertThrows(DocumentoIncompativelComTipoException.class,
				() -> validadorCliente.validarDocumentoCompativelComTipo(TipoCliente.PF, "123"));

		assertEquals("Documento incompatível com o tipo do cliente.", excecao.getMessage());

	}

	@Test
	void deveLancarExcecaoQuandoPjReceberDocumentoComTamanhoIncompativel() {

		DocumentoIncompativelComTipoException excecao = assertThrows(DocumentoIncompativelComTipoException.class,
				() -> validadorCliente.validarDocumentoCompativelComTipo(TipoCliente.PJ, "123"));

		assertEquals("Documento incompatível com o tipo do cliente.", excecao.getMessage());

	}

	@Test
	void pfComDocumentoCompativelNaoDeveLancarExcecao() {

		// EXECUTAR / VERIFICAR
		assertDoesNotThrow(() -> validadorCliente.validarDocumentoCompativelComTipo(TipoCliente.PF, "12312313212"));

	}

	@Test
	void pjComDocumentoCompativelNaoDeveLancarExcecao() {

		// EXECUTAR / VERIFICAR
		assertDoesNotThrow(() -> validadorCliente.validarDocumentoCompativelComTipo(TipoCliente.PJ, "12312313111212"));

	}

	@Test
	void deveLancarExcecaoQuandoPfReceberDocumentoComLetras() {

		DocumentoInvalidoException excecao = assertThrows(DocumentoInvalidoException.class,
				() -> validadorCliente.validarDocumento(TipoCliente.PF, "1231231321a"));

		assertEquals("Documento inválido.", excecao.getMessage());

	}

	@Test
	void deveLancarExcecaoQuandoPjReceberDocumentoComLetras() {

		DocumentoInvalidoException excecao = assertThrows(DocumentoInvalidoException.class,
				() -> validadorCliente.validarDocumento(TipoCliente.PJ, "1231231311121a"));

		assertEquals("Documento inválido.", excecao.getMessage());

	}

	@Test
	void cpfValidoNaoDeveLancarExcecao() {

		// EXECUTAR / VERIFICAR
		assertDoesNotThrow(() -> validadorCliente.validarDocumento(TipoCliente.PF, "23869297018"));

	}

	@Test
	void cnpjValidoNaoDeveLancarExcecao() {

		// EXECUTAR / VERIFICAR
		assertDoesNotThrow(() -> validadorCliente.validarDocumento(TipoCliente.PJ, "83852078000161"));

	}

	@Test
	void deveLancarExcecaoQuandoPfReceberDocumentoComOsMesmosNumeros() {

		DocumentoInvalidoException excecao = assertThrows(DocumentoInvalidoException.class,
				() -> validadorCliente.validarDocumento(TipoCliente.PF, "11111111111"));

		assertEquals("Documento inválido.", excecao.getMessage());

	}

	@Test
	void deveLancarExcecaoQuandoPjReceberDocumentoComOsMesmosNumeros() {

		DocumentoInvalidoException excecao = assertThrows(DocumentoInvalidoException.class,
				() -> validadorCliente.validarDocumento(TipoCliente.PJ, "11111111111111"));

		assertEquals("Documento inválido.", excecao.getMessage());

	}

	@Test
	void estadoValidoNaoDeveLancarExcecao() {

		// EXECUTAR / VERIFICAR
		assertDoesNotThrow(() -> validadorCliente.validarEstado("SC"));

	}

	@Test
	void deveLancarExcecaoQuandoEstadoReceberSiglasIncompativeis() {

		EstadoInvalidoException excecao = assertThrows(EstadoInvalidoException.class,
				() -> validadorCliente.validarEstado("ZZ"));

		assertEquals("Estado inválido.", excecao.getMessage());

	}
}