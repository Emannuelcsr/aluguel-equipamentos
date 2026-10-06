package br.com.projetosecsr.aluguelequipamentos.categoria.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.projetosecsr.aluguelequipamentos.categoria.request.AtualizarCategoriaRequest;
import br.com.projetosecsr.aluguelequipamentos.categoria.request.CadastrarCategoriaRequest;
import br.com.projetosecsr.aluguelequipamentos.categoria.response.CategoriaResponse;
import br.com.projetosecsr.aluguelequipamentos.categoria.service.CategoriaService;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

	private final CategoriaService categoriaService;

	public CategoriaController(CategoriaService categoriaService) {
		this.categoriaService = categoriaService;
	}

	@PostMapping
	public ResponseEntity<CategoriaResponse> cadastrar(@Valid @RequestBody CadastrarCategoriaRequest request) {

		CategoriaResponse response = categoriaService.cadastrar(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);

	}

	@GetMapping("/{id}")
	public ResponseEntity<CategoriaResponse> buscarPorId(@PathVariable Long id) {

		CategoriaResponse response = categoriaService.buscarPorId(id);

		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<PaginaResponse<CategoriaResponse>> listarPaginado(Pageable paginacao) {

		PaginaResponse<CategoriaResponse> resposta = categoriaService.listarPaginado(paginacao);

		return ResponseEntity.ok(resposta);
	}

	@PutMapping("/{id}")
	public ResponseEntity<CategoriaResponse> atualizar(@PathVariable Long id,
			@Valid @RequestBody AtualizarCategoriaRequest request) {

		CategoriaResponse response = categoriaService.atualizar(request, id);

		return ResponseEntity.ok(response);

	}

	@PatchMapping("/{id}/ativar")
	public ResponseEntity<CategoriaResponse> ativar(@PathVariable Long id) {

		CategoriaResponse response = categoriaService.ativar(id);

		return ResponseEntity.ok(response);

	}

	@PatchMapping("/{id}/desativar")
	public ResponseEntity<CategoriaResponse> desativar(@PathVariable Long id) {

		CategoriaResponse response = categoriaService.desativar(id);

		return ResponseEntity.ok(response);
	}

}
