package br.com.projetosecsr.aluguelequipamentos.reserva.controller;

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
import br.com.projetosecsr.aluguelequipamentos.reserva.request.CadastrarReservaRequest;
import br.com.projetosecsr.aluguelequipamentos.reserva.response.ReservaResponse;
import br.com.projetosecsr.aluguelequipamentos.reserva.service.ReservaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

	private final ReservaService reservaService;

	public ReservaController(ReservaService reservaService) {
		this.reservaService = reservaService;
	}

	@PostMapping
	public ResponseEntity<ReservaResponse> cadastrar(@Valid @RequestBody CadastrarReservaRequest request) {

		ReservaResponse response = reservaService.cadastrar(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);

	}

	@GetMapping("/{id}")
	public ResponseEntity<ReservaResponse> buscarPorId(@PathVariable Long id) {

		ReservaResponse response = reservaService.buscarPorId(id);

		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<PaginaResponse<ReservaResponse>> listarPaginado(Pageable paginacao) {

		PaginaResponse<ReservaResponse> resposta = reservaService.listarPaginado(paginacao);

		return ResponseEntity.ok(resposta);
	}

	@PatchMapping("/{id}/aprovar")
	public ResponseEntity<ReservaResponse> aprovar(@PathVariable Long id) {

		ReservaResponse response = reservaService.aprovar(id);

		return ResponseEntity.ok(response);

	}

	@PatchMapping("/{id}/rejeitar")
	public ResponseEntity<ReservaResponse> rejeitar(@PathVariable Long id) {

		ReservaResponse response = reservaService.rejeitar(id);

		return ResponseEntity.ok(response);

	}

	@PatchMapping("/{id}/cancelar")
	public ResponseEntity<ReservaResponse> cancelar(@PathVariable Long id) {

		ReservaResponse response = reservaService.cancelar(id);

		return ResponseEntity.ok(response);

	}

}
