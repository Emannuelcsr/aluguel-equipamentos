package br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.request.CadastrarUnidadeEquipamentoRequest;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.response.UnidadeEquipamentoResponse;
import br.com.projetosecsr.aluguelequipamentos.unidadeequipamento.service.UnidadeEquipamentoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/unidades-equipamento")
public class UnidadeEquipamentoController {

	private final UnidadeEquipamentoService unidadeEquipamentoService;

	public UnidadeEquipamentoController(UnidadeEquipamentoService unidadeEquipamentoService) {
		this.unidadeEquipamentoService = unidadeEquipamentoService;
	}

	@PostMapping
	public ResponseEntity<UnidadeEquipamentoResponse> cadastrar(
			@Valid @RequestBody CadastrarUnidadeEquipamentoRequest request) {

		UnidadeEquipamentoResponse response = unidadeEquipamentoService.cadastrar(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);

	}

	@GetMapping("/{id}")
	public ResponseEntity<UnidadeEquipamentoResponse> buscarPorId(@PathVariable Long id) {

		UnidadeEquipamentoResponse response = unidadeEquipamentoService.buscarPorId(id);

		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<PaginaResponse<UnidadeEquipamentoResponse>> listarPaginado(Pageable paginacao) {

		PaginaResponse<UnidadeEquipamentoResponse> resposta = unidadeEquipamentoService.listarPaginado(paginacao);

		return ResponseEntity.ok(resposta);
	}

	@PatchMapping("/{id}/ativar")
	public ResponseEntity<UnidadeEquipamentoResponse> ativar(@PathVariable Long id) {

		UnidadeEquipamentoResponse response = unidadeEquipamentoService.ativar(id);

		return ResponseEntity.ok(response);

	}

	@PatchMapping("/{id}/desativar")
	public ResponseEntity<UnidadeEquipamentoResponse> desativar(@PathVariable Long id) {

		UnidadeEquipamentoResponse response = unidadeEquipamentoService.desativar(id);

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{id}/manutencao")
	public ResponseEntity<UnidadeEquipamentoResponse> manutencao(@PathVariable Long id) {

		UnidadeEquipamentoResponse response = unidadeEquipamentoService.enviarParaManutencao(id);

		return ResponseEntity.ok(response);

	}

	@PatchMapping("/{id}/liberar-manutencao")
	public ResponseEntity<UnidadeEquipamentoResponse> liberarManutencao(@PathVariable Long id) {

		UnidadeEquipamentoResponse response = unidadeEquipamentoService.liberarDaManutencao(id);

		return ResponseEntity.ok(response);
	}

}
