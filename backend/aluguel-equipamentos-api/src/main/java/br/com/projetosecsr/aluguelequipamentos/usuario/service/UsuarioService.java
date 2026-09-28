package br.com.projetosecsr.aluguelequipamentos.usuario.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.PaginaResponse;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.EmailJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.UsuarioNaoEncontradoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.repository.UsuarioRepository;
import br.com.projetosecsr.aluguelequipamentos.usuario.request.AtualizarUsuarioRequest;
import br.com.projetosecsr.aluguelequipamentos.usuario.request.CadastrarUsuarioRequest;
import br.com.projetosecsr.aluguelequipamentos.usuario.response.UsuarioResponse;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	private PaginaResponse<UsuarioResponse> converterPagina(Page<UsuarioResponse> pagina) {

		if (pagina.getTotalPages() > 0 && pagina.getNumber() >= pagina.getTotalPages()) {

			throw new PaginaInvalidaException("A página informada não existe");
		}

		return new PaginaResponse<>(pagina.getContent(), pagina.getNumber() + 1, pagina.getSize(),
				pagina.getNumberOfElements(), pagina.getTotalElements(), pagina.getTotalPages(), pagina.isFirst(),
				pagina.isLast());
	}

	private Usuario buscarUsuarioPorId(Long id) {

		Optional<Usuario> usuarioEncontrado = usuarioRepository.findById(id);

		if (usuarioEncontrado.isEmpty()) {

			throw new UsuarioNaoEncontradoException();
		}

		Usuario usuario = usuarioEncontrado.get();

		return usuario;
	}

	private String normalizarNome(String nome) {

		String nomeNormalizado = nome.strip();

		return nomeNormalizado;

	}

	private String normalizarEmail(String email) {

		String emailNormalizado = email.strip().toLowerCase(Locale.ROOT);

		return emailNormalizado;
	}

	@Transactional
	public UsuarioResponse cadastrar(CadastrarUsuarioRequest request) {

		String nomeNormalizado = normalizarNome(request.nome());

		String emailNormalizado = normalizarEmail(request.email());

		if (usuarioRepository.existsByEmail(emailNormalizado)) {

			throw new EmailJaCadastradoException();

		}

		String senhaHash = passwordEncoder.encode(request.senha());

		Usuario usuario = new Usuario(nomeNormalizado, emailNormalizado, senhaHash, request.perfil());

		Usuario usuarioSalvo = usuarioRepository.save(usuario);

		return UsuarioResponse.de(usuarioSalvo);
	}

	@Transactional(readOnly = true)
	public UsuarioResponse buscarPorId(Long id) {

		Usuario usuario = buscarUsuarioPorId(id);

		UsuarioResponse resposta = UsuarioResponse.de(usuario);

		return resposta;

	}

	@Transactional(readOnly = true)
	public PaginaResponse<UsuarioResponse> listarPaginado(Pageable paginacao) {

		Page<Usuario> paginaDeUsuarios = usuarioRepository.findAll(paginacao);

		Page<UsuarioResponse> paginaDeRespostas = paginaDeUsuarios.map(usuario -> UsuarioResponse.de(usuario));

		return converterPagina(paginaDeRespostas);

	}

	@Transactional
	public UsuarioResponse atualizar(Long id, AtualizarUsuarioRequest request) {

		Usuario usuario = buscarUsuarioPorId(id);

		String nomeNormalizado = normalizarNome(request.nome());

		String emailNormalizado = normalizarEmail(request.email());

		if (usuarioRepository.existsByEmailAndIdNot(emailNormalizado, usuario.getId())) {

			throw new EmailJaCadastradoException();

		}

		usuario.atualizarDados(nomeNormalizado, emailNormalizado, request.perfil());

		return UsuarioResponse.de(usuario);
	}

}
