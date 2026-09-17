package br.com.projetosecsr.aluguelequipamentos.autenticacao.token;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;

@Service
public class ServicoToken {

	private final JwtEncoder jwtEncoder;
	private final String emissor;
	private final long expiracaoMinutos;

	public ServicoToken(JwtEncoder jwtEncoder, @Value("${app.jwt.emissor}") String emissor,
			@Value("${app.jwt.expiracao-minutos}") long expiracaoMinutos) {

		this.jwtEncoder = jwtEncoder;
		this.emissor = emissor;
		this.expiracaoMinutos = expiracaoMinutos;
	}

	public TokenGerado gerar(Usuario usuario) {

		Instant emitidoEm = Instant.now();

		Instant expiraEm = emitidoEm.plus(expiracaoMinutos, ChronoUnit.MINUTES);

		JwtClaimsSet claims = JwtClaimsSet.builder().issuer(emissor).issuedAt(emitidoEm).expiresAt(expiraEm)
				.subject(usuario.getId().toString()).claim("perfil", usuario.getPerfil().name()).build();

		JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();

		JwtEncoderParameters parametros = JwtEncoderParameters.from(cabecalho, claims);

		String valor = jwtEncoder.encode(parametros).getTokenValue();

		return new TokenGerado(valor, expiraEm);
	}

}
