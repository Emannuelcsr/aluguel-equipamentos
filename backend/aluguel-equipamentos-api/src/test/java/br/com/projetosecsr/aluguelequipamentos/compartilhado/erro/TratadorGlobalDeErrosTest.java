package br.com.projetosecsr.aluguelequipamentos.compartilhado.erro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.projetosecsr.aluguelequipamentos.usuario.excecao.EmailJaCadastradoException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@WebMvcTest
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

	}
}
