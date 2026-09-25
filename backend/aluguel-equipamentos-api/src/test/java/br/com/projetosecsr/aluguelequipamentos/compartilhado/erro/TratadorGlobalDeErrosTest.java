package br.com.projetosecsr.aluguelequipamentos.compartilhado.erro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.projetosecsr.aluguelequipamentos.compartilhado.paginacao.excecao.PaginaInvalidaException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.EmailJaCadastradoException;
import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.UsuarioNaoEncontradoException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = TratadorGlobalDeErrosTest.ControllerValidacaoTeste.class)
@Import({ TratadorGlobalDeErros.class, TratadorGlobalDeErrosTest.ControllerValidacaoTeste.class })
public class TratadorGlobalDeErrosTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void deveRetornarConflitoQuandoEmailJaEstiverCadastrado() throws Exception {

		mockMvc.perform(post("/test/email-duplicado")).andExpect(status().isConflict())
				.andExpect(jsonPath("$.timestamp").exists()).andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.erro").value("Conflito de dados"))
				.andExpect(jsonPath("$.mensagens[0]").value("O e-mail informado já está cadastrado."))
				.andExpect(jsonPath("$.path").value("/test/email-duplicado"))
				.andExpect(jsonPath("$.codigo").value("EMAIL_JA_CADASTRADO"));

	}

	@Test
	void deveRetornarErroPadronizadoQuandoNomeForInvalido() throws Exception {

		mockMvc.perform(post("/teste/validacao").contentType(MediaType.APPLICATION_JSON).content("""
				{
				  "nome": ""
				}
				""")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.erro").value("Dados inválidos"))
				.andExpect(jsonPath("$.mensagens[0]").value("O nome é obrigatório"))
				.andExpect(jsonPath("$.path").value("/teste/validacao"))
				.andExpect(jsonPath("$.codigo").value("DADOS_INVALIDOS"));
	}

	private record DadosValidosTeste(

			@NotBlank(message = "O nome é obrigatório") String nome

	) {

	}

	@RestController
	static class ControllerValidacaoTeste {

		@PostMapping("/teste/validacao")
		void validar(@Valid @RequestBody DadosValidosTeste dados) {

		}

		@PostMapping("/test/email-duplicado")
		void simularEmailDuplicado() {

			throw new EmailJaCadastradoException();
		}

		@GetMapping("/teste/usuario-nao-encontrado")
		void simularUsuarioNaoEncontrado() {

			throw new UsuarioNaoEncontradoException();
		}

		@GetMapping("/teste/pagina-invalida")
		void lancarPaginaInvalida() {

			throw new PaginaInvalidaException("A página informada não existe");
		}

	}

	@Test
	void deveRetornarNaoEncontradoQuandoUsuarioNaoExistir() throws Exception {

		// EXECUTAR E VERIFICAR
		mockMvc.perform(get("/teste/usuario-nao-encontrado")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.timestamp").exists()).andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
				.andExpect(jsonPath("$.mensagens[0]").value("Usuário não encontrado."))
				.andExpect(jsonPath("$.path").value("/teste/usuario-nao-encontrado"))
				.andExpect(jsonPath("$.codigo").value("USUARIO_NAO_ENCONTRADO"));
	}

	@Test
	void deveRetornarBadRequestQuandoPaginaForInvalida() throws Exception {

		// EXECUTAR E VERIFICAR
		mockMvc.perform(get("/teste/pagina-invalida")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.timestamp").exists()).andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.erro").value("Parâmetros de paginação inválidos"))
				.andExpect(jsonPath("$.mensagens[0]").value("A página informada não existe"))
				.andExpect(jsonPath("$.path").value("/teste/pagina-invalida"))
				.andExpect(jsonPath("$.codigo").value("PAGINA_NAO_ENCONTRADA"));
	}

}
