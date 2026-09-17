package br.com.projetosecsr.aluguelequipamentos.autenticacao.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.projetosecsr.aluguelequipamentos.autenticacao.excecao.CredenciaisInvalidasException;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.excecao.UsuarioInativoException;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.request.LoginRequest;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.response.LoginResponse;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.response.UsuarioAutenticadoResponse;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.token.ServicoToken;
import br.com.projetosecsr.aluguelequipamentos.autenticacao.token.TokenGerado;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.repository.UsuarioRepository;

@Service
public class AutenticacaoService {

	private final UsuarioRepository usuarioRepository;

	private final PasswordEncoder passwordEncoder;

	private final ServicoToken servicoToken;

	public AutenticacaoService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
			ServicoToken servicoToken) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.servicoToken = servicoToken;
	}

	public LoginResponse autenticar(LoginRequest request) {

		String emailNormalizado = request.email().strip().toLowerCase(Locale.ROOT);
		Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
				.orElseThrow(CredenciaisInvalidasException::new);

		boolean senhaCorreta = passwordEncoder.matches(request.senha(), usuario.getSenhaHash());

		if (!senhaCorreta) {

			throw new CredenciaisInvalidasException();
		}

		if (!usuario.isAtivo()) {

			throw new UsuarioInativoException();
		}

		TokenGerado tokenGerado = servicoToken.gerar(usuario);

		return new LoginResponse(tokenGerado.valor(), "Bearer", tokenGerado.expiraEm(),
				UsuarioAutenticadoResponse.de(usuario));

	}

}
