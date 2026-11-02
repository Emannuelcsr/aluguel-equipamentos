package br.com.projetosecsr.aluguelequipamentos.devolucao.controller;

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
import br.com.projetosecsr.aluguelequipamentos.devolucao.request.CadastrarDevolucaoRequest;
import br.com.projetosecsr.aluguelequipamentos.devolucao.response.DevolucaoResponse;
import br.com.projetosecsr.aluguelequipamentos.devolucao.service.DevolucaoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/devolucoes")
public class DevolucaoController {

	private final DevolucaoService devolucaoService;

	public DevolucaoController(DevolucaoService devolucaoService) {
		this.devolucaoService = devolucaoService;
	}

	@PostMapping
	ResponseEntity<DevolucaoResponse> cadastrar(@Valid @RequestBody CadastrarDevolucaoRequest request) {

		DevolucaoResponse response = devolucaoService.cadastrar(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);

	}

	@GetMapping("/{id}")
	ResponseEntity<DevolucaoResponse> buscarPorId(@PathVariable Long id) {

		DevolucaoResponse response = devolucaoService.buscarPorId(id);

		return ResponseEntity.ok(response);
	}

	@GetMapping
	ResponseEntity<PaginaResponse<DevolucaoResponse>> listarPaginado(Pageable paginacao) {

		PaginaResponse<DevolucaoResponse> resposta = devolucaoService.listarPaginado(paginacao);

		return ResponseEntity.ok(resposta);
	}

}
