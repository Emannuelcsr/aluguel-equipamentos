package br.com.projetosecsr.aluguelequipamentos.compartilhado.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ConfiguracaoRestClient {

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
