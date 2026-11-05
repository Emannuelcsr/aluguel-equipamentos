
package br.com.projetosecsr.aluguelequipamentos.compartilhado.configuracao;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import br.com.projetosecsr.aluguelequipamentos.autenticacao.seguranca.ConversorAutenticacaoJwt;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorAcessoNegado;
import br.com.projetosecsr.aluguelequipamentos.compartilhado.seguranca.TratadorFalhaAutenticacao;

@Configuration
public class ConfiguracaoDeSeguranca {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ConversorAutenticacaoJwt conversorAutenticacaoJwt,
            TratadorFalhaAutenticacao tratadorFalhaAutenticacao,
            TratadorAcessoNegado tratadorAcessoNegado) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(autorizacao -> autorizacao

                // Swagger
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html"
                ).permitAll()

                // Autenticação
                .requestMatchers(HttpMethod.POST, "/api/autenticacao/login")
                    .permitAll()

                // Usuários
                .requestMatchers(HttpMethod.POST, "/api/usuarios")
                    .hasRole("ADMINISTRADOR")

                .requestMatchers(HttpMethod.GET, "/api/usuarios/**")
                    .hasRole("ADMINISTRADOR")

                .requestMatchers(HttpMethod.PUT, "/api/usuarios/**")
                    .hasRole("ADMINISTRADOR")

                .requestMatchers(HttpMethod.PATCH, "/api/usuarios/me/senha")
                    .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/usuarios/*/senha"
                ).hasRole("ADMINISTRADOR")

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/usuarios/*/ativar",
                    "/api/usuarios/*/desativar"
                ).hasRole("ADMINISTRADOR")

                // Clientes
                .requestMatchers(HttpMethod.POST, "/api/clientes")
                    .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                .requestMatchers(HttpMethod.GET, "/api/clientes/**")
                    .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                .requestMatchers(HttpMethod.PUT, "/api/clientes/**")
                    .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/clientes/*/ativar",
                    "/api/clientes/*/desativar"
                ).hasRole("ADMINISTRADOR")

                // Categorias
                .requestMatchers(HttpMethod.POST, "/api/categorias/**")
                    .hasRole("ADMINISTRADOR")

                .requestMatchers(HttpMethod.GET, "/api/categorias/**")
                    .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                .requestMatchers(HttpMethod.PUT, "/api/categorias/**")
                    .hasRole("ADMINISTRADOR")

                .requestMatchers(HttpMethod.PATCH, "/api/categorias/**")
                    .hasRole("ADMINISTRADOR")

                // Equipamentos
                .requestMatchers(HttpMethod.POST, "/api/equipamentos/**")
                    .hasRole("ADMINISTRADOR")

                .requestMatchers(HttpMethod.GET, "/api/equipamentos/**")
                    .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                .requestMatchers(HttpMethod.PUT, "/api/equipamentos/**")
                    .hasRole("ADMINISTRADOR")

                .requestMatchers(HttpMethod.PATCH, "/api/equipamentos/**")
                    .hasRole("ADMINISTRADOR")

                // Unidades de equipamento
                .requestMatchers(HttpMethod.POST, "/api/unidades-equipamento/**")
                    .hasRole("ADMINISTRADOR")

                .requestMatchers(HttpMethod.GET, "/api/unidades-equipamento/**")
                    .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/unidades-equipamento/*/ativar",
                    "/api/unidades-equipamento/*/desativar"
                ).hasRole("ADMINISTRADOR")

                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/unidades-equipamento/*/manutencao",
                    "/api/unidades-equipamento/*/liberar-manutencao"
                ).hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                // Retiradas
                .requestMatchers(HttpMethod.POST, "/api/retiradas")
                    .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                .requestMatchers(HttpMethod.GET, "/api/retiradas/*")
                    .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                .requestMatchers(HttpMethod.GET, "/api/retiradas")
                    .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                // Devoluções
                .requestMatchers(HttpMethod.POST, "/api/devolucoes")
                    .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                .requestMatchers(HttpMethod.GET, "/api/devolucoes/*")
                    .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                .requestMatchers(HttpMethod.GET, "/api/devolucoes")
                    .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                // Reservas
                .requestMatchers(HttpMethod.POST, "/api/reservas")
                    .hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/reservas",
                    "/api/reservas/*"
                ).hasAnyRole("ADMINISTRADOR", "FUNCIONARIO")

                .requestMatchers(HttpMethod.PATCH, "/api/reservas/**")
                    .hasRole("ADMINISTRADOR")

                // Bloqueia endpoints não autorizados
                .anyRequest().denyAll()
            )
            .exceptionHandling(excecoes -> excecoes
                .authenticationEntryPoint(tratadorFalhaAutenticacao)
                .accessDeniedHandler(tratadorAcessoNegado)
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .authenticationEntryPoint(tratadorFalhaAutenticacao)
                .jwt(jwt ->
                    jwt.jwtAuthenticationConverter(conversorAutenticacaoJwt)
                )
            );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuracao = new CorsConfiguration();

        configuracao.setAllowedOrigins(
            List.of("http://localhost:4200")
        );

        configuracao.setAllowedMethods(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuracao.setAllowedHeaders(
            List.of("Authorization", "Content-Type")
        );

        UrlBasedCorsConfigurationSource fonte =
            new UrlBasedCorsConfigurationSource();

        fonte.registerCorsConfiguration("/**", configuracao);

        return fonte;
    }
}
