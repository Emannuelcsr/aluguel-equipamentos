package br.com.projetosecsr.aluguelequipamentos.cliente.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projetosecsr.aluguelequipamentos.cliente.entidade.Cliente;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.ClienteNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.DocumentoJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.excecao.EmailClienteJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.cliente.repository.ClienteRepository;
import br.com.projetosecsr.aluguelequipamentos.cliente.request.CadastrarClienteRequest;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.ClienteResponse;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.ClienteResumoResponse;
import br.com.projetosecsr.aluguelequipamentos.cliente.validacao.ValidadorCliente;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;

@Service
public class ClienteService {

	private final ClienteRepository clienteRepository;

	private final ValidadorCliente validadorCliente;

	public ClienteService(ClienteRepository clienteRepository, ValidadorCliente validadorCliente) {
		this.clienteRepository = clienteRepository;
		this.validadorCliente = validadorCliente;
	}

	private String normalizarEstado(String estado) {

		String estadoNormalizado = estado.strip().toUpperCase(Locale.ROOT);

		return estadoNormalizado;
	}

	private String normalizarEmail(String email) {

		String emailNormalizado = email.strip().toLowerCase(Locale.ROOT);

		return emailNormalizado;
	}

	private String normalizarNomeRazaoSocial(String nomeRazaoSocial) {

		String nomeNormalizado = nomeRazaoSocial.strip();

		return nomeNormalizado;
	}

	private String normalizarNomeFantasia(String nomeFantasia) {

		if (nomeFantasia == null) {
			return null;
		}

		String nomeFantasiaNormalizado = nomeFantasia.strip();

		if (nomeFantasiaNormalizado.isBlank()) {
			return null;
		}

		return nomeFantasiaNormalizado;
	}

	private String normalizarComplemento(String complemento) {

		if (complemento == null) {
			return null;
		}

		String complementoNormalizado = complemento.strip();

		if (complementoNormalizado.isBlank()) {
			return null;
		}

		return complementoNormalizado;
	}

	private Cliente buscarClientePorId(Long id) {

		Optional<Cliente> clienteEncontrado = clienteRepository.findById(id);

		if (clienteEncontrado.isEmpty()) {
			throw new ClienteNaoEncontradoException();
		}

		Cliente cliente = clienteEncontrado.get();

		return cliente;
	}

	private PaginaResponse<ClienteResumoResponse> converterPagina(Page<ClienteResumoResponse> pagina) {

		if (pagina.getTotalPages() > 0 && pagina.getNumber() >= pagina.getTotalPages()) {

			throw new PaginaInvalidaException("A página informada não existe");
		}

		return new PaginaResponse<>(pagina.getContent(), pagina.getNumber() + 1, pagina.getSize(),
				pagina.getNumberOfElements(), pagina.getTotalElements(), pagina.getTotalPages(), pagina.isFirst(),
				pagina.isLast());
	}

//---------------------------------------------METODOS PUBLICOS ----------------------------------------------------------------------

	@Transactional
	public ClienteResponse cadastrar(CadastrarClienteRequest request) {

		String nomeRazaoSocial = normalizarNomeRazaoSocial(request.nomeRazaoSocial());

		String nomeFantasia = normalizarNomeFantasia(request.nomeFantasia());

		String email = normalizarEmail(request.email());

		String estado = normalizarEstado(request.estado());

		String complemento = normalizarComplemento(request.complemento());

		validadorCliente.validarNomeFantasia(request.tipo(), nomeFantasia);

		validadorCliente.validarDocumentoCompativelComTipo(request.tipo(), request.documento());

		validadorCliente.validarDocumento(request.tipo(), request.documento());

		validadorCliente.validarEstado(estado);

		if (clienteRepository.existsByDocumento(request.documento())) {
			throw new DocumentoJaCadastradoException();
		}

		if (clienteRepository.existsByEmail(email)) {
			throw new EmailClienteJaCadastradoException();
		}

		Cliente cliente = new Cliente(request.tipo(), nomeRazaoSocial, nomeFantasia, request.documento(), email,
				request.telefone(), request.cep(), request.logradouro(), request.numero(), complemento,
				request.bairro(), request.cidade(), estado);

		Cliente clienteSalvo = clienteRepository.save(cliente);

		return ClienteResponse.de(clienteSalvo);
	}

	@Transactional(readOnly = true)
	public ClienteResponse buscarPorId(Long id) {

		Cliente cliente = buscarClientePorId(id);

		ClienteResponse resposta = ClienteResponse.de(cliente);

		return resposta;
	}

	@Transactional(readOnly = true)
	public PaginaResponse<ClienteResumoResponse> listarPaginado(Pageable paginacao) {

		Page<Cliente> paginaDeClientes = clienteRepository.findAll(paginacao);

		Page<ClienteResumoResponse> paginaDeRespostas = paginaDeClientes
				.map(cliente -> ClienteResumoResponse.de(cliente));

		return converterPagina(paginaDeRespostas);

	}

}