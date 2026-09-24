package br.com.projetosecsr.aluguelequipamentos.compartilhado.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorFalhaAutenticacao;

@Configuration
public class ConfiguracaoDeSeguranca {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter,
			TratadorFalhaAutenticacao tratadorFalhaAutenticacao) throws Exception {

		http.csrf(AbstractHttpConfigurer::disable).formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable).logout(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(
						autorizacao -> autorizacao.requestMatchers(HttpMethod.POST, "/api/autenticacao/login")
								.permitAll().requestMatchers(HttpMethod.POST, "/api/usuarios").hasRole("ADMINISTRADOR")
								.anyRequest().denyAll())

				.exceptionHandling(excecoes -> excecoes.authenticationEntryPoint(tratadorFalhaAutenticacao))

				.oauth2ResourceServer(oauth2 -> oauth2.authenticationEntryPoint(tratadorFalhaAutenticacao)
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

		return http.build();
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {

		JwtGrantedAuthoritiesConverter conversorDeAutoridades = new JwtGrantedAuthoritiesConverter();

		conversorDeAutoridades.setAuthoritiesClaimName("perfil");
		conversorDeAutoridades.setAuthorityPrefix("ROLE_");

		JwtAuthenticationConverter conversorDeAutenticacao = new JwtAuthenticationConverter();

		conversorDeAutenticacao.setJwtGrantedAuthoritiesConverter(conversorDeAutoridades);

		return conversorDeAutenticacao;

	}

}
