package br.com.projetosecsr.aluguelequipamentos.usuario.inicializacao;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.PerfilUsuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.entidade.Usuario;
import br.com.projetosecsr.aluguelequipamentos.usuario.repository.UsuarioRepository;
import br.com.projetosecsr.aluguelequipamentos.usuario.request.CadastrarUsuarioRequest;
import br.com.projetosecsr.aluguelequipamentos.usuario.service.UsuarioService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Component
@ConditionalOnProperty(name = "app.admin-inicial.habilitado", havingValue = "true")
public class InicializadorAdministrador implements ApplicationRunner {

	private final UsuarioService usuarioService;
	private final UsuarioRepository usuarioRepository;
	private final Validator validator;
	private final String nome;
	private final String email;
	private final String senha;

	public InicializadorAdministrador(UsuarioService usuarioService, UsuarioRepository usuarioRepository,
			Validator validator, @Value("${app.admin-inicial.nome}") String nome,
			@Value("${app.admin-inicial.email}") String email, @Value("${app.admin-inicial.senha}") String senha) {

		this.usuarioService = usuarioService;
		this.usuarioRepository = usuarioRepository;
		this.validator = validator;
		this.nome = nome;
		this.email = email;
		this.senha = senha;
	}

	@Override
	public void run(ApplicationArguments args) {

		CadastrarUsuarioRequest request = new CadastrarUsuarioRequest(nome, email, senha, PerfilUsuario.ADMINISTRADOR);

		Set<ConstraintViolation<CadastrarUsuarioRequest>> violacoes = validator.validate(request);

		if (!violacoes.isEmpty()) {

			String mensagens = violacoes.stream().map(ConstraintViolation::getMessage).sorted()
					.collect(Collectors.joining(" "));

			throw new IllegalStateException("Configuração do administrador inicial inválida: " + mensagens);
		}

		String emailNormalizado = email.strip().toLowerCase(Locale.ROOT);

		Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(emailNormalizado);

		if (usuarioExistente.isPresent()) {

			if (usuarioExistente.get().getPerfil() != PerfilUsuario.ADMINISTRADOR) {

				throw new IllegalStateException("O e-mail do administrador inicial pertence a um funcionário.");
			}

			return;
		}

		usuarioService.cadastrar(request);
	}

}
