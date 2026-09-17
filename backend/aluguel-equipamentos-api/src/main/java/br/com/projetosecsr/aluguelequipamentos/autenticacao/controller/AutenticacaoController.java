package br.com.projetosecsr.aluguelequipamentos.autenticacao.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.request.LoginRequest;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.response.LoginResponse;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.service.AutenticacaoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/autenticacao")
public class AutenticacaoController {

	private final AutenticacaoService autenticacaoService;

	public AutenticacaoController(AutenticacaoService autenticacaoService) {
		this.autenticacaoService = autenticacaoService;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

		LoginResponse response = autenticacaoService.autenticar(request);

		return ResponseEntity.ok(response);

	}

}
