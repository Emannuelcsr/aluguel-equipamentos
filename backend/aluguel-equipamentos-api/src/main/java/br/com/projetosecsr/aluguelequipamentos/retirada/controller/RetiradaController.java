package br.com.projetosecsr.aluguelequipamentos.retirada.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.retirada.request.CadastrarRetiradaRequest;
import br.com.projetosecsr.aluguelequipamentos.retirada.response.RetiradaResponse;
import br.com.projetosecsr.aluguelequipamentos.retirada.service.RetiradaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/retiradas")
public class RetiradaController {

	private final RetiradaService retiradaService;

	public RetiradaController(RetiradaService retiradaService) {
		this.retiradaService = retiradaService;
	}

	@PostMapping
	public ResponseEntity<RetiradaResponse> cadastrar(@Valid @RequestBody CadastrarRetiradaRequest request) {

		RetiradaResponse response = retiradaService.cadastrar(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);

	}

	@GetMapping("/{id}")
	public ResponseEntity<RetiradaResponse> buscarPorId(@PathVariable Long id) {

		RetiradaResponse resposta = retiradaService.buscarPorId(id);

		return ResponseEntity.ok(resposta);
	}

	@GetMapping
	public ResponseEntity<PaginaResponse<RetiradaResponse>> listarPaginado(Pageable paginacao) {

		PaginaResponse<RetiradaResponse> resposta = retiradaService.listarPaginado(paginacao);

		return ResponseEntity.ok(resposta);

	}

}
