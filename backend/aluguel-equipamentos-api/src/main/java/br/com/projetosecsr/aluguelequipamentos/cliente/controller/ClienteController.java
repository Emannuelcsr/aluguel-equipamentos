package br.com.projetosecsr.aluguelequipamentos.cliente.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.projetosecsr.aluguelequipamentos.cliente.request.AtualizarClienteRequest;
import br.com.projetosecsr.aluguelequipamentos.cliente.request.CadastrarClienteRequest;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.ClienteResponse;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.ClienteResumoResponse;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.EnderecoCepResponse;
import br.com.projetosecsr.aluguelequipamentos.cliente.response.ViaCepResponse;
import br.com.projetosecsr.aluguelequipamentos.cliente.service.ClienteService;
import br.com.projetosecsr.aluguelequipamentos.cliente.service.ConsultaCepService;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.usuario.request.AtualizarUsuarioRequest;
import br.com.projetosecsr.aluguelequipamentos.usuario.response.UsuarioResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

	private final ClienteService clienteService;

	private final ConsultaCepService consultaCepService;

	public ClienteController(ClienteService clienteService, ConsultaCepService consultaCepService) {
		this.clienteService = clienteService;
		this.consultaCepService = consultaCepService;

	}

	@PostMapping
	public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody CadastrarClienteRequest request) {

		ClienteResponse response = clienteService.cadastrar(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);

	}

	@GetMapping("/{id}")
	public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {

		ClienteResponse response = clienteService.buscarPorId(id);

		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<PaginaResponse<ClienteResumoResponse>> listarPaginado(Pageable paginacao) {

		PaginaResponse<ClienteResumoResponse> resposta = clienteService.listarPaginado(paginacao);

		return ResponseEntity.ok(resposta);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ClienteResponse> atualizar(@PathVariable Long id,
			@Valid @RequestBody AtualizarClienteRequest request) {

		ClienteResponse response = clienteService.atualizar(request, id);

		return ResponseEntity.ok(response);

	}

	@PatchMapping("/{id}/ativar")
	public ResponseEntity<ClienteResponse> ativar(@PathVariable Long id) {

		ClienteResponse response = clienteService.ativar(id);

		return ResponseEntity.ok(response);

	}

	@PatchMapping("/{id}/desativar")
	public ResponseEntity<ClienteResponse> desativar(@PathVariable Long id) {

		ClienteResponse response = clienteService.desativar(id);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/cep/{cep}")
	public ResponseEntity<EnderecoCepResponse> consultaCep(@PathVariable String cep) {

		EnderecoCepResponse resposta = consultaCepService.consultar(cep);

		return ResponseEntity.ok(resposta);
	}

}
