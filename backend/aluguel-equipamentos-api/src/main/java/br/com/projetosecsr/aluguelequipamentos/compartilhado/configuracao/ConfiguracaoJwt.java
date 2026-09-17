package br.com.projetosecsr.aluguelequipamentos.compartilhado.configuracao;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
@Configuration
public class ConfiguracaoJwt {

	private final String segredo;

	public ConfiguracaoJwt(
			@Value("${app.jwt.segredo}") String segredo) {

		this.segredo = segredo;
	}

	@Bean
	SecretKey chaveJwt() {

		byte[] segredoDecodificado =
				Base64.getDecoder().decode(segredo);

		return new SecretKeySpec(
				segredoDecodificado,
				"HmacSHA256"
		);
	}


	@Bean
	JwtEncoder jwtEncoder(SecretKey chaveJwt) {

		return NimbusJwtEncoder
				.withSecretKey(chaveJwt)
				.algorithm(MacAlgorithm.HS256)
				.build();
	}




}