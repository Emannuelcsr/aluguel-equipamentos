package br.com.projetosecsr.aluguelequipamentos.compartilhado.configuracao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;

public class ConfiguracaoJwtTest {

	private JwtEncoder jwtEncoder;

	private JwtDecoder jwtDecoder;

	@BeforeEach
	void preparar() {

		String segredoEmTexto = "12345678901234567890123456789012";

		byte[] segredoEmBytes = segredoEmTexto.getBytes(StandardCharsets.UTF_8);

		String segredoEmBase64 = Base64.getEncoder().encodeToString(segredoEmBytes);

		ConfiguracaoJwt configuracaoJwt = new ConfiguracaoJwt(segredoEmBase64);

		SecretKey chaveJwt = configuracaoJwt.chaveJwt();

		this.jwtEncoder = configuracaoJwt.jwtEncoder(chaveJwt);

		this.jwtDecoder = configuracaoJwt.jwtDecoder(chaveJwt, "aluguel-equipamentos-api");
	}

	@Test
	void deveAceitarTokenValido() {

		// PREPARAR
		Instant emitidoEm = Instant.now();

		Instant expiraEm = emitidoEm.plus(5, ChronoUnit.MINUTES);

		JwtClaimsSet claims = JwtClaimsSet.builder().issuer("aluguel-equipamentos-api").issuedAt(emitidoEm)
				.expiresAt(expiraEm).subject("1").claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();

		JwtEncoderParameters parametros = JwtEncoderParameters.from(cabecalho, claims);

		// MOCKS
		// Este teste não possui mocks.

		// EXECUTAR
		String token = jwtEncoder.encode(parametros).getTokenValue();

		Jwt jwtDecodificado = assertDoesNotThrow(() -> jwtDecoder.decode(token));

		// VERIFICAR
		assertEquals("aluguel-equipamentos-api", jwtDecodificado.getClaimAsString("iss"));

		assertEquals("1", jwtDecodificado.getSubject());

		assertEquals(PerfilUsuario.ADMINISTRADOR.name(), jwtDecodificado.getClaimAsString("perfil"));
	}

	@Test
	void deveRejeitarTokenQuandoEmissorForIncorreto() {

		// PREPARAR
		Instant emitidoEm = Instant.now();

		Instant expiraEm = emitidoEm.plus(5, ChronoUnit.MINUTES);

		JwtClaimsSet claims = JwtClaimsSet.builder().issuer("outra-aplicacao").issuedAt(emitidoEm).expiresAt(expiraEm)
				.subject("1").claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();

		JwtEncoderParameters parametros = JwtEncoderParameters.from(cabecalho, claims);

		// MOCKS
		// Este teste não possui mocks.

		// EXECUTAR
		String token = jwtEncoder.encode(parametros).getTokenValue();

		// VERIFICAR
		assertThrows(JwtValidationException.class, () -> jwtDecoder.decode(token));
	}

	@Test
	void deveRejeitarTokenQuandoEstiverExpirado() {

		// PREPARAR
		Instant agora = Instant.now();

		Instant emitidoEm = agora.minus(10, ChronoUnit.MINUTES);

		Instant expiraEm = agora.minus(5, ChronoUnit.MINUTES);

		JwtClaimsSet claims = JwtClaimsSet.builder().issuer("aluguel-equipamentos-api").issuedAt(emitidoEm)
				.expiresAt(expiraEm).subject("1").claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();

		JwtEncoderParameters parametros = JwtEncoderParameters.from(cabecalho, claims);

		// MOCKS
		// Este teste não possui mocks.

		// EXECUTAR
		String token = jwtEncoder.encode(parametros).getTokenValue();

		// VERIFICAR
		assertThrows(JwtValidationException.class, () -> jwtDecoder.decode(token));
	}

	@Test
	void deveRejeitarTokenQuandoAssinaturaForIncorreta() {

		// PREPARAR
		String outroSegredoEmTexto = "abcdef1234567890abcdef1234567890";

		byte[] outroSegredoEmBytes = outroSegredoEmTexto.getBytes(StandardCharsets.UTF_8);

		String outroSegredoEmBase64 = Base64.getEncoder().encodeToString(outroSegredoEmBytes);

		ConfiguracaoJwt outraConfiguracaoJwt = new ConfiguracaoJwt(outroSegredoEmBase64);

		SecretKey outraChaveJwt = outraConfiguracaoJwt.chaveJwt();

		JwtEncoder outroJwtEncoder = outraConfiguracaoJwt.jwtEncoder(outraChaveJwt);

		Instant emitidoEm = Instant.now();

		Instant expiraEm = emitidoEm.plus(5, ChronoUnit.MINUTES);

		JwtClaimsSet claims = JwtClaimsSet.builder().issuer("aluguel-equipamentos-api").issuedAt(emitidoEm)
				.expiresAt(expiraEm).subject("1").claim("perfil", PerfilUsuario.ADMINISTRADOR.name()).build();

		JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();

		JwtEncoderParameters parametros = JwtEncoderParameters.from(cabecalho, claims);

		// MOCKS
		// Este teste não possui mocks.

		// EXECUTAR
		String token = outroJwtEncoder.encode(parametros).getTokenValue();

		// VERIFICAR
		assertThrows(JwtException.class, () -> jwtDecoder.decode(token));
	}
}