package br.com.projetosecsr.aluguelequipamentos.usuario.controller;

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

import br.com.projetosecsr.aluguelequipamentos.usuario.request.AtualizarUsuarioRequest;
import br.com.projetosecsr.aluguelequipamentos.usuario.request.CadastrarUsuarioRequest;
import br.com.projetosecsr.aluguelequipamentos.usuario.response.UsuarioResponse;
import br.com.projetosecsr.aluguelequipamentos.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@PostMapping
	ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastrarUsuarioRequest request) {

		UsuarioResponse response = usuarioService.cadastrar(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);

	}

	@GetMapping("/{id}")
	ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {

		UsuarioResponse response = usuarioService.buscarPorId(id);

		return ResponseEntity.ok(response);
	}

	@GetMapping
	ResponseEntity<PaginaResponse<UsuarioResponse>> listarPaginado(Pageable paginacao) {

		PaginaResponse<UsuarioResponse> resposta = usuarioService.listarPaginado(paginacao);

		return ResponseEntity.ok(resposta);
	}

	@PutMapping("/{id}")
	ResponseEntity<UsuarioResponse> atualizar(@PathVariable Long id,
			@Valid @RequestBody AtualizarUsuarioRequest request) {

		UsuarioResponse response = usuarioService.atualizar(id, request);

		return ResponseEntity.ok(response);

	}

	@PatchMapping("/{id}/ativar")
	ResponseEntity<UsuarioResponse> ativar(@PathVariable Long id) {

		UsuarioResponse response = usuarioService.ativar(id);

		return ResponseEntity.ok(response);

	}

	@PatchMapping("/{id}/desativar")
	ResponseEntity<UsuarioResponse> desativar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {

		Long usuarioAutenticadoId = Long.valueOf(jwt.getSubject());

		UsuarioResponse response = usuarioService.desativar(id, usuarioAutenticadoId);

		return ResponseEntity.ok(response);
	}

}
