package br.com.projetosecsr.aluguelequipamentos.usuario.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.EmailJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.repository.UsuarioRepository;
import br.com.projetosecsr.aluguelequipamentos.usuario.request.CadastrarUsuarioRequest;
import br.com.projetosecsr.aluguelequipamentos.usuario.response.UsuarioResponse;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public UsuarioResponse cadastrar(CadastrarUsuarioRequest request) {

		String nomeNormalizado = request.nome().strip();

		String emailNormalizado = request.email().strip().toLowerCase(Locale.ROOT);

		if (usuarioRepository.existsByEmail(emailNormalizado)) {

			throw new EmailJaCadastradoException();

		}

		String senhaHash = passwordEncoder.encode(request.senha());

		Usuario usuario = new Usuario(nomeNormalizado, emailNormalizado, senhaHash, request.perfil());

		Usuario usuarioSalvo = usuarioRepository.save(usuario);

		return UsuarioResponse.de(usuarioSalvo);
	}

}
