
package br.com.projetosecsr.aluguelequipamentos.compartilhado.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class ConfiguracaoOpenApi {

	@Bean
	public OpenAPI configurarOpenApi() {

		String esquema = "bearerAuth";

		return new OpenAPI()
				.info(new Info().title("Sistema de Aluguel de Equipamentos").version("1.0.0").description("""
						API REST para gerenciamento de aluguel de
						equipamentos, desenvolvida com Java e Spring Boot.

						Funcionalidades:
						- Autenticação e autorização com JWT
						- Gerenciamento de usuários e clientes
						- Cadastro de categorias e equipamentos
						- Controle de unidades e disponibilidade
						- Reservas e aprovação
						- Registro de retiradas
						- Registro de devoluções
						- Controle de equipamentos em manutenção

						Perfis de acesso:
						ADMINISTRADOR: gerenciamento e aprovação.
						FUNCIONARIO: operações permitidas de atendimento.

						Para testar endpoints protegidos, utilize
						o botão Authorize e informe seu token JWT.
						"""))

				.addTagsItem(new Tag().name("01 - Autenticação").description("Login e autenticação de usuários."))

				.addTagsItem(new Tag().name("02 - Usuários").description("Gerenciamento de usuários e permissões."))

				.addTagsItem(
						new Tag().name("03 - Clientes").description("Cadastro e gerenciamento de clientes PF e PJ."))

				.addTagsItem(
						new Tag().name("04 - Categorias").description("Organização das categorias de equipamentos."))

				.addTagsItem(
						new Tag().name("05 - Equipamentos").description("Cadastro e gerenciamento dos equipamentos."))

				.addTagsItem(new Tag().name("06 - Unidades de Equipamento")
						.description("Controle das unidades e seus estados."))

				.addTagsItem(new Tag().name("07 - Reservas").description("Solicitação e gerenciamento de reservas."))

				.addTagsItem(new Tag().name("08 - Retiradas")
						.description("Registro de entrega dos equipamentos ao cliente."))

				.addTagsItem(new Tag().name("09 - Devoluções")
						.description("Registro de recebimento e condição física dos equipamentos."))

				.components(new Components().addSecuritySchemes(esquema,
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
								.description("Informe o token JWT obtido no login.")))

				.addSecurityItem(new SecurityRequirement().addList(esquema));
	}
}
