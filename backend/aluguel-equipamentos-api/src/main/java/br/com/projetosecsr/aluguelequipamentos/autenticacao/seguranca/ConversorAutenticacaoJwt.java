package br.com.projetosecsr.aluguelequipamentos.autenticacao.seguranca;

import java.util.List;
import java.util.Optional;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.repository.UsuarioRepository;

@Component
public class ConversorAutenticacaoJwt implements Converter<Jwt, AbstractAuthenticationToken> {

	private final UsuarioRepository usuarioRepository;

	public ConversorAutenticacaoJwt(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public AbstractAuthenticationToken convert(Jwt source) {

		Long usuarioId;

		try {

			usuarioId = Long.parseLong(source.getSubject());

		} catch (NumberFormatException e) {
			throw new BadCredentialsException("Autenticação inválida", e);
		}

		Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);

		if (!usuario.isPresent()) {
			throw new BadCredentialsException("Autenticação inválida");
		}

		Usuario usuarioEncontrado = usuario.get();

		if (!usuarioEncontrado.isAtivo()) {
			throw new BadCredentialsException("Autenticação inválida");
		}

		String autoridade = "ROLE_" + usuarioEncontrado.getPerfil().name();

		SimpleGrantedAuthority permissao = new SimpleGrantedAuthority(autoridade);

		return new JwtAuthenticationToken(source, List.of(permissao));
	}
}
