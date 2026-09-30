package br.com.projetosecsr.aluguelequipamentos.cliente.validacao;

import org.springframework.stereotype.Component;

import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.TipoCliente;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.DocumentoIncompativelComTipoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.DocumentoInvalidoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.EstadoInvalidoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.NomeFantasiaNaoPermitidoException;

@Component
public class ValidadorCliente {

	private void validarCpf(String cpf) {

		boolean somenteNumeros = cpf.chars().allMatch(Character::isDigit);

		if (!somenteNumeros) {
			throw new DocumentoInvalidoException();
		}

		boolean todosNumerosIguais = cpf.chars().distinct().count() == 1;

		if (todosNumerosIguais) {
			throw new DocumentoInvalidoException();
		}

		int soma = 0;
		int peso = 10;

		for (int i = 0; i < 9; i++) {

			int numero = Character.getNumericValue(cpf.charAt(i));

			soma += numero * peso;

			peso--;
		}

		int resto = soma % 11;

		int primeiroDigito;

		if (resto < 2) {
			primeiroDigito = 0;
		} else {
			primeiroDigito = 11 - resto;
		}

		int primeiroDigitoInformado = Character.getNumericValue(cpf.charAt(9));

		if (primeiroDigito != primeiroDigitoInformado) {
			throw new DocumentoInvalidoException();
		}

		soma = 0;
		peso = 11;

		for (int i = 0; i < 10; i++) {

			int numero = Character.getNumericValue(cpf.charAt(i));

			soma += numero * peso;

			peso--;
		}

		resto = soma % 11;

		int segundoDigito;

		if (resto < 2) {
			segundoDigito = 0;
		} else {
			segundoDigito = 11 - resto;
		}

		int segundoDigitoInformado = Character.getNumericValue(cpf.charAt(10));

		if (segundoDigito != segundoDigitoInformado) {
			throw new DocumentoInvalidoException();
		}
	}

	private void validarCnpj(String cnpj) {

		boolean somenteNumeros = cnpj.chars().allMatch(Character::isDigit);

		if (!somenteNumeros) {
			throw new DocumentoInvalidoException();
		}

		boolean todosNumerosIguais = cnpj.chars().distinct().count() == 1;

		if (todosNumerosIguais) {
			throw new DocumentoInvalidoException();
		}

		int soma = 0;

		int[] pesosPrimeiroDigito = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };

		for (int i = 0; i < 12; i++) {

			int numero = Character.getNumericValue(cnpj.charAt(i));

			soma += numero * pesosPrimeiroDigito[i];
		}

		int resto = soma % 11;

		int primeiroDigito;

		if (resto < 2) {
			primeiroDigito = 0;
		} else {
			primeiroDigito = 11 - resto;
		}

		int primeiroDigitoInformado = Character.getNumericValue(cnpj.charAt(12));

		if (primeiroDigito != primeiroDigitoInformado) {
			throw new DocumentoInvalidoException();
		}

		soma = 0;

		int[] pesosSegundoDigito = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };

		for (int i = 0; i < 13; i++) {

			int numero = Character.getNumericValue(cnpj.charAt(i));

			soma += numero * pesosSegundoDigito[i];
		}

		resto = soma % 11;

		int segundoDigito;

		if (resto < 2) {
			segundoDigito = 0;
		} else {
			segundoDigito = 11 - resto;
		}

		int segundoDigitoInformado = Character.getNumericValue(cnpj.charAt(13));

		if (segundoDigito != segundoDigitoInformado) {
			throw new DocumentoInvalidoException();
		}
	}

	public void validarNomeFantasia(TipoCliente tipo, String nomeFantasia) {

		if (tipo == TipoCliente.PF && nomeFantasia != null && !nomeFantasia.isBlank()) {
			throw new NomeFantasiaNaoPermitidoException();
		}
	}

	public void validarDocumentoCompativelComTipo(TipoCliente tipo, String documento) {

		if (tipo == TipoCliente.PF && documento.length() != 11) {
			throw new DocumentoIncompativelComTipoException();
		}

		if (tipo == TipoCliente.PJ && documento.length() != 14) {
			throw new DocumentoIncompativelComTipoException();
		}

	}

	public void validarDocumento(TipoCliente tipo, String documento) {

		if (tipo == TipoCliente.PF) {
			validarCpf(documento);
		}

		if (tipo == TipoCliente.PJ) {
			validarCnpj(documento);
		}
	}

	public void validarEstado(String estado) {

		String[] estadosValidos = { "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
				"PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO" };

		boolean estadoValido = false;

		for (String estadoDaLista : estadosValidos) {

			if (estadoDaLista.equals(estado)) {
				estadoValido = true;
				break;
			}
		}

		if (!estadoValido) {
			throw new EstadoInvalidoException();
		}

	}

}
