package br.com.projetosecsr.aluguelequipamentos.autenticacao.token;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;

public class ServicoTokenTest {

	private ServicoToken servicoToken;

	private JwtDecoder jwtDecoder;

	@BeforeEach
	void preparar() {

		byte[] chaveEmBytes = "12345678901234567890123456789012".getBytes(StandardCharsets.UTF_8);

		SecretKey chave = new SecretKeySpec(chaveEmBytes, "HmacSHA256");

		JwtEncoder jwtEncoder = NimbusJwtEncoder.withSecretKey(chave).algorithm(MacAlgorithm.HS256).build();

		this.jwtDecoder = NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();

		this.servicoToken = new ServicoToken(jwtEncoder, "aluguel-equipamentos-api", 30);

	}

	@Test
	void deveGerarTokenComDadosDoUsuario() {

		//PREPARAR E MOCKS

		Usuario usuario = mock(Usuario.class);

		when(usuario.getId()).thenReturn(10L);

		when(usuario.getPerfil()).thenReturn(PerfilUsuario.ADMINISTRADOR);


		//executar

		TokenGerado tokenGerado = servicoToken.gerar(usuario);

		Jwt jwt = jwtDecoder.decode(tokenGerado.valor());

		// VERIFICAR

		assertFalse(tokenGerado.valor().isBlank());

		assertEquals("aluguel-equipamentos-api", jwt.getClaimAsString("iss"));

		assertEquals("10", jwt.getSubject());

		assertEquals(
				"ADMINISTRADOR",
				jwt.getClaimAsString("perfil")
		);

		assertNotNull(jwt.getIssuedAt());

		assertNotNull(jwt.getExpiresAt());

		assertEquals(
				Duration.ofMinutes(30),
				Duration.between(
						jwt.getIssuedAt(),
						jwt.getExpiresAt()
				)
		);

		assertEquals(
				tokenGerado.expiraEm().getEpochSecond(),
				jwt.getExpiresAt().getEpochSecond()
		);
	}


}
