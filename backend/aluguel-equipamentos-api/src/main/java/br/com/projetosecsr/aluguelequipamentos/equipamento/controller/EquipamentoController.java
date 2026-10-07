package br.com.projetosecsr.aluguelequipamentos.equipamento.controller;

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

import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.equipamento.request.AtualizarEquipamentoRequest;
import br.com.projetosecsr.aluguelequipamentos.equipamento.request.CadastrarEquipamentoRequest;
import br.com.projetosecsr.aluguelequipamentos.equipamento.response.EquipamentoResponse;
import br.com.projetosecsr.aluguelequipamentos.equipamento.service.EquipamentoService;
import br.com.projetosecsr.aluguelequipamentos.usuario.request.AtualizarUsuarioRequest;
import br.com.projetosecsr.aluguelequipamentos.usuario.response.UsuarioResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/equipamentos")
public class EquipamentoController {

	private final EquipamentoService equipamentoService;

	public EquipamentoController(EquipamentoService equipamentoService) {
		this.equipamentoService = equipamentoService;
	}

	@PostMapping
	public ResponseEntity<EquipamentoResponse> cadastrar(@Valid @RequestBody CadastrarEquipamentoRequest request) {

		EquipamentoResponse response = equipamentoService.cadastrar(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);

	}

	@GetMapping("/{id}")
	public ResponseEntity<EquipamentoResponse> buscarPorId(@PathVariable Long id) {

		EquipamentoResponse response = equipamentoService.buscarPorId(id);

		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<PaginaResponse<EquipamentoResponse>> listarPaginado(Pageable paginacao) {

		PaginaResponse<EquipamentoResponse> resposta = equipamentoService.listarPaginado(paginacao);

		return ResponseEntity.ok(resposta);
	}

	@PutMapping("/{id}")
	public ResponseEntity<EquipamentoResponse> atualizar(@PathVariable Long id,
			@Valid @RequestBody AtualizarEquipamentoRequest request) {

		EquipamentoResponse response = equipamentoService.atualizar(id, request);

		return ResponseEntity.ok(response);

	}

	@PatchMapping("/{id}/ativar")
	public ResponseEntity<EquipamentoResponse> ativar(@PathVariable Long id) {

		EquipamentoResponse response = equipamentoService.ativar(id);

		return ResponseEntity.ok(response);

	}

	@PatchMapping("/{id}/desativar")
	public ResponseEntity<EquipamentoResponse> desativar(@PathVariable Long id) {

		EquipamentoResponse response = equipamentoService.desativar(id);

		return ResponseEntity.ok(response);
	}

}
